package cz.Empatix.Gamestates;



abstract class GameState {

	transient GameStateManager gsm;

	abstract void init();
	abstract void update();
	abstract void draw();
	abstract void keyPressed(int k);
	abstract void keyReleased(int k);
	abstract void mousePressed(int button);
	abstract void mouseReleased(int button);
	abstract void mouseScroll(double x, double y);
}
