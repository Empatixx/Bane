package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class Ladder extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\ladder.tga");
        Loader.loadImage("Textures\\arrowpointer.tga");
    }
    transient private Spritesheet spritesheetArrowPointer;
    transient private Animation animationPointer;

    transient private TextRender textRender;

    public Ladder(TileMap tm){
        super(tm);
        width = 32;
        height = 32;
        cwidth = 16;
        cheight = 16;
        scale = 4;

        facingRight = true;
        flinching=false;

        spriteSheetCols = 1;
        spriteSheetRows = 1;

        collision = false;
        moveable=false;
        preDraw = true;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\ladder.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\ladder.tga");
            Sprite[] sprites = new Sprite[1];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

        }

        // try to find spritesheet if it was created once
        spritesheetArrowPointer = SpritesheetManager.getSpritesheet("Textures\\arrowpointer.tga");

        // creating a new spritesheet
        if (spritesheetArrowPointer == null){
            spritesheetArrowPointer = SpritesheetManager.createSpritesheet("Textures\\arrowpointer.tga");
            Sprite[] sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/4,0,

                                (float)i/4,1,

                                (1.0f+i)/4,1,

                                (1.0f+i)/4,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheetArrowPointer.addSprites(sprites);

        }

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(0));
        animation.setDelay(-1);

        animationPointer = new Animation();
        animationPointer.setFrames(spritesheetArrowPointer.getSprites(0));
        animationPointer.setDelay(100);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        textRender = new TextRender();
    }

    @Override
    public void loadSave() {
        width = 32;
        height = 32;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\ladder.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\ladder.tga");
            Sprite[] sprites = new Sprite[1];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

        }

        // try to find spritesheet if it was created once
        spritesheetArrowPointer = SpritesheetManager.getSpritesheet("Textures\\arrowpointer.tga");

        // creating a new spritesheet
        if (spritesheetArrowPointer == null){
            spritesheetArrowPointer = SpritesheetManager.createSpritesheet("Textures\\arrowpointer.tga");
            Sprite[] sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/4,0,

                                (float)i/4,1,

                                (1.0f+i)/4,1,

                                (1.0f+i)/4,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheetArrowPointer.addSprites(sprites);

        }

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(0));
        animation.setDelay(-1);

        animationPointer = new Animation();
        animationPointer.setFrames(spritesheetArrowPointer.getSprites(0));
        animationPointer.setDelay(100);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;

        textRender = new TextRender();
    }

    public void update(){
        setMapPosition();
        checkTileMapCollision();

        animation.update();
        animationPointer.update();
    }

    @Override
    public void touchEvent(MapObject o) {
    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        super.draw();
        if (isNotOnScrean()){
            return;
        }

        Matrix4f target;

        Vector3f posPointer = new Vector3f(position);
        posPointer.y-=tileSize;
        target = new Matrix4f().translate(posPointer)
                    .scale(scale);
        Camera.getInstance().projection().mul(target,target);

        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",target);
        glActiveTexture(GL_TEXTURE0);
        spritesheetArrowPointer.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1 );


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);


        glBindBuffer(GL_ARRAY_BUFFER,animationPointer.getFrame().getVbo());
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
        float time = (float)Math.sin(System.currentTimeMillis() % 2000 / 600f)+(1-(float)Math.cos((System.currentTimeMillis() % 2000 / 600f) +0.5f));

        textRender.drawMap("Press E to enter next layer",new Vector3f(position.x-185,position.y+100,0),2,
                new Vector3f((float)Math.sin(time),(float)Math.cos(0.5f+time),1f));
    }
    public boolean shouldRemove(){
        return remove;
    }
    @Override
    public void keyPress() {
        if(remove) return;
        tileMap.newMap();
        remove = true;
    }
}
