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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenuScreen extends GameScreen {
    private static final float TOP_PADDING = 128f;
    private static final float TITLE_GAP = 32f;
    private static final float OBJECTIVE_GAP = 12f;
    private static final float ICON_GAP = 10f;
    private static final float BUTTON_WIDTH = 240f;
    private static final float BUTTON_HEIGHT = 72f;
    private static final float BUTTON_BOTTOM_PADDING = 160f;

    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 touchPos = new Vector3();
    private final Color buttonColor = new Color(0.16f, 0.16f, 0.22f, 1f);
    private final Color buttonHoverColor = new Color(0.25f, 0.25f, 0.36f, 1f);
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private Texture buttonTexture;
    private TextureRegion scrapIcon;
    private TextureRegion bridgeIcon;
    private TextureRegion treasureIcon;
    private Rectangle startBounds;
    private SpriteBatch batch;
    private FitViewport viewport;

    @Override
    public void init() {
        titleFont = Game.assman.get("ui/font-subtitle.fnt", BitmapFont.class);
        buttonFont = Game.assman.get("ui/font-window.fnt", BitmapFont.class);
        scrapIcon = Game.atlas.findRegion("checker");
        bridgeIcon = Game.atlas.findRegion("dotted-diagonal-cross");
        treasureIcon = Game.atlas.findRegion("treasure");

        viewport = new FitViewport(Game.WORLD_WIDTH, Game.WORLD_HEIGHT);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        buttonTexture = buildPixelTexture();

        float startX = (Game.WORLD_WIDTH - BUTTON_WIDTH) * 0.5f;
        startBounds = new Rectangle(startX, BUTTON_BOTTOM_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        handleInput();

        batch.begin();
        drawTitle();
        drawStartButton();
        batch.end();
    }

    private void handleInput() {
        boolean keyPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if (keyPressed) {
            Bridgeit.requestedState = BridgeitState.ARENA;
            Gdx.app.log("MainMenuScreen", "Start clicked via keyboard");
            return;
        }
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.unproject(touchPos);

            if (startBounds.contains(touchPos.x, touchPos.y)) {
                Bridgeit.requestedState = BridgeitState.ARENA;
                Gdx.app.log("MainMenuScreen", "Start clicked");
            }
        }
    }

    private void drawTitle() {
        layout.setText(titleFont, "Bridge-It!");
        float titleX = (Game.WORLD_WIDTH - layout.width) * 0.5f;
        float titleY = (Game.WORLD_HEIGHT - layout.height) * 0.8f;
        titleFont.draw(batch, layout, titleX, titleY);

        titleY -= layout.height + TITLE_GAP;
        titleY = drawObjectiveLine("Find scrap ", scrapIcon, Color.GREEN, titleY);
        titleY = drawObjectiveLine("Fix bridge ", bridgeIcon, Color.ORANGE, titleY);
        drawObjectiveLine("Get treasure ", treasureIcon, Color.YELLOW, titleY);
    }

    private void drawStartButton() {
        boolean hovering = isPointerOverButton();

        batch.setColor(hovering ? buttonHoverColor : buttonColor);
        batch.draw(buttonTexture, startBounds.x, startBounds.y, startBounds.width, startBounds.height);
        batch.setColor(Color.WHITE);

        layout.setText(buttonFont, "START");
        float textX = startBounds.x + (startBounds.width - layout.width) * 0.5f;
        float textY = startBounds.y + (startBounds.height + layout.height) * 0.5f;
        buttonFont.draw(batch, layout, textX, textY);
    }

    private boolean isPointerOverButton() {
        if (!Gdx.input.isTouched()) {
            return false;
        }
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touchPos);
        return startBounds.contains(touchPos.x, touchPos.y);
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

    private float drawObjectiveLine(String text, TextureRegion icon, Color iconColor, float y) {
        if (icon == null) {
            layout.setText(buttonFont, text);
            float x = (Game.WORLD_WIDTH - layout.width) * 0.5f;
            buttonFont.draw(batch, layout, x, y);
            return y - layout.height - OBJECTIVE_GAP;
        }

        layout.setText(buttonFont, text);
        float textWidth = layout.width;
        float iconWidth = icon.getRegionWidth();
        float totalWidth = textWidth + ICON_GAP + iconWidth;
        float x = (Game.WORLD_WIDTH - totalWidth) * 0.5f;

        buttonFont.draw(batch, layout, x, y);

        float iconX = x + textWidth + ICON_GAP;
        float iconY = y - (buttonFont.getCapHeight() * 0.5f) - (icon.getRegionHeight() * 0.5f);
        batch.setColor(iconColor);
        batch.draw(icon, iconX, iconY);
        batch.setColor(Color.WHITE);

        return y - layout.height - OBJECTIVE_GAP;
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
}
