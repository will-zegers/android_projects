package com.will.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    final float gapSize = 400f;
    final float frameDuration = 0.100f;
    final float gravity = 0.5f;
    final float tubeLipSize = 100f;


    float       stateTime;
    float       spriteWidth;
    float       spriteHeight;
    float       birdY;
    float       birdVelocity;
    float       maxTubeOffset;
    float       tubeOffset;

    boolean     hasStarted;

    Texture     topTube;
    Texture     bottomTube;

	Texture     background;
    Texture[]   birds;
    Animation   flapAnimation;

    SpriteBatch batch;

    Random      randomGenerator;

    @Override
	public void create () {

        stateTime    = 0f;
        birdY        = (Gdx.graphics.getHeight() - spriteHeight)/2;
        birdVelocity = 0;
        maxTubeOffset = (Gdx.graphics.getHeight() - gapSize)/2 - tubeLipSize;
        tubeOffset    = 0;

        randomGenerator = new Random();

		batch = new SpriteBatch();

        background = new Texture("bg.png");
        topTube    = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");

        Texture bird = new Texture("bird.png");
        spriteWidth  = bird.getWidth();
        spriteHeight = bird.getHeight();

        flapAnimation = new Animation(
                frameDuration,
                new TextureRegion[] {
                    new TextureRegion(bird),
                    new TextureRegion(new Texture("bird2.png")),
                });
        birds = new Texture[] {new Texture("bird.png"), new Texture("bird2.png")};
	}

	@Override
	public void render () {

        if (birdY < 0) {birdY = 0;}

        batch.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (hasStarted) {
            if (Gdx.input.justTouched()) {
                birdVelocity = -15;
//                tubeOffset = 886.0f;
                tubeOffset = randomGenerator.nextFloat() * (Gdx.graphics.getHeight() - 2*tubeLipSize - gapSize/2) + tubeLipSize + gapSize;
            }
            birdVelocity += gravity;
            birdY -= birdVelocity;
            batch.draw(topTube, (Gdx.graphics.getWidth() - topTube.getWidth())/2 , tubeOffset);
//            batch.draw(bottomTube, (Gdx.graphics.getWidth() - bottomTube.getWidth())/2 , tubeOffset - gapSize/2 - 1280);

        } else {
            tubeOffset = 0;
            if (Gdx.input.justTouched()) {
                hasStarted = true;
            }
        }

        stateTime += Gdx.graphics.getDeltaTime();
        batch.draw(flapAnimation.getKeyFrame(stateTime, true),
                (Gdx.graphics.getWidth() - spriteWidth)/2,
                birdY);
		batch.end();
	}
}
