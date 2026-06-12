package com.dbcgames.bridgeit;

import com.artemis.Component;
import com.github.tommyettinger.gdcrux.PointF2;

// Per-smiley state
public class SmileyCmp extends Component {
    public float runawayTime = 0f;
    public PointF2 runawayTarget = new PointF2(-1f, -1f);
}
