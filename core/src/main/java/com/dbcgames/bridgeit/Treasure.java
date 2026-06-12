package com.dbcgames.bridgeit;

import com.artemis.EntityEdit;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;

public final class Treasure {
    public static Animation<TextureRegion> idleAnim;

    private Treasure() { }

    public static void init() {
        TextureRegion frame = Game.atlas.findRegion("treasure");
        if (frame != null) {
            idleAnim = new Animation<TextureRegion>(0, frame);
        }
    }

    public static void createTreasure(float x, float y) {
        EntityEdit edit = Game.world.createEntity().edit();

        MobCmp mob = edit.create(MobCmp.class);
        mob.type = MobType.TREASURE;
        mob.x = x;
        mob.y = y;
        mob.speed = 0;  // treasure doesn't move

        TexaCmp texa = edit.create(TexaCmp.class);
        texa.idleAnim = idleAnim;
        texa.movingAnim = idleAnim;  // unanimated NPC, reuse idle frame
        texa.color.set(Color.YELLOW);

        BBoxCmp bbox = edit.create(BBoxCmp.class);
        TextureRegion baseFrame = idleAnim != null ? idleAnim.getKeyFrame(0) : null;
        if (baseFrame != null) {
            // Smiley is round; use the full frame size for its bounds
            bbox.setBySize(baseFrame.getRegionWidth(), baseFrame.getRegionHeight());
        }
    }

}
