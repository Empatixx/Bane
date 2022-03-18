package cz.Empatix.Render.Hud;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Main.KeyboardInput;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Utility.CopyImagetoClipBoard;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL20.*;

public class InputBar {

    private final float minX;
    private final float maxX;

    private final float minY;
    private final float maxY;

    private int type;

    private Shader shader;
    private float scale;
    private Animation animation;
    private Spritesheet spritesheet;
    int vboVertices;
    private Matrix4f matrixPos;
    private Vector3f pos;

    private boolean click;

    private boolean enabled;
    StringBuilder stringbuilder;
    private boolean dot;
    private long time;

    private int[] keys;
    private boolean[] used;
    private long writeDelay;
    private long globalDelay;

    private TextRender[] textRender;
    private String title;


    /**
     *
     * @param file - path of texture
     * @param pos - position of menu bar
     * @param scale - scaling of texture(width*scale,height*scale)
     * @param width - width on screen
     * @param height- height on screen
     * @param title - name of input bar
     */
    public InputBar(String file, Vector3f pos, float scale, int width, int height, String title){
        this.pos = pos;
        this.title = title;
        this.scale = scale;

        minX = (int)pos.x-width*scale/2;
        minY = (int)pos.y-height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        spritesheet = SpritesheetManager.getSpritesheet(file);
        if(spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet(file);
        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }
        Sprite[] sprites = new Sprite[2];
        for(int i = 0;i<2;i++) {
            float[] texCoords =
                    {
                            0, i * 0.5f,
                            0, 0.5f + i * 0.5f,
                            1, 0.5f + i * 0.5f,
                            1, i * 0.5f
                    };
            sprites[i] = new Sprite(texCoords);

        }
        matrixPos = new Matrix4f()
                .translate(pos)
                .scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        stringbuilder = new StringBuilder();
        time = System.currentTimeMillis();
        dot = true;

        keys = new int[10];
        used = new boolean[10];

        textRender = new TextRender[2];
        for(int i = 0;i < textRender.length;i++){
            textRender[i] = new TextRender();
        }

        animation = new Animation();
        animation.setDelay(100);
        animation.setFrames(sprites);

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void addChar(char c){
        stringbuilder.append(c);
    }
    public void keyReleased(int k){
        for(int i = 0; i< keys.length; i++){
            if(k == keys[i]) used[i] = false;
        }
    }
    public void setDefaultValue(String s){
        stringbuilder = new StringBuilder(s);
    }

    public boolean isEmpty(){
        return stringbuilder.length() == 0;
    }
    public void keyPressed(int k){
        if(enabled) {
            if(k == GLFW.GLFW_KEY_ENTER){
                enabled = false;
                return;
            }
            for(int i = 0; i< keys.length; i++){
                if(!used[i]){
                    if((k == GLFW.GLFW_KEY_C && keys[i] == GLFW.GLFW_KEY_LEFT_CONTROL) || (k == GLFW.GLFW_KEY_LEFT_CONTROL && keys[i] == GLFW.GLFW_KEY_C)){
                        CopyImagetoClipBoard copyImagetoClipBoard = new CopyImagetoClipBoard();
                        String copy = copyImagetoClipBoard.getStringCopy();
                        if(copy != null) stringbuilder.append(copy);
                        if(stringbuilder.length() > 20){
                            stringbuilder.delete(21,stringbuilder.length());
                        }
                        return;
                    }
                    keys[i] = k;
                    used[i] = true;
                    if ((k >= '0' && k <= '9') || (k >= 'a' && k <= 'z') || (k >= 'A' && k <= 'Z') || k == ':' || k == '.') {
                        boolean caps = KeyboardInput.capsLock;
                        if(caps){
                            if (stringbuilder.length() <= 20) addChar((char)k);
                        } else {
                            if (stringbuilder.length() <= 20) addChar(Character.toLowerCase((char)k));
                        }
                    }
                    if(k == GLFW.GLFW_KEY_BACKSPACE){
                        if (stringbuilder.length() >= 1) stringbuilder.setLength(stringbuilder.length() - 1);
                    }
                    globalDelay = System.currentTimeMillis();
                    return;
                } else {
                    if((k == GLFW.GLFW_KEY_C && keys[i] == GLFW.GLFW_KEY_LEFT_CONTROL) || (k == GLFW.GLFW_KEY_LEFT_CONTROL && keys[i] == GLFW.GLFW_KEY_C)){
                        CopyImagetoClipBoard copyImagetoClipBoard = new CopyImagetoClipBoard();
                        String copy = copyImagetoClipBoard.getStringCopy();
                        if(copy != null) stringbuilder.append(copy);
                        if(stringbuilder.length() > 20){
                            stringbuilder.delete(21,stringbuilder.length());
                        }
                        used[i] = false;
                        return;
                    }
                }
            }
        }
    }

    public void draw(){
        shader.bind();
        shader.setUniformm4f("projection",matrixPos);
        shader.setUniformi("sampler",0);
        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());

        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);

        String text = stringbuilder.toString();
        float centerX = TextRender.getHorizontalCenter((int)minX,(int)maxX,text,2);
        textRender[0].draw(text,new Vector3f(centerX,pos.y+17.5f*scale,0),2,new Vector3f(0,0,0));
        float centerTitleX = TextRender.getHorizontalCenter((int)minX,(int)maxX,title,2);
        textRender[1].draw(title,new Vector3f(centerTitleX,pos.y-25*scale,0),2,new Vector3f(0.874f,0.443f,0.149f));
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
    public boolean intersects(float x, float y){
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    public void setClick(boolean click) {
        this.click = click;
    }
    public void update(){
        if(click)animation.update();
        else animation.setFrame(0);

        if(System.currentTimeMillis() - writeDelay > 70 && System.currentTimeMillis() - globalDelay > 400){
            writeDelay = System.currentTimeMillis();
            for(int i = 0; i< keys.length; i++){
                if(keys[i] == GLFW.GLFW_KEY_BACKSPACE && used[i]){
                    if (stringbuilder.length() >= 1) stringbuilder.setLength(stringbuilder.length() - 1);
                    return;
                }
            }
            for(int i = 0;i<keys.length;i++){
                if(used[i]){
                    int k = keys[i];
                    if ((k >= '0' && k <= '9') || (k >= 'a' && k <= 'z') || (k >= 'A' && k <= 'Z') || k == ' ') {
                        if (stringbuilder.length() <= 20){
                            boolean caps = KeyboardInput.capsLock;
                            if(caps){
                                addChar((char)k);
                            } else {
                                addChar(Character.toLowerCase((char)k));
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isClick() {
        return click;
    }
    public String getValue(){return stringbuilder.toString();}

    public void clearKeys() {
        for(int i = 0;i<used.length;i++){
            used[i] = false;
        }
    }
}
