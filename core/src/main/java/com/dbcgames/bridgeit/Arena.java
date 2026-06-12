package com.dbcgames.bridgeit;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.github.tommyettinger.gdcrux.PointF2;

// Runs the main game
public class Arena extends GameScreen {
    private static final int BASE_MAP_SIZE = 15;
    private static final int MAP_SIZE_STEP = 5;
    private static final int MAX_MAP_SIZE = 50;
    private static final int BASE_SCRAP = 3;
    private static final int SCRAP_STEP = 3;
    private static final int MAX_SCRAP = 33;
    private static final int MAX_SMILEYS = 7;
    private static final int BASE_BROKEN = 3;
    private static final int BROKEN_STEP = 3;
    private static final int MAX_BROKEN = 33;

    private enum Mode {
        PLAYING,
        LEVEL_COMPLETE
    }

    private static class LevelSettings {
        final int mapWidth;
        final int mapHeight;
        final int scrapCount;
        final int smileyCount;

        LevelSettings(int mapWidth, int mapHeight, int scrapCount, int smileyCount) {
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            this.scrapCount = scrapCount;
            this.smileyCount = smileyCount;
        }
    }

    private int level = 0;
    private int totalScrap = 0;
    private int totalTreasure = 0;
    private boolean levelCompleteRequested = false;
    private boolean playerDeathRequested = false;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRend;
    private boolean generatedMap = false;
    private LevelCompletedScreen levelCompletedScreen;
    private Mode mode = Mode.PLAYING;
    private LevelSettings currentSettings;
    private final LevelProgressListener progressListener = new LevelEvents();
    private final PlayerDeathListener deathListener = new DeathEvents();

