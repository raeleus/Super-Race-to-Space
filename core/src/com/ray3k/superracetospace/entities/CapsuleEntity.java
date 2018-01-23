/*
 * The MIT License
 *
 * Copyright 2018 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.superracetospace.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ray3k.superracetospace.Core;
import com.ray3k.superracetospace.Entity;
import com.ray3k.superracetospace.SpineEntity;
import com.ray3k.superracetospace.states.GameState;
import static com.ray3k.superracetospace.states.GameState.stageChangeListeners;

public class CapsuleEntity extends SpineEntity implements StageChangeListener {
    private MoonEntity moon;
    private ParticleEntity particleEntity;
    
    public static enum Mode {
        STATIONARY, FLYING, FALLING, QUEUE_DEATH
    }
    private Mode mode;

    public CapsuleEntity() {
        super(Core.DATA_PATH + "/spine/capsule.json", "normal");
        
        stageChangeListeners.add(this);
        
        getAnimationState().getData().setDefaultMix(0.0f);
        mode = Mode.STATIONARY;
        setDepth(-90);
    }

    @Override
    public void actSub(float delta) {
        if (particleEntity != null) {
            particleEntity.setPosition(getX(), getY());
        }
        
        if (mode == Mode.FLYING || mode == Mode.QUEUE_DEATH) {
            addMotion(100.0f * delta, 90.0f);
            if (getSpeed() > 1000.0f) {
                setMotion(1000.0f, 90.0f);
            }
        } else if (mode == Mode.FALLING) {
            addMotion(1500.0f * delta, 270.0f);
            if (getSpeed() > 1000.0f) {
                setMotion(1000.0f, 270.0f);
            }
        }
        
        if (mode == Mode.QUEUE_DEATH) {
            if (getY() > GameState.cam.getY() + GameState.GAME_HEIGHT / 2.0f + 1000.0f) {
                if (particleEntity != null) {
                    particleEntity.dispose();
                }
                
                dispose();
                
                if (moon != null) {
                    moon.getAnimationState().setAnimation(0, "win", false);
                } else {
                    GameState.entityManager.addEntity(new GameOverTimerEntity(3.0f));
                }
                
                GameState.rocketSound.stop();
            }
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    @Override
    public void stageChanged(int stage) {
        if (stage == 1) {
            mode = Mode.FLYING;
            GameState.rocketSound.setLooping(true);
            GameState.rocketSound.play();
        }
        
        if (stage == 6) {
            particleEntity = new ParticleEntity(Core.DATA_PATH + "/particles/thrust.p");
            particleEntity.setPosition(getX(), getY());
            particleEntity.getEffect().setEmittersCleanUpBlendFunction(false);
            particleEntity.setDepth(getDepth() + 1);
            GameState.entityManager.addEntity(particleEntity);
            
            GameState.cam.setTarget(this);
            
            if (GameState.inst().getScore() > 430.0f) {
                moon = new MoonEntity();
                moon.setPosition(getX(), GameState.cam.getY() + GameState.GAME_HEIGHT / 2.0f + 200.0f);
                GameState.entityManager.addEntity(moon);
                GameState.cam.setTarget(moon);
            } else {
                GameState.cam.setTarget(null);
            }
            
            mode = Mode.QUEUE_DEATH;
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.FALLING) {
            GameState.rocketSound.stop();
        }
    }
}
