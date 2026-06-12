package com.dbcgames.bridgeit;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// Draws and manages the on-screen touchpad for movement input.
public class TouchpadUiSys extends BaseSystem {
    private final Stage stage;
    private final Touchpad touchpad;
    private final Texture padBackgroundTexture;
    private final Texture padKnobTexture;
    private final Vector2 tmpVec = new Vector2();
    private int touchPointer = -1;

    public TouchpadUiSys() {
        stage = new Stage(new ScreenViewport());
        padBackgroundTexture = buildCircleTexture(110, new Color(0.15f, 0.15f, 0.15f, 0.45f));
        padKnobTexture = buildCircleTexture(55, new Color(0.1f, 0.1f, 0.5f, 0.9f));
        touchpad = buildTouchpad();
        touchpad.setSize(110f, 110f);
        touchpad.setVisible(false);
        touchpad.setPosition(16f, 16f);
        stage.addActor(touchpad);

        InputMultiplexer multiplexer = new InputMultiplexer(new TouchpadActivator(), stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private Touchpad buildTouchpad() {
        Drawable background = new TextureRegionDrawable(new TextureRegion(padBackgroundTexture));
        Drawable knob = new TextureRegionDrawable(new TextureRegion(padKnobTexture));
        Touchpad.TouchpadStyle style = new Touchpad.TouchpadStyle(background, knob);
        return new Touchpad(8f, style);
    }

    private Texture buildCircleTexture(int diameter, Color color) {
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private class TouchpadActivator extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (touchPointer != -1) {
                return false;
            }
            touchPointer = pointer;
            tmpVec.set(screenX, screenY);
            stage.screenToStageCoordinates(tmpVec);
            touchpad.setPosition(tmpVec.x - touchpad.getWidth() * 0.5f, tmpVec.y - touchpad.getHeight() * 0.5f);
            touchpad.setVisible(true);
            return false; // allow Stage to handle the drag on the knob
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (pointer != touchPointer) {
                return false;
            }
            touchPointer = -1;
            touchpad.setVisible(false);
            return false;
        }
    }

    public float getKnobPercentX() {
        if (touchPointer == -1 && !touchpad.isTouched()) {
            return 0f;
        }
        return touchpad.getKnobPercentX();
    }

    public float getKnobPercentY() {
        if (touchPointer == -1 && !touchpad.isTouched()) {
            return 0f;
        }
        return touchpad.getKnobPercentY();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    protected void processSystem() {
        float delta = world.getDelta();
        // Ensure UI uses the full screen viewport to avoid distortion from the world viewport.
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    protected void dispose() {
        stage.dispose();
        padBackgroundTexture.dispose();
        padKnobTexture.dispose();
    }
}
