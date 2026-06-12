package com.dbcgames.bridgeit;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.gand.Float2UndirectedGraph;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gand.smoothing.PathSmoother;
import com.github.tommyettinger.gand.smoothing.RaycastCollisionDetector;
import com.github.tommyettinger.gdcrux.PointF2;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.place.MixedGenerator;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Map related functions
public final class Map {
    // Names of maps
    // Add new TMX files here so AssetManager loads them before use.
    public static final String[] mapNames = {
        "debug",
        "bridge-up",
        "bridge-down",
        "bridge-left",
        "bridge-right"
    };
    // Number of random samples to try when searching for a distant floor tile
    public static final int FURTHEST_FROM_POINT_SAMPLES = 64;
    // Tile id used to fill space surrounding an attached bridge section
    private static final int BRIDGE_FILL_TILE_ID = 20;
    // Special tiles to replace
    private static final int TREASURE_TILE_ID = 23;
    private static final int BROKEN_TILE_ID = 5;
    private static final int TMPWALL_TILE_ID = 4;

    // Loads maps into asset manager
    public static void loadMaps(AssetManager assman) {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.textureMinFilter = TextureFilter.Nearest;
        params.textureMagFilter = TextureFilter.Nearest;
        for (String mapname : mapNames) {
            assman.load("maps/" + mapname + ".tmx", TiledMap.class, params);
        }
    }

    public static TiledMap genMap(int width, int height) {
        EnhancedRandom rng = new WhiskerRandom(TimeUtils.millis());
        MixedGenerator generator = new MixedGenerator(width, height, rng);
        generator.putCaveCarvers(2);
        generator.putBoxRoomCarvers(1);
        generator.putRoundRoomCarvers(1);
        char[][] generated = generator.generate();
        char[][] scaled = scaleMap(generated, 2);
        int[][] bridged = bridgeMap(scaled, rng);
        return buildTiledMapFromIds(bridged);
    }

