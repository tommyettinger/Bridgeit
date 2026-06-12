package com.dbcgames.bridgeit;

import com.artemis.EntityEdit;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gdcrux.PointF2;
import com.badlogic.gdx.graphics.Color;

// Smiley NPC utilities
public final class Smiley {
    public static Animation<TextureRegion> idleAnim;
    public static final float DEFAULT_RUNAWAY_TIME = 2f;

    private Smiley() { }

    public static void init() {
        TextureRegion frame = Game.atlas.findRegion("smiling-face");
        if (frame != null) {
            idleAnim = new Animation<TextureRegion>(0, frame);
        }
    }

    public static void createSmiley(float x, float y, float speedCoefficient) {
        EntityEdit edit = Game.world.createEntity().edit();

        MobCmp mob = edit.create(MobCmp.class);
        mob.type = MobType.SMILEY;
        mob.x = x;
        mob.y = y;
        mob.speed *= speedCoefficient;

        SmileyCmp smiley = edit.create(SmileyCmp.class);
        smiley.runawayTime = 0f;

        TexaCmp texa = edit.create(TexaCmp.class);
        texa.idleAnim = idleAnim;
        texa.movingAnim = idleAnim;  // unanimated NPC, reuse idle frame
        texa.color.set(Color.RED);

        BBoxCmp bbox = edit.create(BBoxCmp.class);
        TextureRegion baseFrame = idleAnim != null ? idleAnim.getKeyFrame(0) : null;
        if (baseFrame != null) {
            // Smiley is round; use the full frame size for its bounds
            bbox.setBySize(baseFrame.getRegionWidth(), baseFrame.getRegionHeight());
        }

        AiCmp ai = edit.create(AiCmp.class);
        ai.path = new Path<PointF2>(8);
    }
}
