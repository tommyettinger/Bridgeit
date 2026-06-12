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

public class T1 extends GameScreen {
    private static final float GRAVESTONE_SIZE = 128f;
    private static final float TOP_PADDING = 128f;
    private static final float TITLE_GAP = 32f;
    private static final float BUTTON_WIDTH = 240f;
    private static final float BUTTON_HEIGHT = 72f;
    private static final float BUTTON_BOTTOM_PADDING = 128f;

    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 touchPos = new Vector3();
    private final Color buttonColor = new Color(0.16f, 0.16f, 0.22f, 1f);
    private final Color buttonHoverColor = new Color(0.25f, 0.25f, 0.36f, 1f);
    private SpriteBatch batch;
    private TextureRegion gravestone;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private Texture buttonTexture;
    private Rectangle restartBounds;

    @Override
    public void init() {
        gravestone = Game.atlas.findRegion("gravestone");
        titleFont = Game.assman.get("ui/font-subtitle.fnt", BitmapFont.class);
        buttonFont = Game.assman.get("ui/font-window.fnt", BitmapFont.class);

        batch = new SpriteBatch();
        buttonTexture = buildPixelTexture();

        float restartX = (Game.WORLD_WIDTH - BUTTON_WIDTH) * 0.5f;
        restartBounds = new Rectangle(restartX, BUTTON_BOTTOM_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Game.viewport.apply();
        batch.setProjectionMatrix(Game.viewport.getCamera().combined);

        handleInput();

        batch.begin();
        drawGravestone();
        drawTitle();
        drawRestartButton();
        batch.end();
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        Game.viewport.unproject(touchPos);

        if (restartBounds.contains(touchPos.x, touchPos.y)) {
            Gdx.app.log("T1", "Restart clicked");
        }
    }

    private void drawGravestone() {
        float gravestoneX = (Game.WORLD_WIDTH - GRAVESTONE_SIZE) * 0.5f;
        float gravestoneY = Game.WORLD_HEIGHT - GRAVESTONE_SIZE - TOP_PADDING;
        batch.draw(gravestone, gravestoneX, gravestoneY, GRAVESTONE_SIZE, GRAVESTONE_SIZE);
    }

    private void drawTitle() {
        float gravestoneY = Game.WORLD_HEIGHT - GRAVESTONE_SIZE - TOP_PADDING;
        layout.setText(titleFont, "GAME OVER");
        float titleX = (Game.WORLD_WIDTH - layout.width) * 0.5f;
        float titleY = gravestoneY - TITLE_GAP - layout.height;
        titleFont.draw(batch, layout, titleX, titleY);
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
        Game.viewport.unproject(touchPos);
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
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (buttonTexture != null) {
            buttonTexture.dispose();
        }
    }
}
