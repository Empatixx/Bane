package cz.Empatix.Gamestates;


import cz.Empatix.Render.Camera;

abstract class GameState {

	GameStateManager gsm;
	Camera camera;

	abstract void init();
	abstract void update();
	abstract void draw();
	abstract void keyPressed(int k);
	abstract void keyReleased(int k);
	abstract void mousePressed(int button);
	abstract void mouseReleased(int button);

	GameState(Camera c){
	    this.camera = c;
    }

}
