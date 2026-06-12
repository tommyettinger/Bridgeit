package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.github.tommyettinger.gand.Float2UndirectedGraph;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gdcrux.PointF2;

@All({MobCmp.class, BBoxCmp.class, AiCmp.class, SmileyCmp.class})
public class SmileyControlSys extends IteratingSystem {
    private static final float REPATH_INTERVAL = 1f;
    private static final float REACH_EPSILON = 4f;

    private final TiledMap map;
    private final TiledMapTileLayer layer;
    private final float tileWidth;
    private final float tileHeight;
    private final float collisionStep;
    private final int mapWidth;
    private final int mapHeight;
    private boolean[][] passable;
    private Float2UndirectedGraph graph;

    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<BBoxCmp> bboxMap;
    private ComponentMapper<AiCmp> aiMap;
    private ComponentMapper<SmileyCmp> smileyMap;

    private boolean hasPlayer = false;
    private float playerX;
    private float playerY;

    public SmileyControlSys(TiledMap map) {
        super(Aspect.all(MobCmp.class, BBoxCmp.class, AiCmp.class));
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
    protected void begin() {
        hasPlayer = false;
        IntBag players = world.getAspectSubscriptionManager()
            .get(Aspect.all(PlayerCmp.class, MobCmp.class, BBoxCmp.class))
            .getEntities();
        if (players.size() == 0)
            return;

        int playerId = players.get(0);
        MobCmp mob = mobMap.get(playerId);
        playerX = mob.x;
        playerY = mob.y;
        hasPlayer = true;
    }

    @Override
    protected void process(int e) {
        MobCmp mob = mobMap.get(e);
        if (mob.type != MobType.SMILEY)
            return;

        AiCmp ai = aiMap.get(e);
        BBoxCmp bbox = bboxMap.get(e);
        SmileyCmp smiley = smileyMap.get(e);
        float dt = world.getDelta();

        if (!hasPlayer || graph.size() == 0) {
            mob.vx = 0f;
            mob.vy = 0f;
            return;
        }

        smiley.runawayTime = Math.max(0f, smiley.runawayTime - dt);

        ai.repathTimer -= dt;
        if (ai.path == null) {
            ai.path = new Path<PointF2>(8);
        }

        boolean runningAway = smiley.runawayTime > 0f;
        if (runningAway) {
            if (ai.path.size() == 0 || ai.pathIndex >= ai.path.size()) {
                smiley.runawayTime = 0f;
                ai.path.clear();
                ai.pathIndex = 0;
                runningAway = false;
            } else {
                Map.followPath(mob, ai, tileWidth, tileHeight, REACH_EPSILON);
                if (ai.pathIndex >= ai.path.size() || smiley.runawayTime <= 0f) {
                    smiley.runawayTime = 0f;
                    ai.path.clear();
                    ai.pathIndex = 0;
                } else {
                    return;
                }
            }
        }

        boolean needsPath = ai.path.size() == 0 || ai.pathIndex >= ai.path.size() || ai.repathTimer <= 0f;
        if (needsPath) {
            Path<PointF2> newPath = Map.computeSmoothedPath(
                mob,
                bbox,
                playerX,
                playerY,
                map,
                graph,
                passable,
                tileWidth,
                tileHeight,
                mapWidth,
                mapHeight,
                collisionStep);
            ai.pathIndex = 0;
            ai.repathTimer = REPATH_INTERVAL;
            if (newPath != null && newPath.size() > 0) {
                ai.path = newPath;
                ai.pathIndex = Math.min(1, ai.path.size() - 1);
            } else {
                ai.path.clear();
            }
        }

        Map.followPath(mob, ai, tileWidth, tileHeight, REACH_EPSILON);
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
