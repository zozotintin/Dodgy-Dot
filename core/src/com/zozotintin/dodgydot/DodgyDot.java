package com.zozotintin.dodgydot;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class DodgyDot extends Game implements Configuration{
	Batch batch;
	
	//Box2DDebugRenderer debugRenderer;
	BitmapFont font;
	
	Preferences pref;
	
	Label.LabelStyle labelStyle;
	Label highScoreWordsLabel;

	int highScore;
	
	@Override
	public void create () {
		pref = Gdx.app.getPreferences("DATA");
		highScore = pref.getInteger("Score", -1);
		if (highScore == -1) {
			highScore = 0;
			pref.putInteger("Score", 0);
			pref.flush();
		}
		
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("ChicagoFLF.fnt"));
		//font.setScale(5f);
		labelStyle = new Label.LabelStyle(font, new Color(124 / 255.0f, 199 / 255.0f, 72 / 255.0f, 1));

		highScoreWordsLabel = new  Label("Score:\n\nHigh Score:", labelStyle);
		highScoreWordsLabel.setFontScale(0.7f);
		highScoreWordsLabel.setHeight((float) highScoreWordsLabel.getHeight() * 0.7f);
		highScoreWordsLabel.setPosition(0, VIRTUAL_HEIGHT - highScoreWordsLabel.getHeight());
		highScoreWordsLabel.setAlignment(Align.left);
		//debugRenderer = new Box2DDebugRenderer();
		Assets.load();
		this.setScreen(new GameScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
