package cz.Empatix.Entity.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.*;

public class Crystal extends RoomObject {
    public Crystal(TileMap tm) {
        super(tm);
        if (tm.isServerSide()) {
            width = 64;
            height = 64;
            cwidth = 64;
            cheight = 64;
            scale = 5;

            facingRight = true;
            flinching = false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            collision = false;
            moveable = false;
            preDraw = true;

            animation = new Animation(12);
            animation.setDelay(140);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            remove = false;
        } else {
            width = 64;
            height = 64;
            cwidth = 64;
            cheight = 64;
            scale = 5;

            facingRight = true;
            flinching = false;

            spriteSheetCols = 12;
            spriteSheetRows = 1;

            collision = false;
            moveable = false;
            preDraw = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\crystal.tga");

            // creating a new spritesheet
            if (spritesheet == null) {
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\crystal.tga");
                Sprite[] sprites = new Sprite[12];
                for (int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float) i / spriteSheetCols, 0,

                                    (float) i / spriteSheetCols, 1,

                                    (1.0f + i) / spriteSheetCols, 1,

                                    (1.0f + i) / spriteSheetCols, 0
                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

            }
            vboVertices = ModelManager.getModel(width, height);
            if (vboVertices == -1) {
                vboVertices = ModelManager.createModel(width, height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(0));
            animation.setDelay(140);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null) {
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            remove = false;
        }
    }

    public void update() {
        setMapPosition();

        animation.update();
    }

    @Override
    public void touchEvent(MapObject o) {
    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()) {
            return;
        }

        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
                    .scale(scale)
                    .rotateY(3.14f);

        }
        Camera.getInstance().projection().mul(target, target);

        shader.bind();
        shader.setUniformi("sampler", 0);
        shader.setUniformm4f("projection", target);
        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);


        glBindBuffer(GL_ARRAY_BUFFER, animation.getFrame().getVbo());
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(0);
        if (Game.displayCollisions) {
            glColor3i(255, 255, 255);
            glBegin(GL_LINE_LOOP);
            // BOTTOM LEFT
            glVertex2f(position.x + xmap - cwidth / 2, position.y + ymap - cheight / 2);
            // TOP LEFT
            glVertex2f(position.x + xmap - cwidth / 2, position.y + ymap + cheight / 2);
            // TOP RIGHT
            glVertex2f(position.x + xmap + cwidth / 2, position.y + ymap + cheight / 2);
            // BOTTOM RIGHT
            glVertex2f(position.x + xmap + cwidth / 2, position.y + ymap - cheight / 2);
            glEnd();

            glPointSize(10);
            glColor3i(255, 0, 0);
            glBegin(GL_POINTS);
            glVertex2f(position.x + xmap, position.y + ymap);
            glEnd();


        }
    }

    public boolean shouldRemove() {
        return remove;
    }

    @Override
    public void keyPress() {

    }
}
