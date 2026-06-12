package com.dbcgames.bridgeit;

import com.artemis.BaseSystem;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// Renders the player's scrap count in screen space.
public class ScrapHudSys extends BaseSystem {
    private static final float MARGIN = 12f;
    private static final float PADDING = 8f;
    private static final float GAP = 10f;
    private static final Color BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.5f);

    private final ScreenViewport viewport;
    private final SpriteBatch batch;
    private final GlyphLayout layout;
    private final Texture backgroundPixel;

    private TextureRegion scrapIcon;
    private BitmapFont font;

    private ComponentMapper<PlayerCmp> playerMap;

    public ScrapHudSys() {
        viewport = new ScreenViewport();
        batch = new SpriteBatch();
        layout = new GlyphLayout();
        backgroundPixel = buildPixelTexture();

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    protected void initialize() {
        scrapIcon = Game.atlas.findRegion("checker");
        font = Game.assman.get("ui/font-window.fnt", BitmapFont.class);
    }

    @Override
    protected void processSystem() {
        int playerId = findPlayerId();
        if (playerId == -1) {
            return;
        }

        PlayerCmp player = playerMap.get(playerId);
        int clampedScrap = Math.min(player.scrap, 999);
        String scrapText = formatScrap(clampedScrap);

        layout.setText(font, scrapText);

        float iconWidth = scrapIcon != null ? scrapIcon.getRegionWidth() : 0f;
        float iconHeight = scrapIcon != null ? scrapIcon.getRegionHeight() : 0f;
        float contentHeight = Math.max(iconHeight, layout.height);
        float boxWidth = PADDING * 2f + iconWidth + GAP + layout.width;
        float boxHeight = PADDING * 2f + contentHeight;
        float boxX = MARGIN;
        float boxY = viewport.getWorldHeight() - MARGIN - boxHeight;

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        batch.setColor(BACKGROUND_COLOR);
        batch.draw(backgroundPixel, boxX, boxY, boxWidth, boxHeight);

        batch.setColor(Color.GREEN);
        float iconX = boxX + PADDING;
        float iconY = boxY + (boxHeight - iconHeight) * 0.5f;
        if (scrapIcon != null) {
            batch.draw(scrapIcon, iconX, iconY);
        }

        batch.setColor(Color.WHITE);
        float textX = iconX + iconWidth + GAP;
        float textY = boxY + (boxHeight + layout.height) * 0.5f;
        font.draw(batch, layout, textX, textY);

        batch.end();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    protected void dispose() {
        batch.dispose();
        backgroundPixel.dispose();
    }

    private String formatScrap(int scrap) {
        int clamped = Math.max(0, Math.min(scrap, 999));
        if (clamped < 10) {
            return "00" + clamped;
        }
        if (clamped < 100) {
            return "0" + clamped;
        }
        return Integer.toString(clamped);
    }

    private int findPlayerId() {
        IntBag players = world.getAspectSubscriptionManager()
            .get(Aspect.all(PlayerCmp.class))
            .getEntities();
        return players.size() > 0 ? players.get(0) : -1;
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
}
