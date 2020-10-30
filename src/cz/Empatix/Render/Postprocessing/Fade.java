package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Render.Graphics.Framebuffer;

public class Fade extends Postprocess  {
    private float value;

    private long timer;
    private float time;

    private boolean stop;
    private long firstTime;

    public Fade(String shader) {
        super(shader);
        value = 0f;
        time = 75f;
        stop = false;
    }
    public void update(){
        if(firstTime == 0){
            firstTime = System.currentTimeMillis();
        } else if(System.currentTimeMillis() - firstTime > 3500){
            stop = true;
        }
        if(System.currentTimeMillis() - timer > time && !stop){
            value+=0.045f;
            timer=System.currentTimeMillis();
            time-=0.5f;
        }

    }

    @Override
    public void draw(Framebuffer framebuffer) {
        shader.bind();
        shader.setUniformf("darkness",value);
        super.draw(framebuffer);
        shader.unbind();
    }
}
