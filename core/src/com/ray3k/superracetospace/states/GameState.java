/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
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
package com.ray3k.superracetospace.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.superracetospace.Core;
import com.ray3k.superracetospace.EntityManager;
import com.ray3k.superracetospace.InputManager;
import com.ray3k.superracetospace.State;
import com.ray3k.superracetospace.entities.BackgroundEntity;
import com.ray3k.superracetospace.entities.CameraEntity;
import com.ray3k.superracetospace.entities.StageChangeListener;
import com.ray3k.superracetospace.entities.StageEntity;
import com.ray3k.superracetospace.entities.StarEntity;
import com.ray3k.superracetospace.entities.TowerEntity;

public class GameState extends State {
    private static GameState instance;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    public static final float GAME_WIDTH = 800.0f;
    public static final float GAME_HEIGHT = 600.0f;
    public static int stageNumber;
    public static int currentStageNumber;
    public static Array<StageChangeListener> stageChangeListeners = new Array<StageChangeListener>();
    public static CameraEntity cam;
    public static Array<StageEntity> stages = new Array<StageEntity>();
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        instance = this;
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/Super Race To Space.atlas", TextureAtlas.class);
        
        score = 0;
        
        inputManager = new InputManager();
        
        gameCamera = new OrthographicCamera();
        gameViewport = new StretchViewport(GameState.GAME_WIDTH, GameState.GAME_HEIGHT, gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getWidth(), true);
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Super Race To Space.json", Skin.class);
        stage = new Stage(new StretchViewport(GameState.GAME_WIDTH, GameState.GAME_HEIGHT));
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        stageNumber = 0;
        currentStageNumber = 0;
        stageChangeListeners.clear();
        
        cam = new CameraEntity();
        cam.setPosition(GAME_WIDTH / 2.0f, GAME_HEIGHT / 2.0f);
        entityManager.addEntity(cam);
        
        BackgroundEntity back = new BackgroundEntity();
        back.setPosition(0.0f, 0.0f);
        entityManager.addEntity(back);
        
        TowerEntity towerEntity = new TowerEntity();
        towerEntity.setPosition(200.0f, 0.0f);
        entityManager.addEntity(towerEntity);
        
        for (int i = 1; i <= 5; i++) {
            StageEntity stageEnt = new StageEntity();
            stageEnt.setStageNumber(i);
            stageEnt.setPosition(400.0f, 40.0f + (i - 1) * 67);
            stageEnt.getSkeleton().findBone("stage").setScaleY((6 - i * .35f) * .2f);
            stageEnt.setDepth(-100 + i);
            entityManager.addEntity(stageEnt);
        }
        
        populateStars(0);
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top().expandX();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(31.0f / 255.0f, 38.0f / 255.0f, 51.0f / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (currentStageNumber == stageNumber) {
            if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                stageNumber++;
                
                for (StageChangeListener listener : stageChangeListeners) {
                    listener.stageChanged(stageNumber);
                }
            }
        } else {
            if (!Gdx.input.isKeyPressed(Keys.SPACE)) {
                currentStageNumber = stageNumber;
            }
        }
        
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playSound(String name) {
        playSound(name, 1.0f, 1.0f);
    }
    
    public void playSound (String name, float volume) {
        playSound(name, volume, 1.0f);
    }
    
    /**
     * 
     * @param name
     * @param volume
     * @param pitch .5 to 2. 1 is default
     */
    public void playSound(String name, float volume, float pitch) {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/" + name + ".wav", Sound.class).play(volume, pitch, 0.0f);
    }
    
    public void populateStars(float yLevel) {
        for (int i = 0; i < 30; i++) {
            StarEntity star = new StarEntity();
            star.setPosition(MathUtils.random(0.0f, GAME_WIDTH), MathUtils.random(yLevel, yLevel + GAME_HEIGHT));
            entityManager.addEntity(star);
        }
    }
}