    public static void replaceSpecialTiles(TiledMap map, int brokenToActivate) {
        if (map == null || map.getLayers().getCount() == 0) {
            Gdx.app.log("Map", "Cannot replace special tiles; map missing or has no layers");
            return;
        }
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        if (layer == null) {
            Gdx.app.log("Map", "Cannot replace special tiles; first layer missing");
            return;
        }
        int width = layer.getWidth();
        int height = layer.getHeight();
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();

        List<PointF2> brokenTiles = new ArrayList<>();
        PointF2 treasureTile = null;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }
                int tileId = cell.getTile().getId();
                if (tileId == TMPWALL_TILE_ID) {
                    layer.setCell(x, y, null);
                } else if (tileId == TREASURE_TILE_ID) {
                    if (treasureTile == null) {
                        treasureTile = new PointF2(x, y);
                    } else {
                        Gdx.app.log("Map", "Multiple treasure tiles found; using the first");
                    }
                    layer.setCell(x, y, null);
                } else if (tileId == BROKEN_TILE_ID) {
                    brokenTiles.add(new PointF2(x, y));
                    layer.setCell(x, y, null);
                }
            }
        }

        if (treasureTile != null) {
            float tx = tileToWorldX(treasureTile.x, tileWidth);
            float ty = tileToWorldY(treasureTile.y, tileHeight);
            Treasure.createTreasure(tx, ty);
        } else {
            Gdx.app.log("Map", "No treasure tile found to replace");
        }

        if (!brokenTiles.isEmpty() && brokenToActivate > 0) {
            BrokenOrder order = inferBrokenOrder(brokenTiles, treasureTile, width, height);
            Comparator<PointF2> cmp = comparatorForBroken(order);
            Collections.sort(brokenTiles, cmp);
            int activateCount = Math.min(brokenToActivate, brokenTiles.size());
            for (int i = 0; i < activateCount; i++) {
                PointF2 tile = brokenTiles.get(i);
                float bx = tileToWorldX(tile.x, tileWidth);
                float by = tileToWorldY(tile.y, tileHeight);
                Broken.createBroken(bx, by);
            }
        } else if (brokenToActivate > 0) {
            Gdx.app.log("Map", "No broken tiles found to activate");
        }
    }

    private enum BrokenOrder {
        RIGHT,
        LEFT,
        UP,
        DOWN,
        UNKNOWN
    }

    private static BrokenOrder inferBrokenOrder(List<PointF2> brokenTiles, PointF2 treasureTile, int mapWidth, int mapHeight) {
        if (brokenTiles == null || brokenTiles.isEmpty()) {
            return BrokenOrder.UNKNOWN;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (PointF2 pt : brokenTiles) {
            minX = Math.min(minX, pt.x);
            minY = Math.min(minY, pt.y);
            maxX = Math.max(maxX, pt.x);
            maxY = Math.max(maxY, pt.y);
        }
        float rectWidth = maxX - minX + 1f;
        float rectHeight = maxY - minY + 1f;
        BrokenOrder order = BrokenOrder.UNKNOWN;
        if (rectWidth >= rectHeight) {
            if (treasureTile != null) {
                if (treasureTile.x > maxX) {
                    order = BrokenOrder.RIGHT;
                } else if (treasureTile.x < minX) {
                    order = BrokenOrder.LEFT;
                }
            }
            if (order == BrokenOrder.UNKNOWN) {
                order = minX <= mapWidth * 0.5f ? BrokenOrder.RIGHT : BrokenOrder.LEFT;
            }
        } else {
            if (treasureTile != null) {
                if (treasureTile.y > maxY) {
                    order = BrokenOrder.DOWN;
                } else if (treasureTile.y < minY) {
                    order = BrokenOrder.UP;
                }
            }
            if (order == BrokenOrder.UNKNOWN) {
                order = minY <= mapHeight * 0.5f ? BrokenOrder.DOWN : BrokenOrder.UP;
            }
        }
        return order;
    }

    private static Comparator<PointF2> comparatorForBroken(BrokenOrder order) {
        switch (order) {
        case LEFT:
            return (a, b) -> {
                int cmpX = Float.compare(b.x, a.x);
                if (cmpX != 0) {
                    return cmpX;
                }
                return Float.compare(a.y, b.y);
            };
        case UP:
            return (a, b) -> {
                int cmpY = Float.compare(b.y, a.y);
                if (cmpY != 0) {
                    return cmpY;
                }
                return Float.compare(a.x, b.x);
            };
        case DOWN:
            return (a, b) -> {
                int cmpY = Float.compare(a.y, b.y);
                if (cmpY != 0) {
                    return cmpY;
                }
                return Float.compare(a.x, b.x);
            };
        case RIGHT:
        default:
            return (a, b) -> {
                int cmpX = Float.compare(a.x, b.x);
                if (cmpX != 0) {
                    return cmpX;
                }
                return Float.compare(a.y, b.y);
            };
        }
    }

    // Returns true if world coordinates land on a tile whose value is 0 (treated as floor)
    public static boolean isFloor(TiledMap map, float worldX, float worldY) {
        if (map == null || map.getLayers().getCount() == 0)
            return false;

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        int tileX = (int) (worldX / layer.getTileWidth());
        int tileY = (int) (worldY / layer.getTileHeight());

        if (tileX < 0 || tileY < 0 || tileX >= layer.getWidth() || tileY >= layer.getHeight())
            return false;

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        // CSV value 0 yields a null cell in libGDX's TiledMapTileLayer, so treat null as floor.
        if (cell == null || cell.getTile() == null)
            return true;

        // Any non-zero CSV entry yields a populated cell; treat as non-floor.
        return false;
    }

    public static void followPath(MobCmp mob, AiCmp ai, float tileWidth, float tileHeight, float reachEpsilon) {
        if (ai.path == null || ai.pathIndex >= ai.path.size()) {
            mob.vx = 0f;
            mob.vy = 0f;
            return;
        }

        PointF2 target = ai.path.get(ai.pathIndex);
        float targetX = tileToWorldX(target.x, tileWidth);
        float targetY = tileToWorldY(target.y, tileHeight);

        float dx = targetX - mob.x;
        float dy = targetY - mob.y;
        float dist2 = dx * dx + dy * dy;

        if (dist2 < reachEpsilon * reachEpsilon) {
            ai.pathIndex++;
            if (ai.pathIndex >= ai.path.size()) {
                mob.vx = 0f;
                mob.vy = 0f;
                return;
            }
            target = ai.path.get(ai.pathIndex);
            targetX = tileToWorldX(target.x, tileWidth);
            targetY = tileToWorldY(target.y, tileHeight);
            dx = targetX - mob.x;
            dy = targetY - mob.y;
        }

        if (dx == 0f && dy == 0f) {
            mob.vx = 0f;
            mob.vy = 0f;
            return;
        }

        float invLen = (float)(1.0 / Math.sqrt(dx * dx + dy * dy));
        mob.vx = dx * invLen * mob.speed;
        mob.vy = dy * invLen * mob.speed;
        if (dx != 0f) {
            mob.flip = dx < 0f;
        }
    }

    public static Path<PointF2> computeSmoothedPath(
        MobCmp mob,
        BBoxCmp bbox,
        float targetX,
        float targetY,
        TiledMap map,
        Float2UndirectedGraph graph,
        boolean[][] passable,
        float tileWidth,
        float tileHeight,
        int mapWidth,
        int mapHeight,
        float collisionStep) {

        PointF2 startTile = worldToTile(mob.x, mob.y, tileWidth, tileHeight, mapWidth, mapHeight);
        PointF2 goalTile = worldToTile(targetX, targetY, tileWidth, tileHeight, mapWidth, mapHeight);
        adjustToPassable(startTile, passable);
        adjustToPassable(goalTile, passable);

        if (!isTilePassable(startTile, passable) || !isTilePassable(goalTile, passable)) {
            Gdx.app.debug("Map", "No passable start/goal for path");
            return null;
        }

        Path<PointF2> raw = graph.algorithms().findShortestPath(startTile, goalTile, PointF2::dst);
        if (raw == null || raw.size() == 0) {
            return null;
        }

        Path<PointF2> smoothed = new Path<>(raw);
        PathSmoother<PointF2> smoother = new PathSmoother<>(new BoundingRaycast(map, tileWidth, tileHeight, collisionStep, bbox));
        smoother.smoothPath(smoothed);
        return smoothed;
    }

    public static PointF2 worldToTile(float wx, float wy, float tileWidth, float tileHeight, int mapWidth, int mapHeight) {
        if (tileWidth == 0f || tileHeight == 0f) {
            return new PointF2();
        }
        int tx = MathUtils.clamp((int)(wx / tileWidth), 0, mapWidth - 1);
        int ty = MathUtils.clamp((int)(wy / tileHeight), 0, mapHeight - 1);
        return new PointF2(tx, ty);
    }

    public static float tileToWorldX(float tileX, float tileWidth) {
        return tileX * tileWidth + tileWidth * 0.5f;
    }

    public static float tileToWorldY(float tileY, float tileHeight) {
        return tileY * tileHeight + tileHeight * 0.5f;
    }

    public static boolean isTilePassable(PointF2 tile, boolean[][] passable) {
        return isTilePassable((int)tile.x, (int)tile.y, passable);
    }

    public static boolean isTilePassable(int x, int y, boolean[][] passable) {
        int width = passable != null ? passable.length : 0;
        int height = width > 0 && passable[0] != null ? passable[0].length : 0;
        return x >= 0 && y >= 0 && x < width && y < height && passable[x][y];
    }

    public static void adjustToPassable(PointF2 tile, boolean[][] passable) {
        if (isTilePassable(tile, passable)) {
            return;
        }
        int tx = (int)tile.x;
        int ty = (int)tile.y;
        int radius = 1;
        boolean found = false;
        while (!found && radius <= 2) {
            for (int dx = -radius; dx <= radius && !found; dx++) {
                for (int dy = -radius; dy <= radius && !found; dy++) {
                    int nx = tx + dx;
                    int ny = ty + dy;
                    if (isTilePassable(nx, ny, passable)) {
                        tile.x = nx;
                        tile.y = ny;
                        found = true;
                    }
                }
            }
            radius++;
        }
    }

    public static boolean collidesAt(TiledMap map, float cx, float cy, BBoxCmp box) {
        return !(Map.isFloor(map, cx + box.topLeftX, cy + box.topLeftY) &&
                 Map.isFloor(map, cx + box.topRightX, cy + box.topRightY) &&
                 Map.isFloor(map, cx + box.bottomLeftX, cy + box.bottomLeftY) &&
                 Map.isFloor(map, cx + box.bottomRightX, cy + box.bottomRightY));
    }

    public static PointF2 sampleFurthestFloorTile(TiledMap map, float fromTileX, float fromTileY) {
        if (map == null || map.getLayers().getCount() == 0) {
            Gdx.app.log("Map", "No layers on map; cannot sample furthest floor tile");
            return new PointF2(-1f, -1f);
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        int width = layer.getWidth();
        int height = layer.getHeight();
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();
        if (width <= 0 || height <= 0) {
            Gdx.app.log("Map", "Map layer has no tiles; cannot sample furthest floor tile");
            return new PointF2(-1f, -1f);
        }

        boolean[][] passable = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                passable[x][y] = Map.isFloor(map, Map.tileToWorldX(x, tileWidth), Map.tileToWorldY(y, tileHeight));
            }
        }
        Float2UndirectedGraph graph = new Float2UndirectedGraph(passable, 1f, PointF2::dst, true);

        PointF2 start = new PointF2(
            MathUtils.clamp((int)fromTileX, 0, width - 1),
            MathUtils.clamp((int)fromTileY, 0, height - 1));
        adjustToPassable(start, passable);
        if (!isTilePassable(start, passable)) {
            Gdx.app.log("Map", "Could not find a passable start tile for sampling");
            return new PointF2(-1f, -1f);
        }

        float furthestPathLen = -1f;
        int furthestX = -1;
        int furthestY = -1;

        for (int i = 0; i < FURTHEST_FROM_POINT_SAMPLES; i++) {
            int x = MathUtils.random(width - 1);
            int y = MathUtils.random(height - 1);
            if (!passable[x][y]) {
                continue;
            }
            PointF2 target = new PointF2(x, y);
            Path<PointF2> path = graph.algorithms().findShortestPath(start, target, PointF2::dst);
            if (path == null || path.size() == 0) {
                continue;
            }
            float pathLen = 0f;
            for (int idx = 1; idx < path.size(); idx++) {
                PointF2 a = path.get(idx - 1);
                PointF2 b = path.get(idx);
                float dx = b.x - a.x;
                float dy = b.y - a.y;
                pathLen += (float)Math.sqrt(dx * dx + dy * dy);
            }
            if (pathLen > furthestPathLen) {
                furthestPathLen = pathLen;
                furthestX = x;
                furthestY = y;
            }
        }

        if (furthestPathLen < 0f) {
            Gdx.app.log("Map", "No reachable floor tiles found while sampling for furthest point");
            return new PointF2(-1f, -1f);
        }

        return new PointF2(furthestX, furthestY);
    }

    public static PointF2 sampleRandomFloorTile(TiledMap map) {
        if (map == null || map.getLayers().getCount() == 0) {
            Gdx.app.log("Map", "No layers on map; cannot sample random floor tile");
            return new PointF2(-1f, -1f);
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        int width = layer.getWidth();
        int height = layer.getHeight();
        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();
        if (width <= 0 || height <= 0) {
            Gdx.app.log("Map", "Map layer has no tiles; cannot sample random floor tile");
            return new PointF2(-1f, -1f);
        }

        int maxAttempts = Math.max(1, width * height);
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = MathUtils.random(width - 1);
            int y = MathUtils.random(height - 1);
            if (Map.isFloor(map, Map.tileToWorldX(x, tileWidth), Map.tileToWorldY(y, tileHeight))) {
                return new PointF2(x, y);
            }
        }

        Gdx.app.log("Map", "No floor tiles found while sampling randomly");
        return new PointF2(-1f, -1f);
    }

    private static int[][] bridgeMap(char[][] map, EnhancedRandom rng) {
        if (map == null || map.length == 0 || map[0] == null || map[0].length == 0) {
            return null;
        }
        int[][] base = charsToTileIds(map);
        int baseWidth = base.length;
        int baseHeight = base[0].length;

        EnhancedRandom random = rng != null ? rng : new WhiskerRandom(TimeUtils.millis());
        BridgeSide side = BridgeSide.fromIndex(random.nextInt(BridgeSide.values().length));
        int[][] bridgeTemplate = loadBridgeTemplate(side);
        if (bridgeTemplate == null) {
            return base;
        }
        int[][] orientedBridge = copyBridge(bridgeTemplate);

        int bridgeWidth = orientedBridge.length;
        int bridgeHeight = orientedBridge[0].length;

        int finalWidth = baseWidth;
        int finalHeight = baseHeight;
        int baseOffsetX = 0;
        int baseOffsetY = 0;
        int bridgeOffsetX = 0;
        int bridgeOffsetY = 0;
        int clearDx = 0;
        int clearDy = 0;
        int entryStart = 0;

        switch (side) {
        case RIGHT: {
            int maxYOffset = Math.max(0, baseHeight - bridgeHeight);
            int yOffset = maxYOffset > 0 ? random.nextInt(maxYOffset + 1) : 0;
            finalWidth = baseWidth + bridgeWidth;
            finalHeight = baseHeight;
            bridgeOffsetX = baseWidth;
            bridgeOffsetY = yOffset;
            clearDx = -1;
            clearDy = 0;
            entryStart = MathUtils.clamp((bridgeHeight / 2) - 1, 0, Math.max(0, bridgeHeight - 3));
            break;
        }
        case LEFT: {
            int maxYOffset = Math.max(0, baseHeight - bridgeHeight);
            int yOffset = maxYOffset > 0 ? random.nextInt(maxYOffset + 1) : 0;
            finalWidth = baseWidth + bridgeWidth;
            finalHeight = baseHeight;
            baseOffsetX = bridgeWidth;
            bridgeOffsetX = 0;
            bridgeOffsetY = yOffset;
            clearDx = 1;
            clearDy = 0;
            entryStart = MathUtils.clamp((bridgeHeight / 2) - 1, 0, Math.max(0, bridgeHeight - 3));
            break;
        }
        case TOP: {
            int maxXOffset = Math.max(0, baseWidth - bridgeWidth);
            int xOffset = maxXOffset > 0 ? random.nextInt(maxXOffset + 1) : 0;
            finalWidth = baseWidth;
            finalHeight = baseHeight + bridgeHeight;
            baseOffsetY = 0;
            bridgeOffsetX = xOffset;
            bridgeOffsetY = baseHeight;
            clearDx = 0;
            clearDy = -1;
            entryStart = MathUtils.clamp((bridgeWidth / 2) - 1, 0, Math.max(0, bridgeWidth - 3));
            break;
        }
        case BOTTOM: {
            int maxXOffset = Math.max(0, baseWidth - bridgeWidth);
            int xOffset = maxXOffset > 0 ? random.nextInt(maxXOffset + 1) : 0;
            finalWidth = baseWidth;
            finalHeight = baseHeight + bridgeHeight;
            baseOffsetY = bridgeHeight;
            bridgeOffsetX = xOffset;
            bridgeOffsetY = 0;
            clearDx = 0;
            clearDy = 1;
            entryStart = MathUtils.clamp((bridgeWidth / 2) - 1, 0, Math.max(0, bridgeWidth - 3));
            break;
        }
        }

        int[][] result = new int[finalWidth][finalHeight];
        boolean[][] originalArea = new boolean[finalWidth][finalHeight];
        boolean[][] bridgeArea = new boolean[finalWidth][finalHeight];

        for (int x = 0; x < baseWidth; x++) {
            for (int y = 0; y < baseHeight; y++) {
                int targetX = baseOffsetX + x;
                int targetY = baseOffsetY + y;
                result[targetX][targetY] = base[x][y];
                originalArea[targetX][targetY] = true;
            }
        }

        for (int x = 0; x < bridgeWidth; x++) {
            for (int y = 0; y < bridgeHeight; y++) {
                int targetX = bridgeOffsetX + x;
                int targetY = bridgeOffsetY + y;
                result[targetX][targetY] = orientedBridge[x][y];
                bridgeArea[targetX][targetY] = true;
            }
        }

        int[] entryXs = new int[3];
        int[] entryYs = new int[3];
        switch (side) {
        case RIGHT:
            for (int i = 0; i < 3; i++) {
                entryXs[i] = bridgeOffsetX;
                entryYs[i] = bridgeOffsetY + entryStart + i;
            }
            break;
        case LEFT:
            for (int i = 0; i < 3; i++) {
                entryXs[i] = bridgeOffsetX + bridgeWidth - 1;
                entryYs[i] = bridgeOffsetY + entryStart + i;
            }
            break;
        case TOP:
            for (int i = 0; i < 3; i++) {
                entryXs[i] = bridgeOffsetX + entryStart + i;
                entryYs[i] = bridgeOffsetY;
            }
            break;
        case BOTTOM:
            for (int i = 0; i < 3; i++) {
                entryXs[i] = bridgeOffsetX + entryStart + i;
                entryYs[i] = bridgeOffsetY + bridgeHeight - 1;
            }
            break;
        }

        clearEntrance(result, entryXs, entryYs, clearDx, clearDy);
        fillSurroundingSpace(result, originalArea, bridgeArea);
        return result;
    }

    private static int[][] charsToTileIds(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        int[][] tiles = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = map[x][y] == '#' ? 1 : 0;
            }
        }
        return tiles;
    }

    private static int[][] loadBridgeTemplate(BridgeSide side) {
        String mapPath;
        switch (side) {
        case LEFT:
            mapPath = "maps/bridge-left.tmx";
            break;
        case TOP:
            mapPath = "maps/bridge-up.tmx";
            break;
        case BOTTOM:
            mapPath = "maps/bridge-down.tmx";
            break;
        case RIGHT:
        default:
            mapPath = "maps/bridge-right.tmx";
            break;
        }
        if (Game.assman == null || !Game.assman.isLoaded(mapPath, TiledMap.class)) {
            Gdx.app.log("Map", "Bridge map not loaded (" + mapPath + "); skipping bridge transform");
            return null;
        }
        TiledMap bridge = Game.assman.get(mapPath, TiledMap.class);
        if (bridge == null || bridge.getLayers().getCount() == 0) {
            Gdx.app.log("Map", "Bridge map missing layers; skipping bridge transform");
            return null;
        }
        TiledMapTileLayer layer = (TiledMapTileLayer) bridge.getLayers().get(0);
        int width = layer.getWidth();
        int height = layer.getHeight();
        int[][] tiles = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                tiles[x][y] = cell != null && cell.getTile() != null ? cell.getTile().getId() : 0;
            }
        }
        return tiles;
    }

    private static int[][] copyBridge(int[][] bridge) {
        int width = bridge.length;
        int height = bridge[0].length;
        int[][] copy = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(bridge[x], 0, copy[x], 0, height);
        }
        return copy;
    }

    private static void clearEntrance(int[][] map, int[] entryXs, int[] entryYs, int dx, int dy) {
        if (entryXs.length != entryYs.length || (dx == 0 && dy == 0)) {
            return;
        }
        int width = map.length;
        int height = map[0].length;
        int step = 1;
        boolean keepDigging = true;
        while (keepDigging) {
            keepDigging = false;
            for (int i = 0; i < entryXs.length; i++) {
                int cx = entryXs[i] + dx * step;
                int cy = entryYs[i] + dy * step;
                if (cx < 0 || cy < 0 || cx >= width || cy >= height) {
                    return;
                }
                if (map[cx][cy] != 0) {
                    keepDigging = true;
                }
            }
            if (keepDigging) {
                for (int i = 0; i < entryXs.length; i++) {
                    int cx = entryXs[i] + dx * step;
                    int cy = entryYs[i] + dy * step;
                    if (cx < 0 || cy < 0 || cx >= width || cy >= height) {
                        return;
                    }
                    map[cx][cy] = 0;
                }
            }
            step++;
        }
    }

    private static void fillSurroundingSpace(int[][] map, boolean[][] originalArea, boolean[][] bridgeArea) {
        int width = map.length;
        int height = map[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!originalArea[x][y] && !bridgeArea[x][y] && map[x][y] == 0) {
                    map[x][y] = BRIDGE_FILL_TILE_ID;
                }
            }
        }
    }

    private enum BridgeSide {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM;

        static BridgeSide fromIndex(int index) {
            BridgeSide[] values = values();
            return values[MathUtils.clamp(index, 0, values.length - 1)];
        }
    }

    private static char[][] scaleMap(char[][] source, int scale) {
        if (source == null || source.length == 0 || source[0] == null || source[0].length == 0 || scale <= 1) {
            return source;
        }
        int sourceWidth = source.length;
        int sourceHeight = source[0].length;
        char[][] scaled = new char[sourceWidth * scale][sourceHeight * scale];
        for (int x = 0; x < sourceWidth; x++) {
            char[] column = source[x];
            for (int y = 0; y < sourceHeight; y++) {
                char value = column[y];
                int targetX = x * scale;
                int targetY = y * scale;
                for (int dx = 0; dx < scale; dx++) {
                    for (int dy = 0; dy < scale; dy++) {
                        scaled[targetX + dx][targetY + dy] = value;
                    }
                }
            }
        }
        return scaled;
    }

    private static TiledMap buildTiledMapFromIds(int[][] mapData) {
        if (mapData == null || mapData.length == 0 || mapData[0] == null || mapData[0].length == 0) {
            Gdx.app.log("Map", "Map data missing; cannot build TiledMap");
            return null;
        }
        if (Game.assman == null || !Game.assman.isLoaded("maps/debug.tmx", TiledMap.class)) {
            Gdx.app.log("Map", "Reference map not loaded; cannot build TiledMap");
            return null;
        }
        TiledMap reference = Game.assman.get("maps/debug.tmx", TiledMap.class);
        TiledMapTileLayer referenceLayer = reference != null && reference.getLayers().getCount() > 0 ? (TiledMapTileLayer) reference.getLayers().get(0) : null;
        if (referenceLayer == null) {
            Gdx.app.log("Map", "Reference layer missing; cannot build TiledMap");
            return null;
        }

        int width = mapData.length;
        int height = mapData[0].length;

        TiledMap result = new TiledMap();
        TiledMapTileSet tileSet = reference.getTileSets().getTileSet(0);
        if (tileSet != null) {
            result.getTileSets().addTileSet(tileSet);
        } else {
            Gdx.app.log("Map", "Tile set missing on reference map; cannot build TiledMap");
            return null;
        }

        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, (int) referenceLayer.getTileWidth(), (int) referenceLayer.getTileHeight());
        for (int x = 0; x < width; x++) {
            int[] column = mapData[x];
            for (int y = 0; y < height; y++) {
                int tileId = column[y];
                if (tileId <= 0) {
                    continue;
                }
                TiledMapTile tile = tileSet.getTile(tileId);
                if (tile == null) {
                    continue;
                }
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);
            }
        }

        result.getLayers().add(layer);
        return result;
    }

    private static class BoundingRaycast implements RaycastCollisionDetector<PointF2> {
        private final TiledMap map;
        private final float tileWidth;
        private final float tileHeight;
        private final float collisionStep;
        private final BBoxCmp box;

        BoundingRaycast(TiledMap map, float tileWidth, float tileHeight, float collisionStep, BBoxCmp box) {
            this.map = map;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.collisionStep = collisionStep;
            this.box = box;
        }

        @Override
        public boolean collides(PointPair<PointF2> ray) {
            float startX = tileToWorldX(ray.a.x, tileWidth);
            float startY = tileToWorldY(ray.a.y, tileHeight);
            float endX = tileToWorldX(ray.b.x, tileWidth);
            float endY = tileToWorldY(ray.b.y, tileHeight);

            float dx = endX - startX;
            float dy = endY - startY;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            int steps = Math.max(1, (int)Math.ceil(dist / collisionStep));
            float invSteps = 1f / steps;

            for (int i = 0; i <= steps; i++) {
                float t = i * invSteps;
                float cx = startX + dx * t;
                float cy = startY + dy * t;
                if (collidesAt(map, cx, cy, box)) {
                    return true;
                }
            }
            return false;
        }
    }

}
