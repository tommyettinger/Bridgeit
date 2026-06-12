package com.dbcgames.bridgeit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Input;

public class LevelCompletedScreen extends GameScreen {
    private static final float TITLE_Y = Game.WORLD_HEIGHT * 0.75f;
    private static final float LINE_GAP = 14f;
    private static final float ICON_GAP = 10f;
    private static final float BUTTON_WIDTH = 240f;
    private static final float BUTTON_HEIGHT = 72f;
    private static final float BUTTON_BOTTOM_PADDING = 120f;
    private static final float ROUND_END_VOLUME = 0.2f;

    private final Runnable nextAction;
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3 touchPos = new Vector3();
    private final Color buttonColor = new Color(0.16f, 0.16f, 0.22f, 1f);
    private final Color buttonHoverColor = new Color(0.25f, 0.25f, 0.36f, 1f);

    private int totalTreasure;
    private int totalScrap;

    private BitmapFont titleFont;
    private BitmapFont lineFont;
    private TextureRegion treasureIcon;
    private TextureRegion scrapIcon;
    private SpriteBatch batch;
    private Texture buttonTexture;
    private Rectangle nextBounds;
    private FitViewport viewport;

    public LevelCompletedScreen(Runnable nextAction) {
        this.nextAction = nextAction;
    }

    @Override
    public void init() {
        titleFont = Game.assman.get("ui/font-subtitle.fnt", BitmapFont.class);
        lineFont = Game.assman.get("ui/font-window.fnt", BitmapFont.class);
        treasureIcon = Game.atlas.findRegion("treasure");
        scrapIcon = Game.atlas.findRegion("checker");
        batch = new SpriteBatch();
        buttonTexture = buildPixelTexture();

        viewport = new FitViewport(Game.WORLD_WIDTH, Game.WORLD_HEIGHT);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        float nextX = (Game.WORLD_WIDTH - BUTTON_WIDTH) * 0.5f;
        nextBounds = new Rectangle(nextX, BUTTON_BOTTOM_PADDING, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        if (batch == null) {
            return;
        }
        batch.setProjectionMatrix(viewport.getCamera().combined);

        handleInput();

        batch.begin();
        drawTitle();
        drawTotals();
        drawNextButton();
        batch.end();
    }

    public void setTotals(int totalTreasure, int totalScrap) {
        this.totalTreasure = Math.max(0, totalTreasure);
        this.totalScrap = Math.max(0, totalScrap);
    }

    public void playRoundEndSound() {
        if (Game.roundEndSound != null) {
            Game.roundEndSound.play(ROUND_END_VOLUME);
        }
    }

    private void handleInput() {
        if (nextAction == null) {
            return;
        }
        boolean keyPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if (keyPressed) {
            nextAction.run();
            return;
        }
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.unproject(touchPos);

            if (nextBounds != null && nextBounds.contains(touchPos.x, touchPos.y)) {
                nextAction.run();
            }
        }
    }

    private void drawTitle() {
        layout.setText(titleFont, "Level Done");
        float x = (Game.WORLD_WIDTH - layout.width) * 0.5f;
        titleFont.draw(batch, layout, x, TITLE_Y);
    }

    private void drawTotals() {
        float y = TITLE_Y - layout.height - LINE_GAP * 2f;
        y = drawTotalLine("Total", treasureIcon, totalTreasure, y, Color.YELLOW);
        drawTotalLine("Total", scrapIcon, totalScrap, y, Color.GREEN);
    }

    private float drawTotalLine(String label, TextureRegion icon, int count, float y, Color color) {
        String text = label + " ";
        layout.setText(lineFont, text);
        float textWidth = layout.width;
        float iconWidth = icon != null ? icon.getRegionWidth() : 0f;
        String countText = " " + Integer.toString(count);
        GlyphLayout countLayout = new GlyphLayout(lineFont, countText);
        float totalWidth = textWidth + ICON_GAP + iconWidth + ICON_GAP + countLayout.width;
        float x = (Game.WORLD_WIDTH - totalWidth) * 0.5f;

        lineFont.draw(batch, layout, x, y);

        float iconX = x + textWidth + ICON_GAP;
        float iconY = y - (lineFont.getCapHeight() * 0.5f) - (icon != null ? icon.getRegionHeight() * 0.5f : 0f);
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
        lineFont.draw(batch, countLayout, countX, y);

        return y - countLayout.height - LINE_GAP;
    }

    private void drawNextButton() {
        boolean hovering = isPointerOverButton();

        batch.setColor(hovering ? buttonHoverColor : buttonColor);
        batch.draw(buttonTexture, nextBounds.x, nextBounds.y, nextBounds.width, nextBounds.height);
        batch.setColor(Color.WHITE);

        layout.setText(lineFont, "Next");
        float textX = nextBounds.x + (nextBounds.width - layout.width) * 0.5f;
        float textY = nextBounds.y + (nextBounds.height + layout.height) * 0.5f;
        lineFont.draw(batch, layout, textX, textY);
    }

    private boolean isPointerOverButton() {
        if (!Gdx.input.isTouched()) {
            return false;
        }
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touchPos);
        return nextBounds != null && nextBounds.contains(touchPos.x, touchPos.y);
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
}
