package com.dbcgames.bridgeit;

import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.artemis.ComponentMapper;
import com.artemis.Aspect;
import com.artemis.annotations.Wire;

@All({PlayerCmp.class, MobCmp.class})
@Wire
public class PlayerControlSys extends IteratingSystem {

    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<PlayerCmp> playerMap;
    private TouchpadUiSys touchpadUiSys;

    public PlayerControlSys() {
        super(Aspect.all(PlayerCmp.class, MobCmp.class));
    }
    
    @Override
    protected void process(int e) {
        float dx = 0f;
        float dy = 0f;
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        if(left ^ right) dx = left ? -1 : 1;
        if(up ^ down) dy = up ? 1 : -1;
        if (touchpadUiSys != null) {
            dx += touchpadUiSys.getKnobPercentX();
            dy += touchpadUiSys.getKnobPercentY();
        }

        // if(dx != 0 || dy != 0)
        //     Gdx.app.log("PlayerInputSys", "Entity " + e + " moving dx: " + dx + " dy: " + dy);

        MobCmp mob = mobMap.get(e);
        boolean moving = dx != 0f || dy != 0f;
        if (moving) {
            float invLen = 1f / (float)Math.sqrt(dx * dx + dy * dy);
            mob.vx = dx * invLen * mob.speed;
            mob.vy = dy * invLen * mob.speed;
            if (dx != 0f) {
                mob.flip = dx < 0f;
            }
        } else {
            mob.vx = 0f;
            mob.vy = 0f;
        }
    }

}
