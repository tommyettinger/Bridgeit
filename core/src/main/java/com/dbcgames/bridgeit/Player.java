package com.dbcgames.bridgeit;

import com.artemis.EntityEdit;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

// Player related functions
public final class Player {
    // tuneables
    private static final float ANIM_DURATION = 0.12f;
    
    public static Animation<TextureRegion> movingAnim;
    public static Animation<TextureRegion> idleAnim;

    public static void init() {
        Array<TextureAtlas.AtlasRegion> playerFrames = Game.atlas.findRegions("human_run");
        movingAnim = new Animation<TextureRegion>(ANIM_DURATION, playerFrames, Animation.PlayMode.LOOP);
        idleAnim = new Animation<TextureRegion>(0, movingAnim.getKeyFrame(movingAnim.getFrameDuration(), false));
    }

    public static void createPlayer(float x, float y) {
        EntityEdit edit = Game.world.createEntity().edit();
        edit.create(PlayerCmp.class);
        MobCmp mob = edit.create(MobCmp.class);
        mob.type = MobType.HUMAN;
        mob.x = x;
        mob.y = y;
        TexaCmp texa = edit.create(TexaCmp.class);
        texa.movingAnim = movingAnim;
        texa.idleAnim = idleAnim;
        BBoxCmp bbox = edit.create(BBoxCmp.class);
        TextureRegion baseFrame = texa.movingAnim != null ? texa.movingAnim.getKeyFrame(0) : null;
        if (baseFrame != null) {
            // Player is narrow so tweak the width a bit here
            bbox.setBySize(baseFrame.getRegionWidth() * 0.7f, baseFrame.getRegionHeight());
            // manual tuning to cover feet
            bbox.bottomLeftY *= 1.2f;
            bbox.bottomRightY *= 1.2f;
        }
    }

}