    @Override
    public void init() {
        totalScrap = 0;
        totalTreasure = 0;
        RunStats.resetTotals();
        syncRunStats();
        mode = Mode.PLAYING;
        startLevel();

        if (Game.backgroundMusic != null) {
            Game.backgroundMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        if (mode == Mode.LEVEL_COMPLETE) {
            renderLevelComplete(delta);
            return;
        }

        // Clear the screen with a distinct color so we can see it working.
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Game.viewport.apply();

        Game.world.setDelta(delta);
        Game.world.process();

        if (levelCompleteRequested) {
            finishLevel();
        }

        if (playerDeathRequested) {
            handlePlayerDeath();
        }
    }

    // Prepares the current map
    private void prepMap() {
        switch (level) {
        case -2:
        case -1:
            prepDebugMap();
            break;
        default:
            prepRegularMap();
            break;
        }
    }

    private void prepDebugMap() {
        TiledMapTileLayer layer = map != null && map.getLayers().getCount() > 0 ? (TiledMapTileLayer) map.getLayers().get(0) : null;
        if (layer == null) {
            Gdx.app.log("Arena::prepMap", "No map layer found; placing smiley at default position");
            Smiley.createSmiley(480f, 320f, smileySpeedForLevel(level));
            return;
        }

        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();
        int mapWidth = layer.getWidth();
        int mapHeight = layer.getHeight();
        
        PointF2 playerTile = Map.sampleRandomFloorTile(map);
        if (playerTile.x < 0f || playerTile.y < 0f) {
            playerTile = new PointF2(mapWidth * 0.5f, mapHeight * 0.5f);
        }
        float playerX = Map.tileToWorldX(playerTile.x, tileWidth);
        float playerY = Map.tileToWorldY(playerTile.y, tileHeight);
        Player.createPlayer(playerX, playerY);

        PointF2 farTile = Map.sampleFurthestFloorTile(map, playerTile.x, playerTile.y);
        if (farTile.x >= 0f && farTile.y >= 0f) {
            float placeX = Map.tileToWorldX(farTile.x, tileWidth);
            float placeY = Map.tileToWorldY(farTile.y, tileHeight);
            Smiley.createSmiley(placeX, placeY, smileySpeedForLevel(level));
        }

        farTile = Map.sampleFurthestFloorTile(map, playerTile.x, playerTile.y);
        if (farTile.x >= 0f && farTile.y >= 0f) {
            float placeX = Map.tileToWorldX(farTile.x, tileWidth);
            float placeY = Map.tileToWorldY(farTile.y, tileHeight);
            Treasure.createTreasure(placeX, placeY);
        }

        for(int i = 0; i<3; i++) {
            farTile = Map.sampleRandomFloorTile(map);
            if (farTile.x >= 0f && farTile.y >= 0f) {
                float placeX = Map.tileToWorldX(farTile.x, tileWidth);
                float placeY = Map.tileToWorldY(farTile.y, tileHeight);
                Scrap.createScrap(placeX, placeY);
            }
        }

        for(int i = 0; i<3; i++) {
            farTile = Map.sampleRandomFloorTile(map);
            if (farTile.x >= 0f && farTile.y >= 0f) {
                float placeX = Map.tileToWorldX(farTile.x, tileWidth);
                float placeY = Map.tileToWorldY(farTile.y, tileHeight);
                Broken.createBroken(placeX, placeY);
            }
        }

    }

    private void prepRegularMap() {
        TiledMapTileLayer layer = map != null && map.getLayers().getCount() > 0 ? (TiledMapTileLayer) map.getLayers().get(0) : null;
        if (layer == null) {
            Gdx.app.log("Arena::prepRegularMap", "No map layer found for level " + level);
            return;
        }

        float tileWidth = layer.getTileWidth();
        float tileHeight = layer.getTileHeight();
        int mapWidth = layer.getWidth();
        int mapHeight = layer.getHeight();

        PointF2 playerTile = Map.sampleRandomFloorTile(map);
        if (playerTile.x < 0f || playerTile.y < 0f) {
            playerTile = new PointF2(mapWidth * 0.5f, mapHeight * 0.5f);
        }
        float playerX = Map.tileToWorldX(playerTile.x, tileWidth);
        float playerY = Map.tileToWorldY(playerTile.y, tileHeight);
        Player.createPlayer(playerX, playerY);

        for (int i = 0; i < currentSettings.smileyCount; i++) {
            PointF2 smileyTile = Map.sampleFurthestFloorTile(map, playerTile.x, playerTile.y);
            if (smileyTile.x < 0f || smileyTile.y < 0f) {
                smileyTile = new PointF2(mapWidth * 0.5f, mapHeight * 0.5f);
            }
            float placeX = Map.tileToWorldX(smileyTile.x, tileWidth);
            float placeY = Map.tileToWorldY(smileyTile.y, tileHeight);
            Smiley.createSmiley(placeX, placeY, smileySpeedForLevel(level));
        }

        for(int i = 0; i < currentSettings.scrapCount; i++) {
            PointF2 scrapTile = Map.sampleRandomFloorTile(map);
            if (scrapTile.x >= 0f && scrapTile.y >= 0f) {
                float placeX = Map.tileToWorldX(scrapTile.x, tileWidth);
                float placeY = Map.tileToWorldY(scrapTile.y, tileHeight);
                Scrap.createScrap(placeX, placeY);
            }
        }

        Map.replaceSpecialTiles(map, brokenCountForLevel(level));
        SmileyControlSys smileyControl = Game.world != null ? Game.world.getSystem(SmileyControlSys.class) : null;
        if (smileyControl != null) {
            smileyControl.rebuildNavigation();
        }
        SmileySmileyCollSys smileyColl = Game.world != null ? Game.world.getSystem(SmileySmileyCollSys.class) : null;
        if (smileyColl != null) {
            smileyColl.rebuildNavigation();
        }

    //     PointF2 farTile = Map.sampleFurthestFloorTile(map, playerTile.x, playerTile.y);
    //     if (farTile.x >= 0f && farTile.y >= 0f) {
    //         float placeX = Map.tileToWorldX(farTile.x, tileWidth);
    //         float placeY = Map.tileToWorldY(farTile.y, tileHeight);
    //         Treasure.createTreasure(placeX, placeY);
    //     } else {
    //         Treasure.createTreasure(playerX, playerY);
    //     }


    //     for(int i = 0; i < BROKEN_PER_LEVEL; i++) {
    //         PointF2 brokenTile = Map.sampleRandomFloorTile(map);
    //         if (brokenTile.x >= 0f && brokenTile.y >= 0f) {
    //             float placeX = Map.tileToWorldX(brokenTile.x, tileWidth);
    //             float placeY = Map.tileToWorldY(brokenTile.y, tileHeight);
    //             Broken.createBroken(placeX, placeY);
    //         }
    //     }
    }

    // hacky level loader
    private void loadMap() {
        generatedMap = false;
        // Set the map and renderer
        switch (level) {
        case -1:
            //map = Game.assman.get("maps/debug.tmx", TiledMap.class);
            map = Game.assman.get("maps/bridge.tmx", TiledMap.class);
            break;
        case -2:
            map = Map.genMap(15, 15);
            generatedMap = true;
            break;
        default:
            int mapWidth = currentSettings != null ? currentSettings.mapWidth : BASE_MAP_SIZE;
            int mapHeight = currentSettings != null ? currentSettings.mapHeight : BASE_MAP_SIZE;
            map = Map.genMap(mapWidth, mapHeight);
            generatedMap = true;
            break;
        }
        if (map == null) {
            Gdx.app.log("Arena::init", "Failed to load map; falling back to debug.tmx");
            map = Game.assman.get("maps/debug.tmx", TiledMap.class);
        }

        if (mapRend != null) {
            mapRend.dispose();
        }
        mapRend = new OrthogonalTiledMapRenderer(map);
    }

    private LevelSettings computeSettingsForLevel(int levelIndex) {
        int mapSize = MathUtils.clamp(BASE_MAP_SIZE + levelIndex * MAP_SIZE_STEP, BASE_MAP_SIZE, MAX_MAP_SIZE);
        int scrapCount = MathUtils.clamp(BASE_SCRAP + levelIndex * SCRAP_STEP, BASE_SCRAP, MAX_SCRAP);
        int smileyCount = MathUtils.clamp((int)Math.ceil((double)levelIndex / 2), 0, MAX_SMILEYS);
        return new LevelSettings(mapSize, mapSize, scrapCount, smileyCount);
    }

    private float smileySpeedForLevel(int levelIndex) {
        float coefficient = 0.5f + 0.05f * (levelIndex - 1);
        return MathUtils.clamp(coefficient, 0.5f, 0.9f);
    }

    private int brokenCountForLevel(int levelIndex) {
        return MathUtils.clamp(BASE_BROKEN + levelIndex * BROKEN_STEP, BASE_BROKEN, MAX_BROKEN);
    }

    private void startLevel() {
        levelCompleteRequested = false;
        playerDeathRequested = false;
        currentSettings = level >= 0 ? computeSettingsForLevel(level) : null;

        cleanupWorld();
        loadMap();

        WorldConfiguration conf = new WorldConfigurationBuilder()
            .with(new PlayerControlSys())
            .with(new SmileyControlSys(map))
            .with(new MobMoveSys(map))
            .with(new SmileySmileyCollSys(map))
            .with(new PlayerBrokenCollSys())
            .with(new PlayerScrapCollSys(progressListener))
            .with(new PlayerTreasureCollSys(progressListener))
            .with(new PlayerSmileyCollSys(deathListener))
            .with(new TexaSys())
            .with(new MapRenderSys(mapRend))
            .with(new RenderSys())
            //            .with(new DebugBBRenderSys())
            //            .with(new DebugSys(map))
            .with(new TouchpadUiSys())
            .with(new ScrapHudSys())
            .build();
        Game.world = new World(conf);

        prepMap();
        mode = Mode.PLAYING;
    }

    private void cleanupWorld() {
        if (Game.world != null) {
            Game.world.dispose();
            Game.world = null;
        }
        if (mapRend != null) {
            mapRend.dispose();
            mapRend = null;
        }
        if (generatedMap && map != null) {
            map.dispose();
        }
        map = null;
        generatedMap = false;
    }

    private void finishLevel() {
        levelCompleteRequested = false;
        totalTreasure += 1;
        syncRunStats();
        cleanupWorld();
        ensureLevelCompleteScreen();
        levelCompletedScreen.setTotals(totalTreasure, totalScrap);
        levelCompletedScreen.playRoundEndSound();
        mode = Mode.LEVEL_COMPLETE;
    }

    private void ensureLevelCompleteScreen() {
        if (levelCompletedScreen == null) {
            levelCompletedScreen = new LevelCompletedScreen(this::startNextLevel);
            levelCompletedScreen.init();
        }
    }

    private void startNextLevel() {
        level += 1;
        startLevel();
    }

    private void renderLevelComplete(float delta) {
        if (levelCompletedScreen != null) {
            levelCompletedScreen.render(delta);
        }
    }

    private class LevelEvents implements LevelProgressListener {
        @Override
        public void onScrapCollected() {
            totalScrap += 1;
            syncRunStats();
        }

        @Override
        public void onTreasureCollected() {
            levelCompleteRequested = true;
        }
    }

    private void handlePlayerDeath() {
        playerDeathRequested = false;
        syncRunStats();
        cleanupWorld();
        Bridgeit.requestedState = BridgeitState.ACCEPT_DEATH;
    }

    private class DeathEvents implements PlayerDeathListener {
        @Override
        public void onPlayerKilled() {
            playerDeathRequested = true;
        }
    }

    private void syncRunStats() {
        RunStats.setTotals(totalTreasure, totalScrap);
    }
}
