package com.dbcgames.bridgeit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Bridgeit extends ApplicationAdapter {
    // next state requested by specific screens being run
    public static BridgeitState requestedState;

    private BridgeitState state;
    private GameScreen currentScreen;
    private boolean assetsLoaded = false;

    @Override
    public void create() {
        // Ensure debug-level logs are emitted
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        setupViewport();
        queueAssets();
    }

    @Override
    public void render() {
        // render current screen with delta
        float delta = Gdx.graphics.getDeltaTime();
        if (!assetsLoaded) {
            if (Game.assman.update()) {
                onAssetsLoaded();
            }
            return;
        }
        // check for state change requests
        if(state != requestedState) switchState();
        currentScreen.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        Game.viewport.update(width, height, true);
        if (Game.world != null) {
            TouchpadUiSys touchpadUiSys = Game.world.getSystem(TouchpadUiSys.class);
            if (touchpadUiSys != null) {
                touchpadUiSys.resize(width, height);
            }
            ScrapHudSys scrapHudSys = Game.world.getSystem(ScrapHudSys.class);
            if (scrapHudSys != null) {
                scrapHudSys.resize(width, height);
            }
        }
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

    private void setupViewport() {
        Game.viewport = new FillViewport(Game.WORLD_WIDTH, Game.WORLD_HEIGHT);
        Game.viewport.getCamera().position.set(Game.WORLD_WIDTH * 0.5f, Game.WORLD_HEIGHT * 0.5f, 0f);
        Game.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    // Queue assets for asynchronous loading
    private void queueAssets() {
        InternalFileHandleResolver resolver = new InternalFileHandleResolver();
        Game.assman = new AssetManager(resolver);
        Game.assman.setLoader(TiledMap.class, new TmxMapLoader(resolver));
        Game.assman.load("bridgeit.atlas", TextureAtlas.class);
        Game.assman.load("ui/font-subtitle.fnt", BitmapFont.class);
        Game.assman.load("ui/font-window.fnt", BitmapFont.class);
        Game.assman.load("plimplom.ogg", Music.class);
        Game.assman.load("explosion.wav", Sound.class);
        Game.assman.load("pickup.wav", Sound.class);
        Game.assman.load("fix.wav", Sound.class);
        Game.assman.load("round_end.wav", Sound.class);
        Map.loadMaps(Game.assman);
    }

    // Called once when asset loading completes
    private void onAssetsLoaded() {
        assetsLoaded = true;
        Game.atlas = Game.assman.get("bridgeit.atlas", TextureAtlas.class);

        // Force nearest neighbor on all textures
        for (Texture t : Game.atlas.getTextures())
            t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        // initialize static classes
        Player.init();
        Smiley.init();
        Treasure.init();
        Scrap.init();
        Broken.init();

        // set music
        Game.backgroundMusic = Game.assman.get("plimplom.ogg", Music.class);
        Game.backgroundMusic.setLooping(true);
        Game.backgroundMusic.setVolume(0.1f);
        Game.explosionSound = Game.assman.get("explosion.wav", Sound.class);
        Game.pickupSound = Game.assman.get("pickup.wav", Sound.class);
        Game.fixSound = Game.assman.get("fix.wav", Sound.class);
        Game.roundEndSound = Game.assman.get("round_end.wav", Sound.class);

        // Switch to the initial screen
        requestedState = BridgeitState.MAIN_MENU;
        //requestedState = BridgeitState.ACCEPT_DEATH;
        switchState();
        //        switchState(BridgeitState.ARENA);
    }

    private void switchState() {
        if(requestedState == state) return;
        // TODO move this somewhere else
        if (currentScreen instanceof Arena && state != BridgeitState.ARENA) {
            stopBackgroundMusic();
        }
        state = requestedState;
        switch (state) {
        case MAIN_MENU:
            currentScreen = new MainMenuScreen();
            break;
        case ARENA:
            currentScreen = new Arena();
            break;
        case ACCEPT_DEATH:
            currentScreen = new AcceptDeathScreen();
            break;
        case T1:
            currentScreen = new T1();
            break;
        case T2:
            currentScreen = new T2();
            break;
        default:
            throw new IllegalStateException("Arena.switchState: Unhandled state: " + state);
        }

        currentScreen.init();
    }

    private void stopBackgroundMusic() {
        if (Game.backgroundMusic != null && Game.backgroundMusic.isPlaying()) {
            Game.backgroundMusic.stop();
        }
    }

}
