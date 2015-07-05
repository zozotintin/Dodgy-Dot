package com.zozotintin.dodgydot.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.zozotintin.dodgydot.DodgyDot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = (int) (config.getDesktopDisplayMode().height * 0.8f);
		config.width = (config.height * 1080 / 1728);
		
		new LwjglApplication(new DodgyDot(), config);
	}
}
