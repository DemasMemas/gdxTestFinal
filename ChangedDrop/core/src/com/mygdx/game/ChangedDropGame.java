package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class ChangedDropGame extends ApplicationAdapter {
	private Texture boneImage;
	private Texture meatImage;
	private Texture fishImage;
	private Texture bagImage;
	private Texture bottleImage;
	private Texture mouthImage;
	private TextureRegion hungerBarBody;
	private Texture hungerBarBG;
	private Texture hungerBarProgress;
	private TextureRegion backgroundTexture;
	private TextureRegion backgroundDeadTexture;
	private Sound eatSound;
	private Music waterMusic;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle mouth;
	private Array<BetterRectangle> fooddrops;
	private long lastDropTime;
	private float hunger = 100;
	private int score = 0;
	private BitmapFont fontHunger;
	private BitmapFont fontTimeScore;

	@Override
	public void create() {
		fontHunger = new BitmapFont();
		fontTimeScore = new BitmapFont();
		boneImage = new Texture(Gdx.files.internal("bone.png"));
		meatImage = new Texture(Gdx.files.internal("meat.png"));
		fishImage = new Texture(Gdx.files.internal("fish.png"));
		bagImage = new Texture(Gdx.files.internal("bag.png"));
		bottleImage = new Texture(Gdx.files.internal("bottle.png"));
		mouthImage = new Texture(Gdx.files.internal("mouth.png"));
		hungerBarBG = new Texture("hunger-bg.png");
		hungerBarProgress = new Texture("hunger-bg-progress.png");
		hungerBarBody = new TextureRegion(hungerBarProgress, 16, 11, hungerBarProgress.getWidth(), hungerBarProgress.getHeight());
		backgroundTexture = new TextureRegion(new Texture("bg.png"), 0, 0, 800, 480);
		backgroundDeadTexture = new TextureRegion(new Texture("dead.png"), 0, 0, 800, 480);

		// load the eat sound effect and the water background "music"
		eatSound = Gdx.audio.newSound(Gdx.files.internal("eat.wav"));
		waterMusic = Gdx.audio.newMusic(Gdx.files.internal("water.mp3"));

		// start the playback of the background music immediately and decrease its volume
		waterMusic.setLooping(true);
		waterMusic.setVolume(15);
		waterMusic.play();

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		// create a Rectangle to logically represent the mouth
		mouth = new Rectangle();
		mouth.x = 368; // center the mouth horizontally
		mouth.width = 64;
		mouth.height = 64;

		// create the fooddrops array and spawn the first fooddrop
		fooddrops = new Array<>();
		spawnFoodDrop();
	}

	private void spawnFoodDrop() {
		BetterRectangle fooddrop = new BetterRectangle();
		if (MathUtils.random(10) <= 8){
			if (MathUtils.random(10) <= 5){
				fooddrop.x = MathUtils.random(0, 800-24);
				fooddrop.y = 480;
				fooddrop.width = 24;
				fooddrop.height = 84;
				fooddrop.saturate = 1.0;
				fooddrop.type = boneImage;
			} else if(MathUtils.random(10) <= 5){
				fooddrop.x = MathUtils.random(0, 800-64);
				fooddrop.y = 480;
				fooddrop.width = 64;
				fooddrop.height = 64;
				fooddrop.saturate = 3.0;
				fooddrop.type = meatImage;
			} else{
				fooddrop.x = MathUtils.random(0, 800-64);
				fooddrop.y = 480;
				fooddrop.width = 64;
				fooddrop.height = 128;
				fooddrop.AI = 1;
				fooddrop.saturate = 10.0;
				fooddrop.type = fishImage;
			}
		} else if(MathUtils.random(10) <= 7){
			fooddrop.x = MathUtils.random(0, 800-40);
			fooddrop.y = 480;
			fooddrop.width = 40;
			fooddrop.height = 64;
			fooddrop.AI = 0;
			fooddrop.saturate = -5.0;
			fooddrop.type = bagImage;
		} else {
			fooddrop.x = MathUtils.random(0, 800-32);
			fooddrop.y = 480;
			fooddrop.width = 32;
			fooddrop.height = 100;
			fooddrop.AI = 0;
			fooddrop.saturate = -20.0;
			fooddrop.type = bottleImage;
		}
		fooddrops.add(fooddrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render() {
		if (hunger > 0){
			hunger -= 0.06;
			score += 1;

			// clear the screen
			ScreenUtils.clear(0, 0, 0, 1);

			// tell the camera to update its matrices.
			camera.update();

			// tell the SpriteBatch to render in the
			// coordinate system specified by the camera.
			batch.setProjectionMatrix(camera.combined);

			// begin a new batch and draw the mouth and
			// all drops
			batch.begin();
			batch.draw(backgroundTexture, 0, 0,800,480);
			// hunger bar
			batch.draw(hungerBarBG, 10, 5);
			batch.draw(hungerBarBody, 16, -2, 132 * (hunger/100), hungerBarBody.getRegionHeight());
			batch.draw(mouthImage, mouth.x, 0);
			fontHunger.draw(batch, "Food", 60, 50);
			// timer
			fontTimeScore.draw(batch, "Score: " + score, 45, 70);
			// food drop
			for(BetterRectangle fooddrop: fooddrops) {
				batch.draw(fooddrop.type, fooddrop.x, fooddrop.y);
			}
			batch.end();

			// process user input
			if(Gdx.input.isTouched()) {
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos);
				mouth.x = touchPos.x - 32;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) mouth.x -= 500 * Gdx.graphics.getDeltaTime();
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mouth.x += 500 * Gdx.graphics.getDeltaTime();

			// make sure the mouth stays within the screen bounds
			if(mouth.x < 0) mouth.x = 0;
			if(mouth.x > 800 - 64) mouth.x = 800 - 64;

			// check if we need to create a new fooddrop
			if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnFoodDrop();

			// move the fooddrops, remove any that are beneath the bottom edge of
			// the screen or that hit the mouth. In the latter case we play back
			// a sound effect as well.
			for (Iterator<BetterRectangle> iter = fooddrops.iterator(); iter.hasNext(); ) {
				BetterRectangle fooddrop = iter.next();
				fooddrop.y -= 200 * Gdx.graphics.getDeltaTime();
				if (fooddrop.AI == 0){
					fooddrop.x -= MathUtils.random(-50,50) * Gdx.graphics.getDeltaTime();
				} else{
					fooddrop.y -= 200 * Gdx.graphics.getDeltaTime();
					fooddrop.x -= MathUtils.random(-250,250) * Gdx.graphics.getDeltaTime();
				}
				if(fooddrop.y + 64 < 0) iter.remove();
				if(fooddrop.overlaps(mouth)) {
					hunger += fooddrop.saturate;
					if (hunger > 100){ hunger = 100; }
					eatSound.play(0.3f);
					iter.remove();
				}
			}
		} else {
			ScreenUtils.clear(0, 0, 0, 1);
			camera.update();
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.draw(backgroundDeadTexture, 0, 0,800,480);
			fontTimeScore.draw(batch, "Score: " + score, 345, 240);
			batch.end();
		}

	}

	@Override
	public void dispose() {
		boneImage.dispose();
		mouthImage.dispose();
		eatSound.dispose();
		waterMusic.dispose();
		batch.dispose();
	}
}