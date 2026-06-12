package com.dbcgames.bridgeit;

import com.artemis.Component;

// Bounding box offsets from mob center to each corner
public class BBoxCmp extends Component {
    public static final float DEFAULT_SCALE = 0.8f;

    public float topLeftX;
    public float topLeftY;
    public float topRightX;
    public float topRightY;
    public float bottomLeftX;
    public float bottomLeftY;
    public float bottomRightX;
    public float bottomRightY;

    // Sets corner offsets based on mob size and scale factor
    public void setBySize(float width, float height) {
        float halfW = width * DEFAULT_SCALE * 0.5f;
        float halfH = height * DEFAULT_SCALE * 0.5f;

        topLeftX = -halfW;
        topLeftY = halfH;
        topRightX = halfW;
        topRightY = halfH;
        bottomLeftX = -halfW;
        bottomLeftY = -halfH;
        bottomRightX = halfW;
        bottomRightY = -halfH;
    }
}
