package com.will.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    final float   gravity       = -0.5f;
    float         stateTime;
    float         screenWidth;
    float         screenHeight;
    Random        randomGenerator;
    boolean       hasStarted;
    Texture       background;
    SpriteBatch   batch;

    final float frameDuration = 0.100f;
    float       birdSpriteWidth;
    float       birdSpriteHeight;
    float       birdY;
    float       birdVelocity;
    Circle      birdCircle    = new Circle();
    Animation   flapAnimation;

    final int   numberOfTubes    = 4;
    final float tubeVelocity     = 4;
    final float tubeLipSize      = 100f;
    final float gapSize          = 400f;
    float       tubeDistance;
    float[]     tubeX         = new float[numberOfTubes];
    float[]     topTubeLoc    = new float[numberOfTubes];
    Texture     topTube;
    Texture     bottomTube;
    Rectangle[][] tubeRectangle = new Rectangle[numberOfTubes][2];

    @Override
	public void create () {

        batch           = new SpriteBatch();
        background      = new Texture("bg.png");
        screenWidth     = Gdx.graphics.getWidth();
        screenHeight    = Gdx.graphics.getHeight();
        randomGenerator = new Random();

        stateTime    = 0f;
        birdY        = (screenHeight - birdSpriteHeight)/2;
        birdVelocity = 0;

        Texture bird     = new Texture("bird.png");
        birdSpriteWidth  = bird.getWidth();
        birdSpriteHeight = bird.getHeight();
        flapAnimation    = new Animation(
                frameDuration,
                new TextureRegion(bird),
                new TextureRegion(new Texture("bird2.png"))
        );

        topTube      = new Texture("toptube.png");
        bottomTube   = new Texture("bottomtube.png");
        tubeDistance = screenWidth *3 / 4;
        for (int i = 0; i < numberOfTubes; i++) {
            tubeRectangle[i][0] = new Rectangle();
            tubeRectangle[i][1] = new Rectangle();
            topTubeLoc[i] = randomGenerator.nextFloat() *
                    (screenHeight - 2*tubeLipSize - gapSize) + gapSize + tubeLipSize;
            tubeX[i] = screenWidth / 2 + (i+1)*tubeDistance;
        }
	}

	@Override
	public void render () {

        if (birdY < 0) {birdY = 0;}

        batch.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.draw(background, 0, 0, screenWidth, screenHeight);

        if (hasStarted) {
            if (Gdx.input.justTouched()) {
                birdVelocity = 15;
            }

            birdVelocity += gravity;
            birdY += birdVelocity;

            for (int i = 0; i < numberOfTubes; i++) {

                if (tubeX[i] < 0 - topTube.getWidth()) {
                    topTubeLoc[i] = randomGenerator.nextFloat() *
                            (screenHeight - 2*tubeLipSize - gapSize) + gapSize + tubeLipSize;
                    tubeX[i] += numberOfTubes * tubeDistance;
                }

                tubeX[i] -= tubeVelocity;
                float topX = tubeX[i] - topTube.getWidth() / 2;
                float topY = topTubeLoc[i];
                float bottomX = tubeX[i] - bottomTube.getWidth() / 2;
                float bottomY = topTubeLoc[i] - gapSize - bottomTube.getHeight();

                batch.draw(topTube, topX, topY);
                batch.draw(bottomTube, bottomX, bottomY);

                tubeRectangle[i][0].set(topX, topY, topTube.getWidth(), topTube.getHeight());
                tubeRectangle[i][1].set(bottomX, bottomY, bottomTube.getWidth(), bottomTube.getHeight());
            }

        } else {
            if (Gdx.input.justTouched()) {
                hasStarted = true;
            }
        }

        stateTime += Gdx.graphics.getDeltaTime();
        batch.draw(flapAnimation.getKeyFrame(stateTime, true),
                (screenWidth - birdSpriteWidth)/2,
                birdY);
		batch.end();

        birdCircle.set(screenWidth/2, birdY + birdSpriteHeight/2, (birdSpriteWidth + birdSpriteHeight)/4);

        for (int i = 0; i < numberOfTubes; i++) {

                if (Intersector.overlaps(birdCircle, tubeRectangle[i][0]) || Intersector.overlaps(birdCircle, tubeRectangle[i][1])) {
                    Gdx.app.log("Collision", "Yes");
                }
        }
	}
}
