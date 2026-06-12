package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;

@All({PlayerCmp.class, MobCmp.class})
public class MapRenderSys extends IteratingSystem {
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final OrthographicCamera camera;
    private ComponentMapper<MobCmp> mobMap;

    private boolean hasPlayer;

    public MapRenderSys(OrthogonalTiledMapRenderer mapRenderer) {
        super(Aspect.all(PlayerCmp.class, MobCmp.class));
        this.mapRenderer = mapRenderer;
        this.camera = (OrthographicCamera) Game.viewport.getCamera();
    }

    @Override
    protected void begin() {
        hasPlayer = false;
    }

    @Override
    protected void process(int e) {
        MobCmp mob = mobMap.get(e);
        camera.position.set(mob.x, mob.y, 0f);
        hasPlayer = true;
    }

    @Override
    protected void end() {
        if (!hasPlayer) 
            return;

        // Snap to whole pixels to reduce tile seams when the viewport scales unevenly
        camera.position.x = MathUtils.round(camera.position.x);
        camera.position.y = MathUtils.round(camera.position.y);
        camera.update(true);

        mapRenderer.setView(camera);
        Color previous = mapRenderer.getBatch().getColor();
        mapRenderer.getBatch().setColor(Color.GRAY);
        mapRenderer.render();
        mapRenderer.getBatch().setColor(previous);
    }
}
