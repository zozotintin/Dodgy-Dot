package com.zozotintin.dodgydot;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

class Assets {
	public static TextureAtlas atlas;

	public static TextureRegion ball;
	public static TextureRegion retry;
	public static TextureRegion background;
	public static TextureRegion[] enemies = new TextureRegion[3];
	public static TextureRegion prompt[] = new TextureRegion[2];
		
	public static void load() {
		atlas = new TextureAtlas("sheet1.txt");

		ball = atlas.findRegion("ball");
		retry = atlas.findRegion("retry");
		background = atlas.findRegion("background");
		enemies[0] = atlas.findRegion("enemy1");
		enemies[1] = atlas.findRegion("enemy2");
		enemies[2] = atlas.findRegion("enemy3");
		prompt[0] = atlas.findRegion("tap_here");
		prompt[1] = atlas.findRegion("or_here");
		
		
	}
}
