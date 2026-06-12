package com.dbcgames.bridgeit;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;

// Debug helper: on click, log world coords, map coords, and isFloor result
public class DebugSys extends BaseSystem {
    private final TiledMap map;
    private final Vector3 tmpVec = new Vector3();

    public DebugSys(TiledMap map) {
        this.map = map;
    }

    @Override
    protected void processSystem() {
        if (!Gdx.input.justTouched() || map == null || map.getLayers().getCount() == 0) 
            return;

        // Screen -> world
        tmpVec.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        Game.viewport.unproject(tmpVec);
        float worldX = tmpVec.x;
        float worldY = tmpVec.y;

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        int tileX = (int) (worldX / layer.getTileWidth());
        int tileY = (int) (worldY / layer.getTileHeight());

        boolean floor = Map.isFloor(map, worldX, worldY);

        Gdx.app.debug("DebugSys", "World (" + worldX + ", " + worldY + ") -> Tile (" + tileX + ", " + tileY + "), isFloor=" + floor);
    }
}
