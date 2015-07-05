package com.zozotintin.dodgydot;

import java.util.ArrayDeque;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

class GameScreen extends GameSample implements Configuration, ContactListener {
	static final int GAME_READY_SETUP = 6;
	static final int GAME_READY = 0;
	static final int GAME_RUNNING = 1;
	static final int GAME_PAUSED = 2;
	static final int GAME_OVER = 3;
	static final int GAME_OVER_SETUP = 4; 
	static final int BALL_DESTROYED = 5;
	int state;
	boolean gameOverSetup;
	
	World world;
	Stage stage;
	
	final short CATEGORY_PLAYER = 0x0001;
	final short CATEGORY_ENEMY = 0x0002;
	final short CATEGORY_WALL = 0x0004;
	
	final short MASK_PLAYER = ~CATEGORY_PLAYER;
	final short MASK_ENEMY = ~CATEGORY_ENEMY & ~CATEGORY_WALL;
	final short MASK_WALL = -1;
	
	float timeGap = 0;
	float playTime = 0;

	
	DodgyDot game;
	private static OrthographicCamera camera, cameraHUD;
	private static Viewport viewport, viewportHUD;
	
	//Logger logger = new Logger("DEBUG", Logger.INFO);
		
	Vector3 point = new Vector3();
	//Vector2 pos = new Vector2();
	
	CircleShape ballShape;
	PolygonShape squareShape;

	Queue<Enemy> enemies = new ArrayDeque<Enemy>();
	
	Body wall;
	Player player;
	Retry retry;
	Background background;
	
	LeftPrompt leftPrompt;
	RightPrompt rightPrompt;
	
	Label scoreLabel;
	Label highScoreLabel;
	
	
	public GameScreen (DodgyDot game) {
		this.game = game;
		world = new World(new Vector2(0, -GRAVITY), true);

		
		camera = new OrthographicCamera();
		viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
		camera.translate(new Vector2(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2));
		//camera.zoom = 4f; camera.position.y = -10;
		camera.update();
		
		cameraHUD = new OrthographicCamera();
		viewportHUD = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, cameraHUD);
		
		stage = new Stage(viewportHUD, game.batch);
		
		scoreLabel = new Label(Integer.toString((int) playTime), game.labelStyle);
		stage.addActor(scoreLabel);
		scoreLabel.setFontScale(1.5f);
		scoreLabel.setPosition(VIRTUAL_WIDTH - scoreLabel.getWidth(), VIRTUAL_HEIGHT - scoreLabel.getHeight());
		scoreLabel.setAlignment(Align.right);
		
		state = GAME_READY_SETUP;
		gameOverSetup = false;
		
		squareShape = new PolygonShape();
		squareShape.setAsBox(SQUARE_WIDTH / 2, SQUARE_WIDTH / 2);
		
		background = new Background();
		player = new Player(Assets.ball, RETRY_X , RETRY_Y);
		
		leftPrompt = new LeftPrompt();
		rightPrompt = new RightPrompt();
		
		stage.addActor(background);
		background.setZIndex(0);
		
		stage.addActor(leftPrompt);
		stage.addActor(rightPrompt);
		
		stage.addActor(player);
		player.setZIndex(40);
		
		
		
		
		createWall();
		
