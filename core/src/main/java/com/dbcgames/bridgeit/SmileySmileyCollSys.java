package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.github.tommyettinger.gand.Float2UndirectedGraph;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gdcrux.PointF2;

// Detects smiley-smiley overlaps and sends each to a new random floor tile.
public class SmileySmileyCollSys extends BaseSystem {
    private final TiledMap map;
    private final TiledMapTileLayer layer;
    private final float tileWidth;
    private final float tileHeight;
    private final float collisionStep;
    private final int mapWidth;
    private final int mapHeight;
    private boolean[][] passable;
    private Float2UndirectedGraph graph;
    private final Aspect.Builder smileyAspect = Aspect.all(MobCmp.class, BBoxCmp.class, AiCmp.class, SmileyCmp.class);

    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<BBoxCmp> bboxMap;
    private ComponentMapper<AiCmp> aiMap;
    private ComponentMapper<SmileyCmp> smileyMap;

    public SmileySmileyCollSys(TiledMap map) {
        this.map = map;
        this.layer = map != null && map.getLayers().getCount() > 0 ? (TiledMapTileLayer) map.getLayers().get(0) : null;
        if (layer == null) {
            tileWidth = 0f;
            tileHeight = 0f;
            collisionStep = 0.25f;
            mapWidth = 0;
            mapHeight = 0;
            passable = new boolean[0][0];
            graph = new Float2UndirectedGraph();
            return;
        }

        tileWidth = layer.getTileWidth();
        tileHeight = layer.getTileHeight();
        collisionStep = Math.max(1f, Math.min(tileWidth, tileHeight) * 0.25f);
        mapWidth = layer.getWidth();
        mapHeight = layer.getHeight();
        rebuildNavigation();
    }

    @Override
    protected void processSystem() {
        if (graph.size() == 0)
            return;

        IntBag smileys = world.getAspectSubscriptionManager().get(smileyAspect).getEntities();
        int count = smileys.size();
        for (int i = 0; i < count; i++) {
            int e1 = smileys.get(i);
            MobCmp mob1 = mobMap.get(e1);
            if (mob1.type != MobType.SMILEY)
                continue;
            SmileyCmp smiley1 = smileyMap.get(e1);
            BBoxCmp box1 = bboxMap.get(e1);
            float minX1 = mob1.x + box1.bottomLeftX;
            float maxX1 = mob1.x + box1.bottomRightX;
            float minY1 = mob1.y + box1.bottomLeftY;
            float maxY1 = mob1.y + box1.topLeftY;

            for (int j = i + 1; j < count; j++) {
                int e2 = smileys.get(j);
                MobCmp mob2 = mobMap.get(e2);
                if (mob2.type != MobType.SMILEY)
                    continue;
                SmileyCmp smiley2 = smileyMap.get(e2);
                BBoxCmp box2 = bboxMap.get(e2);
                float minX2 = mob2.x + box2.bottomLeftX;
                float maxX2 = mob2.x + box2.bottomRightX;
                float minY2 = mob2.y + box2.bottomLeftY;
                float maxY2 = mob2.y + box2.topLeftY;

                boolean overlaps =
                    minX1 <= maxX2 && maxX1 >= minX2 &&
                    minY1 <= maxY2 && maxY1 >= minY2;

                if (overlaps) {
                    if (smiley1.runawayTime > 0f || smiley2.runawayTime > 0f) {
                        continue;
                    }
                    redirectSmiley(e1);
                    redirectSmiley(e2);
                }
            }
        }
    }

    private void redirectSmiley(int e) {
        MobCmp mob = mobMap.get(e);
        if (mob.type != MobType.SMILEY)
            return;

        PointF2 destTile = Map.sampleRandomFloorTile(map);
        if (destTile.x < 0f || destTile.y < 0f)
            return;

        float destX = Map.tileToWorldX(destTile.x, tileWidth);
        float destY = Map.tileToWorldY(destTile.y, tileHeight);

        AiCmp ai = aiMap.get(e);
        BBoxCmp box = bboxMap.get(e);
        Path<PointF2> path = Map.computeSmoothedPath(
            mob,
            box,
            destX,
            destY,
            map,
            graph,
            passable,
            tileWidth,
            tileHeight,
            mapWidth,
            mapHeight,
            collisionStep);
        SmileyCmp smiley = smileyMap.get(e);
        if (path == null || path.size() == 0) {
            smiley.runawayTime = 0f;
            smiley.runawayTarget.x = -1f;
            smiley.runawayTarget.y = -1f;
            return;
        }

        ai.path = path;
        ai.pathIndex = Math.min(1, ai.path.size() - 1);
        ai.repathTimer = 0f;
        smiley.runawayTime = Smiley.DEFAULT_RUNAWAY_TIME;
        smiley.runawayTarget.x = destTile.x;
        smiley.runawayTarget.y = destTile.y;
    }

    public void rebuildNavigation() {
        if (layer == null) {
            passable = new boolean[0][0];
            graph = new Float2UndirectedGraph();
            return;
        }
        passable = new boolean[mapWidth][mapHeight];
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                passable[x][y] = Map.isFloor(map, Map.tileToWorldX(x, tileWidth), Map.tileToWorldY(y, tileHeight));
            }
        }
        graph = new Float2UndirectedGraph(passable, 1f, PointF2::dst, true);
    }
}
