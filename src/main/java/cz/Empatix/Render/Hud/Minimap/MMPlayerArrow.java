package cz.Empatix.Render.Hud.Minimap;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Player;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

public class MMPlayerArrow {
    private Vector3f originalPos;
    double angle;
    private Spritesheet spritesheet;
    private int vboVertices;
    protected Shader shader;
    protected Animation animation;
    private Vector3f position;

    private final Player followedPlayer; // player that is followed by arrow

    public MMPlayerArrow(PlayerMP player){
        this.followedPlayer = player;
        int width = 32;
        int height = 32;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\mmparrow.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\mmparrow.tga");
            Sprite[] sprites = new Sprite[1];
            Sprite sprite = new Sprite(new float[]
                    {
                            0f, 0,

                            0f, 1,

                            1f, 1,

                            1f, 0
                    }
            );
            sprites[0] = sprite;
            spritesheet.addSprites(sprites);
        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }
        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(0));
        animation.setDelay(145);

        position = new Vector3f();
        originalPos = new Vector3f();

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

    }
    public void update(TileMap tm){
        animation.update();
        float y = followedPlayer.getY() + tm.getY() - 500;
        float x = followedPlayer.getX() + tm.getX() - 960;
        angle = (float)Math.atan2(y,x);
        position.x = 960  + 400 *(float)Math.cos(angle);
        position.y = 500 + 400 *(float)Math.sin(angle);
    }
    public void draw() {
        if(!followedPlayer.isNotOnScrean()) return;
        Matrix4f target;
        target = new Matrix4f().translate(position)
                .scale(4)
                .rotateZ((float)angle);

        Camera.getInstance().hardProjection().mul(target,target);

        shader.bind();
        shader.setUniformi("sampler",0);
        glActiveTexture(GL_TEXTURE0);
        shader.setUniformm4f("projection",target);

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

    }

    public boolean isThisHim(int idCon) {
        return followedPlayer.id == idCon;
    }
}
