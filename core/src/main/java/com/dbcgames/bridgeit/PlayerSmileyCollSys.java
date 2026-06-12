package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;

@All({MobCmp.class, BBoxCmp.class})
public class PlayerSmileyCollSys extends IteratingSystem {
    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<BBoxCmp> bboxMap;
    private final PlayerDeathListener deathListener;

    private boolean hasPlayer = false;
    private float playerMinX;
    private float playerMaxX;
    private float playerMinY;
    private float playerMaxY;

    public PlayerSmileyCollSys(PlayerDeathListener deathListener) {
        super(Aspect.all(MobCmp.class, BBoxCmp.class));
        this.deathListener = deathListener;
    }

    @Override
    protected void begin() {
        hasPlayer = false;

        IntBag players = world.getAspectSubscriptionManager()
            .get(Aspect.all(PlayerCmp.class, MobCmp.class, BBoxCmp.class))
            .getEntities();
        if (players.size() == 0)
            return;

        int playerId = players.get(0);
        MobCmp mob = mobMap.get(playerId);
        BBoxCmp box = bboxMap.get(playerId);

        playerMinX = mob.x + box.bottomLeftX;
        playerMaxX = mob.x + box.bottomRightX;
        playerMinY = mob.y + box.bottomLeftY;
        playerMaxY = mob.y + box.topLeftY;
        hasPlayer = true;
    }

    @Override
    protected void process(int e) {
        if (!hasPlayer)
            return;

        MobCmp mob = mobMap.get(e);
        if (mob.type != MobType.SMILEY)
            return;

        BBoxCmp box = bboxMap.get(e);
        float minX = mob.x + box.bottomLeftX;
        float maxX = mob.x + box.bottomRightX;
        float minY = mob.y + box.bottomLeftY;
        float maxY = mob.y + box.topLeftY;

        boolean overlaps =
            minX <= playerMaxX && maxX >= playerMinX &&
            minY <= playerMaxY && maxY >= playerMinY;

        if (overlaps) {
            Gdx.app.debug("PlayerSmileyCollSys", "Player colliding with smiley id=" + e);
            if (deathListener != null) {
                deathListener.onPlayerKilled();
            }
        }
    }
}
