package com.dbcgames.bridgeit;

import com.artemis.EntityEdit;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;

public final class Scrap {
    public static Animation<TextureRegion> idleAnim;

    private Scrap() { }

    public static void init() {
        TextureRegion frame = Game.atlas.findRegion("checker");
        if (frame != null) {
            idleAnim = new Animation<TextureRegion>(0, frame);
        }
    }

    public static void createScrap(float x, float y) {
        EntityEdit edit = Game.world.createEntity().edit();

        MobCmp mob = edit.create(MobCmp.class);
        mob.type = MobType.SCRAP;
        mob.x = x;
        mob.y = y;
        mob.speed = 0;  // scrap doesn't move

        TexaCmp texa = edit.create(TexaCmp.class);
        texa.idleAnim = idleAnim;
        texa.movingAnim = idleAnim;  // unanimated NPC, reuse idle frame
        texa.color.set(Color.GREEN);

        BBoxCmp bbox = edit.create(BBoxCmp.class);
        TextureRegion baseFrame = idleAnim != null ? idleAnim.getKeyFrame(0) : null;
        if (baseFrame != null) {
            bbox.setBySize(baseFrame.getRegionWidth(), baseFrame.getRegionHeight());
        }
    }

}
