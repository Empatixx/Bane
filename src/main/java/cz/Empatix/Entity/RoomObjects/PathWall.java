package cz.Empatix.Entity.RoomObjects;


import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

import static org.lwjgl.opengl.GL11.*;

public class PathWall extends RoomObject {
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;

    private boolean reverse;
    private int direction;

    public void setDirection(int d){
        if(tileMap.isServerSide()) return;
        direction = d;
        animation.setFrames(spritesheet.getSprites(d));
    }

    public PathWall(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            collision=false;
            moveable=false;
            preDraw = false;
            reverse=false;
            height = 64;
            width = 64;
            cwidth = 65;
            cheight = 65;
            scale = 2;
            animation = new Animation(4);
            animation.setDelay(75);
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        } else {
            collision=false;
            moveable=false;
            preDraw = false;
            reverse=false;
            height = 64;
            width = 64;
            cwidth = 65;
            cheight = 65;
            scale = 2;
            spriteSheetCols=4;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\wall.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\wall.tga");
                // TOP
                Sprite[] sprites = new Sprite[4];
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
                // LEFT
                sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (1.0f+i)/spriteSheetCols,0,

                                    (float) i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,1

                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);
                // BOTTOM
                sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (1.0f+i)/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,0,

                                    (float) i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,1


                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);
                // RIGHT
                sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float)i/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,0,

                                    (float) i/spriteSheetCols,0


                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);
            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setDelay(75);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        }
    }

    public void update() {
        setMapPosition();
        if(!collision && !reverse && animation.getIndexOfFrame() == 3){
            animation.reverse();
            reverse=true;
        }
        if (collision && animation.getIndexOfFrame() != 3) {
            animation.update();

        }else if (!collision && animation.getIndexOfFrame() != 0){
            animation.update();
        }
    }
    @Override
    public void draw() {
        if (Game.displayCollisions){
            glColor3i(255,255,255);
            glBegin(GL_LINE_LOOP);
            // BOTTOM LEFT
            glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
            // TOP LEFT
            glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
            // TOP RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
            // BOTTOM RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
            glEnd();

            glPointSize(10);
            glColor3i(255,0,0);
            glBegin(GL_POINTS);
            glVertex2f(position.x+xmap,position.y+ymap);
            glEnd();


        }
        if(collision || (animation.getIndexOfFrame() != 0 && reverse)){
            super.draw();

        }

    }

    @Override
    public void touchEvent(MapObject o) {

    }
    @Override
    public void keyPress() {

    }
}
