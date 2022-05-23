package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Main.Game;
import cz.Empatix.Render.Graphics.Framebuffer;

public class GaussianBlur extends Postprocess {
    private float darkness;
    public GaussianBlur(String shader){
        super(shader);
        darkness = 1f;
    }

    @Override
    public void draw(Framebuffer framebuffer) {
        shader.bind();
        shader.setUniformf("darkness",darkness);
        super.draw(framebuffer);
        super.unbind();
    }
    public void update(boolean pause){
        if(pause){
            if(darkness <= 0.7f) return;
            darkness-=0.008f * Game.deltaTimeUpdate * 75;
        } else {
            darkness = 1f;
        }
    }
}
