package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class Drop extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private long lastDropTime;

	public final static Vector2 screenSize = new Vector2(800, 480);
	private final int spriteSize = 64;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));

		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenSize.x, screenSize.y);

		bucket = new Rectangle();
		bucket.width = spriteSize;
		bucket.height = spriteSize;
		bucket.setPosition((screenSize.x / 2) - (bucket.width /2), 20);

		raindrops = new Array<>();
		spawnRaindrop();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, screenSize.x - spriteSize);
		raindrop.y = screenSize.y;
		raindrop.width = spriteSize;
		raindrop.height = spriteSize;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}


	private void update() {
		final float bucketSpeed = 200.f;
		final long secInNanos = 1000000000;

		// Input - Mouse or Touch
		if(Gdx.input.isTouched()) {
			Vector3 mousePos = new Vector3();
			mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(mousePos);
			bucket.setX(mousePos.x - (bucket.width / 2));
		}
		// Input - Keyboard
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= bucketSpeed * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += bucketSpeed * Gdx.graphics.getDeltaTime();

		// Clamp bucket to the screen
		if(bucket.getX() < 0) bucket.setX(0);
		if(bucket.getX() > screenSize.x - spriteSize) bucket.setX(screenSize.x - spriteSize);

		// Spawn more raindrops after a set time
		if(TimeUtils.nanoTime() - lastDropTime > secInNanos) spawnRaindrop();

		for (Iterator<Rectangle> it = raindrops.iterator(); it.hasNext(); ) {
			Rectangle raindrop = it.next();
			raindrop.y -= bucketSpeed * Gdx.graphics.getDeltaTime();

			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				it.remove();
			} else if(raindrop.y + spriteSize < 0) it.remove();
		}
	}

	@Override
	public void render () {
		// This is so wrong it hurts me, is render meant to be my update loop?
		// What happened to SRP? Fair enough sometimes you need to stretch it a
		// bit but the whole update loop in render is blasphemous
		// I've put it into a function to separate the logic, I feel at ease
		update();

		// This is supposed to be here, because it's render
		ScreenUtils.clear(1, 0, 0.2f, 1);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.getX(), bucket.getY());
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
