package com.dbcgames.bridgeit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Input;

public class AcceptDeathScreen extends GameScreen {
    private static final float GRAVESTONE_SIZE = 128f;
    private static final float TOP_PADDING = 64f;
    private static final float TITLE_GAP = 24f;
    private static final float TOTALS_TOP_GAP = 96f;
    private static final float LINE_GAP = 14f;
    private static final float ICON_GAP = 10f;
    private static final float BUTTON_WIDTH = 240f;
    private static final float BUTTON_HEIGHT = 72f;
    private static final float BUTTON_BOTTOM_PADDING = 96f;

    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 touchPos = new Vector3();
    private final Color buttonColor = new Color(0.16f, 0.16f, 0.22f, 1f);
    private final Color buttonHoverColor = new Color(0.25f, 0.25f, 0.36f, 1f);
    private SpriteBatch batch;
    private TextureRegion gravestone;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private TextureRegion treasureIcon;
    private TextureRegion scrapIcon;
    private int totalTreasure;
    private int totalScrap;
    private Texture buttonTexture;
    private Rectangle restartBounds;
    private FitViewport viewport;

    @Override
    public void init() {
        stopMusic();
        playExplosion();
        gravestone = Game.atlas.findRegion("gravestone");
        titleFont = Game.assman.get("ui/font-subtitle.fnt", BitmapFont.class);
        buttonFont = Game.assman.get("ui/font-window.fnt", BitmapFont.class);
        treasureIcon = Game.atlas.findRegion("treasure");
        scrapIcon = Game.atlas.findRegion("checker");
        totalTreasure = RunStats.getTotalTreasure();
        totalScrap = RunStats.getTotalScrap();

        viewport = new FitViewport(Game.WORLD_WIDTH, Game.WORLD_HEIGHT);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        buttonTexture = buildPixelTexture();

        float restartX = (Game.WORLD_WIDTH - BUTTON_WIDTH) * 0.5f;
        restartBounds = new Rectangle(restartX, BUTTON_BOTTOM_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        handleInput();

        batch.begin();
        drawGravestone();
        float titleY = drawTitle();
        drawTotals(titleY);
        drawRestartButton();
        batch.end();
    }

    private void handleInput() {
        boolean keyPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if (keyPressed) {
            Bridgeit.requestedState = BridgeitState.ARENA;
            return;
        }
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.unproject(touchPos);

            if (restartBounds.contains(touchPos.x, touchPos.y)) {
                Bridgeit.requestedState = BridgeitState.ARENA;
            }
        }
    }

    private void drawGravestone() {
        float gravestoneX = (Game.WORLD_WIDTH - GRAVESTONE_SIZE) * 0.5f;
        float gravestoneY = Game.WORLD_HEIGHT - GRAVESTONE_SIZE - TOP_PADDING;
        batch.draw(gravestone, gravestoneX, gravestoneY, GRAVESTONE_SIZE, GRAVESTONE_SIZE);
    }

    private float drawTitle() {
        float gravestoneY = Game.WORLD_HEIGHT - GRAVESTONE_SIZE - TOP_PADDING;
        layout.setText(titleFont, "GAME OVER");
        float titleX = (Game.WORLD_WIDTH - layout.width) * 0.5f;
        float titleY = gravestoneY - TITLE_GAP - layout.height;
        titleFont.draw(batch, layout, titleX, titleY);
        return titleY;
    }

    private void drawTotals(float titleY) {
        float y = titleY - TOTALS_TOP_GAP;
        y = drawTotalLine("Total", treasureIcon, totalTreasure, y, Color.YELLOW);
        drawTotalLine("Total", scrapIcon, totalScrap, y, Color.GREEN);
    }

    private float drawTotalLine(String label, TextureRegion icon, int count, float y, Color color) {
        String text = label + " ";
        layout.setText(buttonFont, text);
        float textWidth = layout.width;
        float iconWidth = icon != null ? icon.getRegionWidth() : 0f;
        String countText = " " + Integer.toString(Math.max(0, count));
        GlyphLayout countLayout = new GlyphLayout(buttonFont, countText);
        float totalWidth = textWidth + ICON_GAP + iconWidth + ICON_GAP + countLayout.width;
        float x = (Game.WORLD_WIDTH - totalWidth) * 0.5f;

        buttonFont.draw(batch, layout, x, y);

        float iconX = x + textWidth + ICON_GAP;
        float iconY = y - (buttonFont.getCapHeight() * 0.5f) - (icon != null ? icon.getRegionHeight() * 0.5f : 0f);
        if (icon != null) {
            if (color != null) {
                batch.setColor(color);
            }
            batch.draw(icon, iconX, iconY);
            if (color != null) {
                batch.setColor(Color.WHITE);
            }
        }

        float countX = iconX + iconWidth + ICON_GAP;
        buttonFont.draw(batch, countLayout, countX, y);

        return y - countLayout.height - LINE_GAP;
    }

    private void drawRestartButton() {
        boolean hovering = isPointerOverButton();

        batch.setColor(hovering ? buttonHoverColor : buttonColor);
        batch.draw(buttonTexture, restartBounds.x, restartBounds.y, restartBounds.width, restartBounds.height);
        batch.setColor(Color.WHITE);

        layout.setText(buttonFont, "RESTART");
        float textX = restartBounds.x + (restartBounds.width - layout.width) * 0.5f;
        float textY = restartBounds.y + (restartBounds.height + layout.height) * 0.5f;
        buttonFont.draw(batch, layout, textX, textY);
    }

    private boolean isPointerOverButton() {
        if (!Gdx.input.isTouched()) {
            return false;
        }
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touchPos);
        return restartBounds.contains(touchPos.x, touchPos.y);
    }

    private Texture buildPixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (buttonTexture != null) {
            buttonTexture.dispose();
        }
    }

    private void stopMusic() {
        if (Game.backgroundMusic != null && Game.backgroundMusic.isPlaying()) {
            Game.backgroundMusic.stop();
        }
    }

    private void playExplosion() {
        if (Game.explosionSound != null) {
            Game.explosionSound.play();
        }
    }
}
