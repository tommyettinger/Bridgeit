package com.dbcgames.bridgeit;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.artemis.World;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FillViewport;

// Static class that can't be instantiated, to hold global members
public final class Game {
    // Private constructor to prevent instantiation
    private Game() { }

    // Constants
    public static final float WORLD_WIDTH = 640f;
    public static final float WORLD_HEIGHT = 640f;
    public static final float DEFAULT_MOB_SPEED = 240f;
    
    // Global members
    public static FillViewport viewport;
    public static AssetManager assman;
    public static TextureAtlas atlas;
    public static World world;
    public static Music backgroundMusic;
    public static Sound explosionSound;
    public static Sound pickupSound;
    public static Sound fixSound;
    public static Sound roundEndSound;

}
