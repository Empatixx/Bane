package cz.Empatix.Render.Postprocessing;

import cz.Empatix.Main.Game;
import cz.Empatix.Render.Graphics.Framebuffer;

public class Fade extends Postprocess  {
    private float value;

    private float increment;

    private boolean reverse;

    public Fade(String shader) {
        super(shader);
        value = 0f;
        increment = 0.09f;
    }
    public void setReverse(){
        increment = 0.09f;
        value = 55f;
        reverse = true;
    }

    /**
     *
     * @param translation defines if it should be endless translation so we make black screen to translate between gamestates
     */
    public void update(boolean translation){
        if(reverse){
            if(value > 0){
                value -= 360.69f * value/55 * Game.deltaTime;;
                if(value < 0.001f) value = 0;
            }
        } else {
            if(value < 2){
                value += 0.03f * Game.deltaTime;
                increment += Game.deltaTime;
                value += increment * Game.deltaTime;
            }
            if (translation){
                value += 0.75f * Game.deltaTime;
                increment += 30f * Game.deltaTime;
                value += increment * Game.deltaTime;
            }
        }
/*        if(reverse){
            if(firstTime == 0){
                firstTime = System.currentTimeMillis();
            } else if(System.currentTimeMillis() - firstTime > 2500){
                stop = true;
            }
            if(System.currentTimeMillis() - timer > time && !stop || transition){
                value-=6.69f * value/50;
                if (value < 0.001f){
                    value = 0;
                }
                timer=System.currentTimeMillis();
                time-=0.25f;
            }
        } else {
            if(firstTime == 0){
                firstTime = System.currentTimeMillis();
            } else if(System.currentTimeMillis() - firstTime > 2500){
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
*/
    }

    @Override
    public void draw(Framebuffer framebuffer) {
        shader.bind();
        shader.setUniformf("darkness",value);
        super.draw(framebuffer);
        shader.unbind();
    }
    public boolean isTransitionDone(){
        if(reverse) return value <= 0;
        else return value > 55;
    }
}
