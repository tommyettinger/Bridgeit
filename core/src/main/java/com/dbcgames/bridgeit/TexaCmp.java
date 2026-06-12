package com.dbcgames.bridgeit;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;

// Animated texture
public class TexaCmp extends Component {
    public Animation<TextureRegion> movingAnim;
    public Animation<TextureRegion> idleAnim;
    public float stateTime = 0;
    public boolean moving = false;
    public TextureRegion currentTex;
    public Color color = new Color(Color.WHITE);
}
