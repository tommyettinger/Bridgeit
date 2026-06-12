package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

@All({MobCmp.class, BBoxCmp.class})
public class DebugBBRenderSys extends IteratingSystem {
    private final ShapeRenderer shapeRenderer;
    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<BBoxCmp> bboxMap;

    public DebugBBRenderSys() {
        super(Aspect.all(MobCmp.class, BBoxCmp.class));
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    protected void begin() {
        OrthographicCamera camera = (OrthographicCamera) Game.viewport.getCamera();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
    }

    @Override
    protected void process(int e) {
        MobCmp mob = mobMap.get(e);
        BBoxCmp box = bboxMap.get(e);

        float cx = mob.x;
        float cy = mob.y;

        float tlx = cx + box.topLeftX;
        float tly = cy + box.topLeftY;
        float trx = cx + box.topRightX;
        float tryy = cy + box.topRightY;
        float blx = cx + box.bottomLeftX;
        float bly = cy + box.bottomLeftY;
        float brx = cx + box.bottomRightX;
        float bry = cy + box.bottomRightY;

        shapeRenderer.line(tlx, tly, trx, tryy);
        shapeRenderer.line(trx, tryy, brx, bry);
        shapeRenderer.line(brx, bry, blx, bly);
        shapeRenderer.line(blx, bly, tlx, tly);
    }

    @Override
    protected void end() {
        shapeRenderer.end();
    }
}
