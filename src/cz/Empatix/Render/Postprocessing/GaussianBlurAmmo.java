package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Render.Graphics.Framebuffer;
import org.joml.Vector2f;

public class GaussianBlurAmmo extends Postprocess {
    private float value;
    private Vector2f ratio;

    public GaussianBlurAmmo(String shader){
        super(shader);
        ratio = new Vector2f();
    }

    @Override
    public void draw(Framebuffer framebuffer) {
        shader.bind();
        shader.setUniformf("value",value);
        shader.setUniform2f("ratio",ratio);
        super.draw(framebuffer);
        super.unbind();
    }
    public void update(){
        if(value > 2){
            value-=-10f/(2+10-value)+6;
        }
    }
    public void setValue(float value){
        this.value = value;
    }

    public void setRatio(float x, float y) {
        ratio.x = x;
        ratio.y = y;
    }
}
