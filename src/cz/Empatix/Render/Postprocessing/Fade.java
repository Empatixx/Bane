package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Render.Graphics.Framebuffer;

public class Fade extends Postprocess  {
    private float value;

    private long timer;
    private float time;

    private boolean stop;
    private long firstTime;

    private float increment;

    private boolean reverse;

    public Fade(String shader) {
        super(shader);
        value = 0f;
        time = 75f;
        stop = false;
        increment = 0.09f;
    }
    public void setReverse(){
        value = 50f;
        time = 15;
        stop = false;
        reverse = true;
    }
    public void update(boolean transition){
        if(reverse){
            if(firstTime == 0){
                firstTime = System.currentTimeMillis();
            } else if(System.currentTimeMillis() - firstTime > 3500){
                stop = true;
            }
            if(System.currentTimeMillis() - timer > time && !stop || transition){
                value-=4.69f * value/50;
                if (value < 0.001f){
                    value = 0;
                }
                timer=System.currentTimeMillis();
                time-=0.25f;
            }
        } else {
            if(firstTime == 0){
                firstTime = System.currentTimeMillis();
            } else if(System.currentTimeMillis() - firstTime > 3500){
                stop = true;
            }
            if(System.currentTimeMillis() - timer > time && !stop || transition){
                value+=0.045f;
                if(transition){
                    increment+=0.02f;
                    value+=increment;
                }

                timer=System.currentTimeMillis();
                time-=0.5f;
            }
        }

    }

    @Override
    public void draw(Framebuffer framebuffer) {
        shader.bind();
        shader.setUniformf("darkness",value);
        super.draw(framebuffer);
        shader.unbind();
    }
    public boolean isTransitionDone(){return value <= 0 || value > 50;}
}
