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
import com.ray3k.superracetospace.Entity;
import com.ray3k.superracetospace.Maths;
import com.ray3k.superracetospace.states.GameState;

public class CameraEntity extends Entity {
    private Entity target;
    private float transitionTime;
    private static final float TRANSITION = 2.0f;
    private static final float OFFSET = 100.0f;
    private float nextStarLevel;

    @Override
    public void create() {
        nextStarLevel = 0.0f;
    }

    @Override
    public void act(float delta) {
        GameState.inst().getGameCamera().position.set(getX(), getY(), 0.0f);
        
        if (target != null) {
            if (transitionTime > 0) {
                transitionTime -= delta;
                float calc = Maths.approach(getY(), target.getY() + OFFSET, (TRANSITION - transitionTime) / TRANSITION * Math.abs(target.getY() - getY() + OFFSET));
                setPosition(target.getX(), Math.max(calc, GameState.GAME_HEIGHT / 2.0f));
            }
            
            if (transitionTime <= 0) {
                setPosition(target.getX(), Math.max(target.getY() + OFFSET, GameState.GAME_HEIGHT / 2.0f));
            }
        }
        
        if (getY() + GameState.GAME_HEIGHT / 2.0f > nextStarLevel) {
            nextStarLevel += GameState.GAME_HEIGHT;
            GameState.inst().populateStars(nextStarLevel);
        }
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
        transitionTime = TRANSITION;
    }

}
