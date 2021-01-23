package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public abstract class DestroyableObject extends RoomObject {
    int health;
    int maxHealth;
    boolean destroyed;
    boolean itemDrop;
    boolean itemAlreadyDropped;

    protected long lastTimeDamaged;
    transient private Shader outlineShader;

    public DestroyableObject(TileMap tm){
        super(tm);

        destroyed = false;
        itemDrop = true;
        itemAlreadyDropped = false;

        outlineShader = ShaderManager.getShader("shaders\\outline");
        if (outlineShader == null){
            outlineShader = ShaderManager.createShader("shaders\\outline");
        }
    }

    @Override
    public void loadSave() {
        outlineShader = ShaderManager.getShader("shaders\\outline");
        if (outlineShader == null){
            outlineShader = ShaderManager.createShader("shaders\\outline");
        }
    }

    @Override
    public void touchEvent() {

    }

    @Override
    public void keyPress() {

    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        super.draw();

        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
                    .scale(scale)
                    .rotateY(3.14f);

        }
        Camera.getInstance().projection().mul(target,target);


        long time = System.currentTimeMillis() - lastTimeDamaged - InGame.deltaPauseTime();
        if (time < 500) {
            outlineShader.bind();
            outlineShader.setUniformi("sampler", 0);
            outlineShader.setUniformm4f("projection", target);
            int maxWidth = spriteSheetCols * width;
            int maxHeight = spriteSheetRows * height;
            outlineShader.setUniform2f("stepSize", new Vector2f(2f / maxWidth, 2f / maxHeight));
            float alpha = 1 - (float) (System.currentTimeMillis() - lastTimeDamaged - InGame.deltaPauseTime()) / 500;
            outlineShader.setUniformf("outlineAlpha", alpha);
            outlineShader.setUniform3f("color", new Vector3f(1.0f,0f,0f));

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

            outlineShader.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(0);
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }
    public void setHit(int damage){
        health-=damage;
        if(health < 0) {
            health = 0;
            destroyed = true;
        }
    }
    public boolean canDrop(){return destroyed && itemDrop && !itemAlreadyDropped; }
    public void itemDropped(){itemAlreadyDropped = true;}

}
