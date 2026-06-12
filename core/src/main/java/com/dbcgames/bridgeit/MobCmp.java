package com.dbcgames.bridgeit;

import com.artemis.Component;

public class MobCmp extends Component {
    public MobType type;
    public float x;
    public float y;
    public float vx;
    public float vy;
    public boolean flip = false;  // default facing is right
    public float speed = Game.DEFAULT_MOB_SPEED;
}
