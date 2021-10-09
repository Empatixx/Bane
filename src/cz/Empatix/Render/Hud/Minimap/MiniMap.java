package cz.Empatix.Render.Hud.Minimap;

import cz.Empatix.Java.Loader;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class MiniMap {
    public static void load(){
        Loader.loadImage("Textures\\minimap.tga");
        Loader.loadImage("Textures\\player-icon.tga");
        Loader.loadImage("Textures\\minimap-icons.tga");
    }
    private Image minimapBorders;
    private Image playerIcon;
    private int idTexture;
    private int[] vboTextures;
    private int vboVertices;
    private int pathVboVertices;
    private Shader shader;
     private Shader geometryShader;

    private boolean displayBigMap;


    private MMRoom[] rooms;


    public MiniMap(boolean serverSide){
        // only for multiplayer server side
        if(serverSide){
            rooms = new MMRoom[9];
            return;
        }
        minimapBorders = new Image("Textures\\minimap.tga",new Vector3f(1770,150,0),2);
        playerIcon = new Image("Textures\\player-icon.tga",new Vector3f(1770,150,0),1);
        rooms = new MMRoom[9];
        idTexture = glGenTextures();

        ByteBufferImage decoder = Loader.getImage("Textures\\minimap-icons.tga");
        ByteBuffer spritesheetImage = decoder.getBuffer();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);


        vboVertices = ModelManager.getModel(16,16);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(16, 16);
        }
        pathVboVertices = ModelManager.getModel(4,4);
        if (pathVboVertices == -1) {
            pathVboVertices = ModelManager.createModel(4, 4);
        }
        vboTextures = new int[5];
        for(int i = 0;i<5;i++) {
            float[] texCoords =
                    {
                            (float) i / 5, 0,
                            (float) i / 5, 1.0f,
                            (i + 1.0f) / 5, 1.0f,
                            (i + 1.0f) / 5, 0
                    };
            FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
            buffer.put(texCoords);
            buffer.flip();
            vboTextures[i] = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER,vboTextures[i]);
            glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);
        }
        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        geometryShader = ShaderManager.getShader("shaders\\geometry");
        if (geometryShader == null){
            geometryShader = ShaderManager.createShader("shaders\\geometry");
        }
    }
    public void loadSave(){
        minimapBorders = new Image("Textures\\minimap.tga",new Vector3f(1770,150,0),2);
        playerIcon = new Image("Textures\\player-icon.tga",new Vector3f(1770,150,0),1);
        idTexture = glGenTextures();

        ByteBufferImage decoder = Loader.getImage("Textures\\minimap-icons.tga");
        ByteBuffer spritesheetImage = decoder.getBuffer();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);


        vboVertices = ModelManager.getModel(16,16);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(16, 16);
        }
        pathVboVertices = ModelManager.getModel(4,4);
        if (pathVboVertices == -1) {
            pathVboVertices = ModelManager.createModel(4, 4);
        }
        vboTextures = new int[5];
        for(int i = 0;i<5;i++) {
            float[] texCoords =
                    {
                            (float) i / 5, 0,
                            (float) i / 5, 1.0f,
                            (i + 1.0f) / 5, 1.0f,
                            (i + 1.0f) / 5, 0
                    };
            FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
            buffer.put(texCoords);
            buffer.flip();
            vboTextures[i] = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER,vboTextures[i]);
            glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);
        }
        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        geometryShader = ShaderManager.getShader("shaders\\geometry");
        if (geometryShader == null){
            geometryShader = ShaderManager.createShader("shaders\\geometry");
        }
    }
    public void update(TileMap tm){
        Room room = tm.getCurrentRoom();
        int x = room.getX() - 10;
        int y = room.getY() - 10;
        if(displayBigMap){
            playerIcon.setPosition(new Vector3f(1000+x*64,500+y*64,0));
        } else {
            playerIcon.setPosition(new Vector3f(1770+x*20,150+y*20,0));

        }
    }
    public void draw() {
        if (displayBigMap) {
            minimapBorders.draw();
            shader.bind();
            shader.setUniformi("sampler", 0);
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    Matrix4f matrixPos = new Matrix4f()
                            .translate(new Vector3f(1000 + room.getX() * 64, 500 + room.getY() * 64, 0))
                            .scale(4f);
                    Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                    shader.bind();
                    shader.setUniformi("sampler", 0);
                    shader.setUniformm4f("projection", matrixPos);

                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, idTexture);

                    glEnableVertexAttribArray(0);
                    glEnableVertexAttribArray(1);


                    glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
                    glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                    glBindBuffer(GL_ARRAY_BUFFER, vboTextures[room.getType()]);
                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                    glDrawArrays(GL_QUADS, 0, 4);

                    glBindBuffer(GL_ARRAY_BUFFER, 0);

                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);

                }
            }
            shader.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE0);

            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    MMRoom[] sideRooms = room.getSideRooms();

                    Matrix4f matrixPos;

                    geometryShader.bind();
                    geometryShader.setUniform3f("color", new Vector3f(0.886f,0.6f,0.458f));
                    if(sideRooms[1] != null) {
                        if (room.isBottom() && sideRooms[1].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1000 + room.getX() * 64, 500 + room.getY() * 64 + 32, 0))
                                    .scale(4f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                    if(sideRooms[0] != null) {
                        if (room.isTop() && sideRooms[0].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1000 + room.getX() * 64, 500 + room.getY() * 64 - 32, 0))
                                    .scale(4f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                    if(sideRooms[2] != null) {
                        if (room.isLeft() && sideRooms[2].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1000 + room.getX() * 64 - 32, 500 + room.getY() * 64, 0))
                                    .scale(4f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                    if(sideRooms[3] != null) {
                        if (room.isRight() && sideRooms[3].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1000 + room.getX() * 64 + 32, 500 + room.getY() * 64, 0))
                                    .scale(4f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                }
            }
            geometryShader.unbind();

            playerIcon.draw();
        } else {

            minimapBorders.draw();
            shader.bind();
            shader.setUniformi("sampler", 0);
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    Matrix4f matrixPos = new Matrix4f()
                            .translate(new Vector3f(1770 + room.getX() * 20, 150 + room.getY() * 20, 0))
                            .scale(1.25f);
                    Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);

                    shader.setUniformm4f("projection", matrixPos);

                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, idTexture);

                    glEnableVertexAttribArray(0);
                    glEnableVertexAttribArray(1);


                    glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
                    glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                    glBindBuffer(GL_ARRAY_BUFFER, vboTextures[room.getType()]);
                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                    glDrawArrays(GL_QUADS, 0, 4);

                    glBindBuffer(GL_ARRAY_BUFFER, 0);

                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);

                }
            }
            shader.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE0);

            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    MMRoom[] sideRooms = room.getSideRooms();

                    Matrix4f matrixPos;

                    geometryShader.bind();
                    geometryShader.setUniform3f("color", new Vector3f(0.886f,0.6f,0.458f));
                    if(sideRooms[1] != null) {
                        if (room.isBottom() && sideRooms[1].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1770 + room.getX() * 20, 150 + room.getY() * 20 + 10, 0))
                                    .scale(1.25f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                    if(sideRooms[0] != null) {
                        if (room.isTop() && sideRooms[0].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1770 + room.getX() * 20, 150 + room.getY() * 20 - 10, 0))
                                    .scale(1.25f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                    if(sideRooms[2] != null) {
                        if (room.isLeft() && sideRooms[2].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1770 + room.getX() * 20 - 10, 150 + room.getY() * 20, 0))
                                    .scale(1.25f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                    if(sideRooms[3] != null) {
                        if (room.isRight() && sideRooms[3].isDiscovered()) {
                            matrixPos = new Matrix4f()
                                    .translate(new Vector3f(1770 + room.getX() * 20 + 10, 150 + room.getY() * 20, 0))
                                    .scale(1.25f);
                            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                            geometryShader.setUniformm4f("projection", matrixPos);
                            glEnableVertexAttribArray(0);

                            glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                            glDrawArrays(GL_QUADS, 0, 4);

                            glBindBuffer(GL_ARRAY_BUFFER, 0);

                            glDisableVertexAttribArray(0);
                        }
                    }
                }
            }
            geometryShader.unbind();

            playerIcon.draw();
        }
    }
    public void addRoom(MMRoom room, int number){
        rooms[number] = room;
    }

    public void keyPressed(int k){
        if(k == ControlSettings.getValue(ControlSettings.MAP)){
            displayBigMap = true;
            minimapBorders.setScale(7f);
            minimapBorders.setPosition(new Vector3f(1000,500,0));

            playerIcon.setScale(3f);
            playerIcon.setPosition(new Vector3f(1000,500,0));
        }
    }
    public void keyReleased(int k){
        if(k == ControlSettings.getValue(ControlSettings.MAP)){
            displayBigMap = false;
            minimapBorders.setScale(2);
            minimapBorders.setPosition(new Vector3f(1770,150,0));

            playerIcon.setScale(1f);
            playerIcon.setPosition(new Vector3f(1770,150,0));

        }
    }
}
