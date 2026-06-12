package com.dbcgames.bridgeit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

@All({MobCmp.class, TexaCmp.class})
public class RenderSys extends IteratingSystem {
    private SpriteBatch batch;
    private ComponentMapper<MobCmp> mobMap;
    private ComponentMapper<TexaCmp> texaMap;
    
    public RenderSys() {
        super(Aspect.all(MobCmp.class, TexaCmp.class));
        batch = new SpriteBatch();
    }


    @Override
     protected void begin() {
         batch.setProjectionMatrix(Game.viewport.getCamera().combined);
         batch.begin();
     }
 
     @Override
     protected void process(int e) {
         MobCmp mob = mobMap.get(e);
         TexaCmp texa = texaMap.get(e);

         TextureRegion sprite = texa.currentTex;
        if (sprite == null) {
            return; // texture not ready yet; skip drawing this entity
        }
         float width = sprite.getRegionWidth();
         float height = sprite.getRegionHeight();
         float drawX = mob.x - width * 0.5f;
         float drawY = mob.y - height * 0.5f;

         Color previous = batch.getColor();
         batch.setColor(texa.color);
 
         if (mob.flip) 
             batch.draw(sprite, drawX + width, drawY, -width, height);
         else
             batch.draw(sprite, drawX, drawY, width, height);

         batch.setColor(previous);
     }

    @Override
     protected void end() {
         batch.end();
     }

}
