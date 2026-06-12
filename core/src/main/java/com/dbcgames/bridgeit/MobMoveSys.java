package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.maps.tiled.TiledMap;

@All({MobCmp.class, BBoxCmp.class})
public class MobMoveSys extends IteratingSystem {

    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<BBoxCmp> bboxMap;
    private ComponentMapper<PlayerCmp> playerMap;
    private final TiledMap map;

    public MobMoveSys(TiledMap map) {
        super(Aspect.all(MobCmp.class, BBoxCmp.class));
        this.map = map;
    }

    @Override
    protected void process(int e) {
        MobCmp mob = mobMap.get(e);
        BBoxCmp bbox = bboxMap.get(e);
        float dt = world.getDelta();

        float targetX = mob.x + mob.vx * dt;
        float targetY = mob.y + mob.vy * dt;

        // Before stepping, see if either axis would collide; if so, redirect all speed to the open axis.
        boolean hitsX = Map.collidesAt(map, targetX, mob.y, bbox);
        boolean hitsY = Map.collidesAt(map, mob.x, targetY, bbox);
        hitsX |= collidesWithBroken(targetX, mob.y, bbox, e);
        hitsY |= collidesWithBroken(mob.x, targetY, bbox, e);
        if (hitsX || hitsY) {
            float speed = (float)Math.sqrt(mob.vx * mob.vx + mob.vy * mob.vy);
            if (hitsX && hitsY) {
                mob.vx = 0f;
                mob.vy = 0f;
                return;
            } else if (hitsX) {
                mob.vx = 0f;
                mob.vy = Math.signum(mob.vy) * speed;
            } else { // hitsY
                mob.vy = 0f;
                mob.vx = Math.signum(mob.vx) * speed;
            }
            targetX = mob.x + mob.vx * dt;
            targetY = mob.y + mob.vy * dt;
        }

        boolean canMoveDiag =
            Map.isFloor(map, targetX + bbox.topLeftX, targetY + bbox.topLeftY) &&
            Map.isFloor(map, targetX + bbox.topRightX, targetY + bbox.topRightY) &&
            Map.isFloor(map, targetX + bbox.bottomLeftX, targetY + bbox.bottomLeftY) &&
            Map.isFloor(map, targetX + bbox.bottomRightX, targetY + bbox.bottomRightY) &&
            !collidesWithBroken(targetX, targetY, bbox, e);

        if (canMoveDiag) {
            mob.x = targetX;
            mob.y = targetY;
            return;
        }

        // Try X-only move
        boolean canMoveX =
            Map.isFloor(map, targetX + bbox.topLeftX, mob.y + bbox.topLeftY) &&
            Map.isFloor(map, targetX + bbox.topRightX, mob.y + bbox.topRightY) &&
            Map.isFloor(map, targetX + bbox.bottomLeftX, mob.y + bbox.bottomLeftY) &&
            Map.isFloor(map, targetX + bbox.bottomRightX, mob.y + bbox.bottomRightY) &&
            !collidesWithBroken(targetX, mob.y, bbox, e);

        // Use the updated X position for Y collision testing if X movement is allowed (for smoother corner sliding)
        float baseXForY = canMoveX ? targetX : mob.x;

        // Try Y-only move
        boolean canMoveY =
            Map.isFloor(map, baseXForY + bbox.topLeftX, targetY + bbox.topLeftY) &&
            Map.isFloor(map, baseXForY + bbox.topRightX, targetY + bbox.topRightY) &&
            Map.isFloor(map, baseXForY + bbox.bottomLeftX, targetY + bbox.bottomLeftY) &&
            Map.isFloor(map, baseXForY + bbox.bottomRightX, targetY + bbox.bottomRightY) &&
            !collidesWithBroken(baseXForY, targetY, bbox, e);

        if (canMoveX) {
            mob.x = targetX;
        } else {
            mob.vx = 0;
        }

        if (canMoveY) {
            mob.y = targetY;
        } else {
            mob.vy = 0;
        }
    }

    private boolean collidesWithBroken(float targetX, float targetY, BBoxCmp bbox, int moverId) {
        // Allow the player to ignore broken tiles if they have scrap to fix them.
        boolean playerCanPass = playerMap != null && playerMap.has(moverId) && playerMap.get(moverId).scrap > 0;

        float minX = targetX + bbox.bottomLeftX;
        float maxX = targetX + bbox.bottomRightX;
        float minY = targetY + bbox.bottomLeftY;
        float maxY = targetY + bbox.topLeftY;

        // Iterate all entities with bounding boxes; limit to broken tiles.
        IntBag entities = world.getAspectSubscriptionManager()
            .get(Aspect.all(MobCmp.class, BBoxCmp.class))
            .getEntities();
        for (int i = 0, n = entities.size(); i < n; i++) {
            int other = entities.get(i);
            if (other == moverId) continue;

            MobCmp otherMob = mobMap.get(other);
            if (otherMob.type != MobType.BROKEN)
                continue;

            BBoxCmp otherBox = bboxMap.get(other);
            float oMinX = otherMob.x + otherBox.bottomLeftX;
            float oMaxX = otherMob.x + otherBox.bottomRightX;
            float oMinY = otherMob.y + otherBox.bottomLeftY;
            float oMaxY = otherMob.y + otherBox.topLeftY;

            boolean overlaps =
                minX <= oMaxX && maxX >= oMinX &&
                minY <= oMaxY && maxY >= oMinY;

            if (overlaps) {
                if (playerCanPass) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
