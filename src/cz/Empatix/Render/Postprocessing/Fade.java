package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Render.Graphics.Framebuffer;

public class Fade extends Postprocess  {
    private float value;

    private long timer;
    private float time;

    public Fade(String shader) {
        super(shader);
        value = 1f;
        time = 75f;
    }
    public void update(){
        if(System.currentTimeMillis() - timer > time){
            value-=0.045f;
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
