package com.dbcgames.bridgeit;

import com.artemis.Component;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gdcrux.PointF2;

// AI state shared by NPCs that pathfind.
public class AiCmp extends Component {
    public Path<PointF2> path = new Path<>(8);
    public int pathIndex = 0;
    public float repathTimer = 0f;
}