		Gdx.input.setInputProcessor(this);
		world.setContactListener(this);
	}
	
	private void createWall () {
		BodyDef wallD = new BodyDef();
		wallD.type = BodyType.KinematicBody;
		
		wall = world.createBody(wallD);
		
		EdgeShape edge1 = new EdgeShape();
		edge1.set(0.0f, -5.0f, 0.0f, SCREEN_HEIGHT);
		EdgeShape edge2 = new EdgeShape();
		edge2.set(0.0f, SCREEN_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT);
		EdgeShape edge3 = new EdgeShape();
		edge3.set(SCREEN_WIDTH, SCREEN_HEIGHT, SCREEN_WIDTH, -5.0f);
		EdgeShape edge4 = new EdgeShape();
		edge4.set(0.0f, -BALL_WIDTH, SCREEN_WIDTH, -BALL_WIDTH);
		
		FixtureDef wallFD = new FixtureDef();
		wallFD.shape = edge1;
		wallFD.filter.categoryBits = CATEGORY_WALL;
		wallFD.filter.maskBits = MASK_WALL;
		wall.createFixture(wallFD);
		edge1.dispose();
		
		wallFD = new FixtureDef();
		wallFD.restitution = 0.0f;
		wallFD.shape = edge2;
		wallFD.filter.categoryBits = CATEGORY_WALL;
		wallFD.filter.maskBits = MASK_WALL;
		wall.createFixture(wallFD);
		edge2.dispose();
		
		wallFD = new FixtureDef();
		wallFD.shape = edge3;
		wallFD.filter.categoryBits = CATEGORY_WALL;
		wallFD.filter.maskBits = MASK_WALL;
		wall.createFixture(wallFD);
		edge3.dispose();
		
		wallFD = new FixtureDef();
		wallFD.shape = edge4;
		wallFD.isSensor = true;
		wallFD.filter.categoryBits = CATEGORY_WALL;
		wallFD.filter.maskBits = MASK_WALL;
		wall.createFixture(wallFD);
		edge4.dispose();
	}
	
	public abstract class GameObject extends Actor {
		protected Body body;
		protected TextureRegion texture;
		
		public GameObject () {
		}
		
		public Body getBody () {
			return body;
		}
		
		@Override
		public abstract void draw(Batch batch, float parentAlpha);
	}
	
	private class Retry extends Actor {
		public Retry () {
			setPosition(RETRY_X * 100, SCREEN_HEIGHT * 100);
			setWidth(RETRY_WIDTH * 100);
			setHeight(RETRY_WIDTH * 100);
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			batch.draw(Assets.retry, getX() - getWidth() / 2, getY() - getHeight() / 2, getOriginX(), getOriginY(), getWidth(), getHeight(),
					getScaleX(), getScaleY(), getRotation());
		}
	}
	
	private class Background extends Actor {
		
		@Override
		public void draw(Batch batch, float parentAlpha){
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			//batch.draw(Assets.background, 0, 0);
			batch.draw (Assets.background, 0, 0, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 2,
					2, 0);
		}
	}
	
	private class LeftPrompt extends Actor {
		public LeftPrompt () {
			setPosition(540 - 100 - 1456 / 4, 500);
			setWidth(1456f / 4);
			setHeight(859f / 4);
		}
		public void draw(Batch batch, float parentAlpha){
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			batch.draw (Assets.prompt[0], getX(), getY(), 0, 0, getWidth(), getHeight(), 1,
					1, 0);
		}
	}
	
	private class RightPrompt extends Actor {
		public RightPrompt () {
			setPosition(540 + 85 , 500);
			setWidth(1372f / 4);
			setHeight(859f / 4);
		}
		public void draw(Batch batch, float parentAlpha){
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			batch.draw (Assets.prompt[1], getX(), getY(), 0, 0, getWidth(), getHeight(), 1,
					1, 0);
		}
	}
	
	private class Player extends GameObject {
		
		public Player (TextureRegion texture, float x, float y) {
			this.texture = texture;
			
			BodyDef ballD = new BodyDef();
			ballD.position.set(x, y);
			ballD.type = BodyType.DynamicBody;
			body = world.createBody(ballD);
			
			FixtureDef ballFD = new FixtureDef();
			ballShape = new CircleShape();
			ballShape.setRadius(BALL_WIDTH / 2);
			ballFD.shape = ballShape;
			ballFD.density = 1;
			ballFD.friction = 0;
			ballFD.restitution = 1;
			ballFD.filter.categoryBits = CATEGORY_PLAYER;
			ballFD.filter.maskBits = MASK_PLAYER;
			body.createFixture(ballFD);
			
			ballShape.dispose();
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			batch.draw(
					texture, 
					(body.getPosition().x - (BALL_WIDTH * .5f)) * 100, (body.getPosition().y - (BALL_WIDTH * .5f)) * 100, 
					BALL_WIDTH * 100, BALL_WIDTH * 100);
		}
	}
	
	private class Enemy extends GameObject {		
		
		Enemy (TextureRegion texture) {
			this.texture = texture;
			
			BodyDef squareD = new BodyDef();
			squareD.type = BodyType.KinematicBody;
			squareD.position.set((float) Math.random() * SCREEN_WIDTH, SCREEN_HEIGHT + 3.0f);
			//squareD.linearVelocity.set(0.0f, -((float) Math.random() * (SQUARE_MAX_SPEED - SQUARE_MIN_SPEED)) + SQUARE_MIN_SPEED);
			//squareD.angularVelocity = (SQUARE_MAX_SPEED + squareD.linearVelocity.y) * (Math.random() >= 0.5f ? 1 : -1);
			squareD.gravityScale = 0;
			body = world.createBody(squareD);
			
			FixtureDef squareFD = new FixtureDef();
			squareFD.shape = squareShape;
			//squareFD.density = 99999999;
			squareFD.friction = 0;
			squareFD.restitution = 1;
			squareFD.filter.categoryBits = CATEGORY_ENEMY;
			squareFD.filter.maskBits = MASK_ENEMY;
			body.createFixture(squareFD);
		}
		
		public Body getBody() {
			return body;
		}
		
		@Override
		public void draw(Batch batch, float parentAlpha) {
			Color color = getColor();
			batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
			/*batch.draw(texture, (body.getPosition().x - (SQUARE_WIDTH * .5f)) * 100, (body.getPosition().y - (SQUARE_WIDTH * .5f)) * 100,
					SQUARE_WIDTH * 100 / 2, SQUARE_WIDTH * 100 / 2, SQUARE_WIDTH * 100, SQUARE_WIDTH * 100, 1, 1, (float) Math.toDegrees(body.getAngle()),
					0, 0, this.texture.getRegionWidth(), this.texture.getRegionHeight(), false, false);*/
			batch.draw(texture, (body.getPosition().x - (SQUARE_WIDTH * .5f)) * 100, (body.getPosition().y - (SQUARE_WIDTH * .5f)) * 100,
					SQUARE_WIDTH * 100 / 2, SQUARE_WIDTH * 100 / 2,
					SQUARE_WIDTH * 100, SQUARE_WIDTH * 100,
					1, 1, (float) Math.toDegrees(body.getAngle()));
		}
	}
	
	private void createEnemy () {
		Enemy enemy = new Enemy(Assets.enemies[(int) (Math.random() * 3)]);
		enemy.getBody().setLinearVelocity(0.0f, -(((float) Math.random() * (SQUARE_MAX_SPEED - SQUARE_MIN_SPEED)) + SQUARE_MIN_SPEED));
		enemy.getBody().setAngularVelocity((SQUARE_MAX_SPEED + enemy.getBody().getLinearVelocity().y) * (Math.random() >= 0.5f ? 1 : -1));
		enemies.add(enemy);
		stage.addActor(enemy);
		enemy.setZIndex(10);
		scoreLabel.toFront();
		//logger.info("enemy z:".concat(Integer.toString(enemy.getZIndex())));
		//logger.info("label z:" + Integer.toString(scoreLabel.getZIndex()));
		//logger.info("back z: " + Integer.toString(background.getZIndex()));
		if (state == GAME_OVER && gameOverSetup == true) {
			retry.toFront();
			highScoreLabel.toFront();
			game.highScoreWordsLabel.toFront();
			//logger.info("retry z:".concat(Integer.toString(retry.getZIndex())));
			//logger.info(Integer.toString(world.getBodyCount()));
		}
		for (int i = 0; i < 2; i++) {
			if (enemies.peek() != null && enemies.peek().getBody().getPosition().y < -SCREEN_HEIGHT / 4) {
				world.destroyBody(enemies.peek().getBody());
				enemies.peek().remove();
				enemies.poll();
			}
		}
	}
	
	private void drawEnemy () {
		for (Enemy enemy : enemies) {
			//enemy.draw(game.batch);
		}
	}
	
	public void updateReady (float delta) {
	}
	
	public void renderReady () {
		//player.draw(game.batch);
	}
	
	public void updateRunning (float delta) {
		world.step(delta, 6, 2);
		timeGap += delta;
		playTime += delta;
		if (timeGap >= SQUARE_FALL_INTERVAL) {
			createEnemy();
			timeGap -= SQUARE_FALL_INTERVAL;
		}
		if (state == GAME_RUNNING) {
			//scoreLabel.addAction(Actions.moveTo(540, 990, 1f));
			scoreLabel.setText((Integer.toString((int) playTime)));
			//scoreLabel.setPosition(0, 0);
			scoreLabel.setPosition(VIRTUAL_WIDTH - scoreLabel.getWidth(), VIRTUAL_HEIGHT - scoreLabel.getHeight());
			scoreLabel.setAlignment(Align.right);
			//scoreLabel.setVisible(true);
			//stage.addActor(scoreLabel);
			//logger.info("scoreLabel text: ".concat(scoreLabel.getText().toString()));
			//logger.info("score: ".concat((Integer.toString((int) (playTime)))));
		}
	}
	
	public void renderRunning () {
		//drawEnemy();
		//player.draw(game.batch);
	}
	
	public void udpateBallDestroyed () {
		player.getBody().setActive(false);
		world.destroyBody(player.getBody());
		player.remove();
	}
	
	public void gameOverSetup () {
		retry = new Retry();
		retry.getColor().a = 0;
		retry.addAction(Actions.parallel(Actions.fadeIn(1f),Actions.moveTo(RETRY_X * 100, RETRY_Y * 100, 1f, Interpolation.bounceOut)));
		stage.addActor(retry);
		retry.setZIndex(50);
		
		if ((int) playTime > game.highScore) {
			game.highScore = (int) playTime;
			game.pref.putInteger("Score", game.highScore);
			game.pref.flush();
		}
		
		highScoreLabel = new Label(Integer.toString(game.highScore), game.labelStyle);
		highScoreLabel.setFontScale(1.5f);
		highScoreLabel.setPosition(VIRTUAL_WIDTH - highScoreLabel.getWidth(),  VIRTUAL_HEIGHT - highScoreLabel.getHeight() - 300);
		highScoreLabel.setAlignment(Align.right);
		stage.addActor(highScoreLabel);
		stage.addActor(game.highScoreWordsLabel);
		highScoreLabel.getColor().a = game.highScoreWordsLabel.getColor().a = 0;
		highScoreLabel.setZIndex(50);
		game.highScoreWordsLabel.setZIndex(50);
		highScoreLabel.addAction(Actions.fadeIn(0.25f));
		game.highScoreWordsLabel.addAction(Actions.fadeIn(0.25f));
	}
	
	public void gameReadySetup () {
		//stage.addActor(new LeftPrompt());
		//stage.addActor(new RightPrompt());
		for (Actor actor : stage.getActors()) {
			actor.getColor().a = 0;
			actor.addAction(Actions.fadeIn(0.25f));
		}
		//stage.addAction(Actions.fadeIn(0.5f));
	}
	
	@Override
	public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		/*game.batch.begin();
		game.batch.setProjectionMatrix(viewport.getCamera().combined);
		game.batch.draw(Assets.background, 0, 0);
		game.batch.end();
		*/
		switch (state) {
		
		case GAME_READY_SETUP:
			gameReadySetup();
			state = GAME_READY;
			break;
		
		case GAME_READY:
			updateReady(delta);
			//renderRunning();
			break;
			
		case GAME_RUNNING:
			updateRunning(delta);
			//renderRunning();
			break;
		
		case GAME_OVER_SETUP:
			updateRunning(delta);
			if (gameOverSetup == false) {
				gameOverSetup();
				gameOverSetup = true;
			}
			state = GAME_OVER;
			//renderRunning();
			break;
			
		case GAME_OVER:
			updateRunning(delta);
			break;
			
		case BALL_DESTROYED:
			updateRunning(delta);
			udpateBallDestroyed();
			state = GAME_OVER_SETUP;
			break;
		}
		
		stage.act(delta);
		stage.draw();
		
		//game.debugRenderer.render(world, viewport.getCamera().combined);
	}
	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
	
	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		point.set(screenX, screenY, 0);
		viewport.unproject(point);
		//if ((0 <= point.x && point.x < SCREEN_WIDTH) && (0 <= point.y && point.y < SCREEN_HEIGHT)) {
			switch (state) {
			
			case GAME_READY:
				leftPrompt.addAction(Actions.fadeOut(FADE_INTERVAL));
				rightPrompt.addAction(Actions.fadeOut(FADE_INTERVAL));
				state = GAME_RUNNING;
				//break;
			
			case GAME_RUNNING:
				
				if (point.x < SCREEN_WIDTH / 2) {
					player.getBody().setLinearVelocity(new Vector2(-BALL_JUMP_X, BALL_JUMP_Y));
				}
				if (point.x >= SCREEN_WIDTH / 2) {
					player.getBody().setLinearVelocity(new Vector2(BALL_JUMP_X, BALL_JUMP_Y));
				}
				break;
			
			case GAME_OVER:
				if (point.dst(RETRY_X, RETRY_Y, 0) <= RETRY_WIDTH / 2) {
					stage.addAction(Actions.sequence(Actions.fadeOut(0.25f), new Action() {
						public boolean act(float delta) {
							game.setScreen(new GameScreen(game));
							return true;
						}
					}));
				}
				break;
				
			}
		//}
		
		
		return true;
	}
	
	@Override
	public boolean keyDown (int keycode) {
		
		switch (state) {
		
		case GAME_READY:
			
			if (keycode == Keys.LEFT || keycode == Keys.RIGHT) {
				leftPrompt.addAction(Actions.fadeOut(FADE_INTERVAL));
				rightPrompt.addAction(Actions.fadeOut(FADE_INTERVAL));
				state = GAME_RUNNING;
			}
			//break;
		
		case GAME_RUNNING:
			
			if (keycode == Keys.LEFT) {
				player.getBody().setLinearVelocity(new Vector2(-BALL_JUMP_X, BALL_JUMP_Y));
			}
			if (keycode == Keys.RIGHT) {
				player.getBody().setLinearVelocity(new Vector2(BALL_JUMP_X, BALL_JUMP_Y));
			}
			break;
		
		case GAME_OVER:
			if (keycode == Keys.SPACE) {
				stage.addAction(Actions.sequence(Actions.fadeOut(0.25f), new Action() {
					public boolean act(float delta) {
						game.setScreen(new GameScreen(game));
						return true;
					}
				}));
			}
			break;
			
		}
		
		return true;
	}
	
	@Override
	public void beginContact(Contact contact) {
		// TODO Auto-generated method stub
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		
		//logger.info("contact");
		if (state == GAME_RUNNING) {
			if (fixtureA.getFilterData().categoryBits == CATEGORY_PLAYER &&
					fixtureB.getFilterData().categoryBits == CATEGORY_ENEMY) {
				//fixtureA.getFilterData().maskBits = 0;
				state = GAME_OVER_SETUP;
			}
			
			else if (fixtureA.getFilterData().categoryBits == CATEGORY_ENEMY &&
					fixtureB.getFilterData().categoryBits == CATEGORY_PLAYER) {
				//fixtureB.getFilterData().maskBits = 0;
				state = GAME_OVER_SETUP;
			}
		}
		
		if ((fixtureA.isSensor() == true && fixtureB.getFilterData().categoryBits == CATEGORY_PLAYER)
				|| (fixtureB.isSensor() == true && fixtureA.getFilterData().categoryBits == CATEGORY_PLAYER)) {
			state = BALL_DESTROYED;
		}
	}
	
	@Override
	public void endContact(Contact contact) {
		//logger.info("contact");

	}
	
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
	}

}
