package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Render.Graphics.Framebuffer;

public class Fade extends Postprocess  {
    private float value;
    private boolean reverse;

    public Fade(String shader) {
        super(shader);
        value = 1;
        reverse = false;
    }
    public void update(){
        if(reverse){
            if(value < 1f){
                value+=0.01f;
            }
        } else {
            if(value > 0){
                value-=0.01f;
            } else {
                reverse=true;
            }
        }

    }

    @Override
    public void draw(Framebuffer framebuffer) {
        shader.bind();
        shader.setUniformf("value",value);
        super.draw(framebuffer);
        shader.unbind();
    }
}
