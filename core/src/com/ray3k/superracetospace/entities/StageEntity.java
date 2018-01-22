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
import com.esotericsoftware.spine.AnimationState;
import com.ray3k.superracetospace.Core;
import com.ray3k.superracetospace.Entity;
import com.ray3k.superracetospace.SpineEntity;
import com.ray3k.superracetospace.states.GameState;
import static com.ray3k.superracetospace.states.GameState.stageChangeListeners;
import static com.ray3k.superracetospace.states.GameState.stages;

public class StageEntity extends SpineEntity implements StageChangeListener{
    private int stageNumber;
    private float counterDelay;
    public static enum Mode {
        STATIONARY, FLYING, FALLING
    }
    private Mode mode;

    public StageEntity() {
        super(Core.DATA_PATH + "/spine/stage.json", "normal");
        stageChangeListeners.add(this);
        stages.add(this);
        
        getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                String name = entry.getAnimation().getName();
                if (name.equals("discard")) {
                    StageEntity.this.dispose();
                } else if (name.equals("deplete")) {
                    GameState.cam.setTarget(null);
                    for (StageEntity stage : GameState.stages) {
                        stage.setMode(Mode.FALLING);
                        GameState.entityManager.addEntity(new GameOverTimerEntity(3.5f));
                        GameState.stageChangeListeners.clear();
                    }
                }
            }
        });
        
        getAnimationState().getData().setDefaultMix(0.0f);
        
        counterDelay = -1;
        mode = Mode.STATIONARY;
    }

    @Override
    public void actSub(float delta) {
        if (mode == Mode.FLYING) {
            addMotion(100.0f * delta, 90.0f);
            if (getSpeed() > 1000.0f) {
                setMotion(1000.0f, 90.0f);
            }
        } else if (mode == Mode.FALLING) {
            addMotion(1000.0f * delta, 270.0f);
            if (getSpeed() > 1000.0f) {
                setMotion(1000.0f, 270.0f);
            }
        }
        
        if (counterDelay > 0) {
            counterDelay -= delta;
            if (counterDelay <= 0) {
                getAnimationState().setAnimation(0, "deplete", false);
                getAnimationState().setTimeScale(.075f + stageNumber * .22f);
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

    public int getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(int stageNumber) {
        this.stageNumber = stageNumber;
    }

    @Override
    public void stageChanged(int stage) {
        if (stage == 1) {
            mode = Mode.FLYING;
        }
        
        if (stage == stageNumber) {
            GameState.cam.setTarget(this);
            if (stage == 1) {
                counterDelay = 3.0f;
            } else {
                counterDelay = .5f;
            }
        } else if (stage > stageNumber) {
            if (!getAnimationState().getCurrent(0).getAnimation().getName().equals("discard")) {
                GameState.inst().addScore((int) (getAnimationState().getCurrent(0).getAnimationTime() * 100));
                getAnimationState().setAnimation(0, "discard", false);
                getAnimationState().setTimeScale(1.0f);
            }
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
}