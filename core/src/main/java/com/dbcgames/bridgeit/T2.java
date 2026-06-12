package com.dbcgames.bridgeit;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;

public class T2 extends GameScreen {
    private TextureRegion otto;
    private TiledMap debugMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;

    private boolean debugprinted = false;

    @Override
    public void init() {
        otto = Game.atlas.findRegion("smiling-face");
        debugMap = Game.assman.get("maps/debug.tmx", TiledMap.class);
        mapRenderer = new OrthogonalTiledMapRenderer(debugMap);
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // Clear the screen with a distinct color so we can see it working.
        Gdx.gl.glClearColor(0.15f, 0.45f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Game.viewport.apply();
        batch.setProjectionMatrix(Game.viewport.getCamera().combined);

        OrthographicCamera camera = (OrthographicCamera) Game.viewport.getCamera();
        // Snap to whole pixels to reduce tile seams when the viewport scales unevenly
        camera.position.x = MathUtils.round(camera.position.x);
        camera.position.y = MathUtils.round(camera.position.y);
        camera.update(true);

        mapRenderer.setView(camera);
        mapRenderer.render();

        float x = Game.WORLD_WIDTH * 0.5F;
        float y = Game.WORLD_HEIGHT * 0.5F;
        float width = otto.getRegionWidth();
        float height = otto.getRegionHeight();
        float drawX = x - width* 0.5f;
        float drawY = y - height * 0.5f;

        if(!debugprinted) {
            // print all these to the debug log only once
            Gdx.app.debug("T2", "WORLD_WIDTH: " + Game.WORLD_WIDTH);
            Gdx.app.debug("T2", "WORLD_HEIGHT: " + Game.WORLD_HEIGHT);
            Gdx.app.debug("T2", "drawX: " + drawX);
            Gdx.app.debug("T2", "drawY: " + drawY);
            Gdx.app.debug("T2", "width: " + width);
            Gdx.app.debug("T2", "height: " + height);
            debugprinted = true;
        }

        if (Gdx.input.justTouched()) {
            Vector3 worldCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            Game.viewport.unproject(worldCoords);

            TiledMapTileLayer layer = (TiledMapTileLayer) debugMap.getLayers().get(0);
            float tileWidth = layer.getTileWidth();
            float tileHeight = layer.getTileHeight();
            int tileX = (int) (worldCoords.x / tileWidth);
            int tileY = (int) (worldCoords.y / tileHeight);

            if (tileX >= 0 && tileX < layer.getWidth() && tileY >= 0 && tileY < layer.getHeight()) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                int tileValue = 0;
                if (cell != null && cell.getTile() != null) {
                    tileValue = cell.getTile().getId();
                }
                Gdx.app.debug("T2", "Clicked tile (" + tileX + ", " + tileY + ") value=" + tileValue);
            } else {
                Gdx.app.debug("T2", "Clicked outside map at (" + tileX + ", " + tileY + ")");
            }
        }
        
        batch.begin();
        batch.draw(otto, drawX, drawY, width, height);
        batch.end();
    }

    @Override
    public void dispose() {
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
    }
}
