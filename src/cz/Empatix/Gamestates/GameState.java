package cz.Empatix.Gamestates;



public abstract class GameState {

	protected GameStateManager gsm;

	protected abstract void init();
	protected abstract void update();
	protected abstract void draw();
	protected abstract void keyPressed(int k);
	protected abstract void keyReleased(int k);
	protected abstract void mousePressed(int button);
	protected abstract void mouseReleased(int button);
	protected abstract void mouseScroll(double x, double y);
}
