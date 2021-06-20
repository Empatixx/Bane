package cz.Empatix.Main;

import cz.Empatix.Render.Hud.SliderBar;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.*;

public class ControlSettings {
    private static ControlOption[] controls;

    private boolean choosingValue;
    private int choosingValueIndex;
    private boolean errorTwoKeys;

    private final SliderBar sliderBar;

    private float scrollY;
    private float shiftY;

    public final static int MOVE_UP = 0;
    public final static int MOVE_DOWN = 1;
    public final static int MOVE_RIGHT = 2;
    public final static int MOVE_LEFT = 3;
    public final static int SHOOT = 4;
    public final static int RELOAD = 5;
    public final static int WEAPON_SLOT1 = 6;
    public final static int WEAPON_SLOT2 = 7;
    public final static int WEAPON_DROP = 8;
    public final static int OBJECT_INTERACT = 9;
    public final static int ARTEFACT_USE = 10;
    public final static int MAP = 11;

    public ControlSettings(){
        controls = new ControlOption[12];

        sliderBar = new SliderBar(new Vector3f(1560f,630,0),3f);
        sliderBar.setLength(500);
        sliderBar.setVertical();
        sliderBar.setValue(0f);

        controls[0] = new ControlOption(GLFW.GLFW_KEY_W,"Move up");
        controls[1] = new ControlOption(GLFW.GLFW_KEY_S,"Move down");
        controls[2] = new ControlOption(GLFW.GLFW_KEY_D,"Move right");
        controls[3] = new ControlOption(GLFW.GLFW_KEY_A,"Move left");

        controls[4] = new ControlOption(GLFW.GLFW_MOUSE_BUTTON_1,"Shoot");
        controls[4].mouseKeys = true;
        controls[5] = new ControlOption(GLFW.GLFW_KEY_R,"Reload");
        controls[6] = new ControlOption(GLFW.GLFW_KEY_1,"Weapon slot 1");
        controls[7] = new ControlOption(GLFW.GLFW_KEY_2,"Weapon slot 2");
        controls[8] = new ControlOption(GLFW.GLFW_KEY_Q,"Weapon drop");

        controls[9] = new ControlOption(GLFW.GLFW_KEY_E,"Object interact");
        controls[10] = new ControlOption(GLFW.GLFW_KEY_F,"Artefact use");
        controls[11] = new ControlOption(GLFW.GLFW_KEY_TAB,"Map");
    }

    public static int getValue(int type){
        return controls[type].getValue();
    }

    private static class ControlOption{
        private final int defaultValue;
        private int value;
        private final String type;

        private boolean mouseKeys;

        private TextRender[] textRender;
        private ControlOption(int defaultValue, String type){
            this.defaultValue = value = defaultValue;
            this.type = type;
            textRender = new TextRender[2];
            for(int i = 0;i<2;i++){
                textRender[i] = new TextRender();
            }
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
    public void draw(){
        sliderBar.draw();

        for (int i = 0; i< controls.length; i++) {
            glEnable(GL_SCISSOR_TEST);
            glScissor(335,195,1250,515);

            ControlOption controlOption = controls[i];


            controlOption.textRender[0].draw(controlOption.type, new Vector3f(460, 430+i*55-shiftY, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
            if(controlOption.mouseKeys){
                String button = "MButton "+(controlOption.value+1);
                if(choosingValueIndex == i && choosingValue){
                    if (System.currentTimeMillis() / 100 % 2 == 0){
                        if(errorTwoKeys){
                            controlOption.textRender[1].draw(button, new Vector3f(1060, 430+i*55-shiftY, 0), 3, new Vector3f(1f, 0.111f, 0.149f));
                        } else {
                            controlOption.textRender[1].draw(button, new Vector3f(1060, 430+i*55-shiftY, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                        }
                    }
                } else {
                    controlOption.textRender[1].draw(button, new Vector3f(1060, 430+i*55-shiftY, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));

                }
            } else {
                if(choosingValueIndex == i && choosingValue){
                    if (System.currentTimeMillis() / 100 % 2 == 0){
                        if(errorTwoKeys){
                            controlOption.textRender[1].draw(String.valueOf((char) controlOption.value), new Vector3f(1060, 430+i*55-shiftY, 0), 3, new Vector3f(1f, 0.111f, 0.149f));
                        } else {
                            controlOption.textRender[1].draw(String.valueOf((char) controlOption.value), new Vector3f(1060, 430+i*55-shiftY, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                        }
                    }
                } else {
                    controlOption.textRender[1].draw(String.valueOf((char) controlOption.value), new Vector3f(1060, 430+i*55-shiftY, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));

                }
            }

            glDisable(GL_SCISSOR_TEST);
        }

    }
    public void update(float x, float y){
        // slider update
        if(sliderBar.isLocked()){
            sliderBar.update(x,y);
            scrollY = sliderBar.getValue();

        }

        float value = sliderBar.getValue();
        value += (scrollY - value) * 0.3f;
        if(value > 1) value = 1;
        else if (value < 0) value = 0;
        sliderBar.setValue(value);
        shiftY = value * 175;


    }
    public void mousePressed(float x, float y, int k){

        if(sliderBar.intersects(x,y)){
            sliderBar.setLocked(true);
        }
        //choosing control to change
        for(int i = 0; i< controls.length; i++){
            if(controls[i].mouseKeys) {
                if (y > 385 + i * 55 - shiftY && y < 430 + i * 55 - shiftY && x > 1045 && x < 1325 && !choosingValue) {
                    choosingValueIndex = i;
                    choosingValue = true;
                    return;
                }
            } else if(y > 385+i*55-shiftY && y < 430+i*55-shiftY  &&  x > 1045 && x < 1125 && !choosingValue){
                choosingValueIndex = i;
                choosingValue = true;
                return;
            }
        }
        sliderBar.setLocked(false);
        if(choosingValue && controls[choosingValueIndex].mouseKeys){
            controls[choosingValueIndex].value = k;
            choosingValue = false;
        }
    }
    public void mouseReleased(float x, float y){
    }

    public void mouseScroll(double x, double y) {
        float value = sliderBar.getValue();
        scrollY = value-(float)y/10;
    }
    public void keyReleased(int k){
        // changing value of control
        if(choosingValue && !controls[choosingValueIndex].mouseKeys){
            errorTwoKeys = false;
            for(int i = 0;i<controls.length;i++){
                if(k == controls[i].value && i != choosingValueIndex){
                    errorTwoKeys = true;
                }
            }
            if(!errorTwoKeys){
                choosingValue = false;
            }
            controls[choosingValueIndex].value = k;
        }
    }
    public void cancel(){
        choosingValue = false;
    }
}
