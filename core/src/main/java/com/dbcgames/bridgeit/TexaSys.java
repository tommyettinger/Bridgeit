package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

@All({MobCmp.class, TexaCmp.class})
public class TexaSys extends IteratingSystem {
    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<TexaCmp> texaMap;

    public TexaSys() {
        super(Aspect.all(MobCmp.class, TexaCmp.class));
    }

    @Override
    protected void process(int e) {
        MobCmp mob = mobMap.get(e);
        TexaCmp texa = texaMap.get(e);

        boolean mobMoving = mob.vx != 0 || mob.vy != 0;
        if (texa.moving != mobMoving) {
            texa.moving = mobMoving;
            texa.stateTime = 0;
        } else {
            texa.stateTime += world.getDelta();
        }

        Animation<TextureRegion> anim = texa.moving ? texa.movingAnim : texa.idleAnim;
        if (anim != null) {
            texa.currentTex = anim.getKeyFrame(texa.stateTime, texa.moving);
        }
    }

}
