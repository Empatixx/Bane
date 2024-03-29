package cz.Empatix.Render.Hud.Minimap;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class MiniMap {
    private Image minimapBorders;
    private int idTexture;
    private int[] vboTextures;
    private int vboVertices;

    private int vboTexturesTrans;
    private int idTextureTrans;
    private int pathVboVertices;

    private Shader shader;
    private Shader geometryShader;

    private boolean displayBigMap;

    private MMRoom[] rooms;

    private Animation playerIconAnimation;
    private Spritesheet playerIcon;
    private Vector3f playerIconPos;
    private MMPlayerArrow[] playerArrows;

    private Spritesheet affixesSpritesheet;
    private int affixesVboVertices;
    private TextRender[] textRenderAffixes;
    private byte[] affixes;
    private int totalAffixes;
    private String[] affixesNames;

    private int[] xMin,xMax,yMin,yMax;
    private TextRender affixDesc;
    private byte hoverAffix;

    public MiniMap(boolean serverSide){
        // only for multiplayer server side
        if(serverSide){
            rooms = new MMRoom[9];
            return;
        }
        minimapBorders = new Image("Textures\\minimap.tga",new Vector3f(1770,150,0),2);
        //playerIcon = new Image("Textures\\player-icon.tga",new Vector3f(1770,150,0),1);
        playerIcon = SpritesheetManager.getSpritesheet("Textures\\player-icon.tga");
        if(playerIcon == null){
            playerIcon=SpritesheetManager.createSpritesheet("Textures\\player-icon.tga");
            Sprite[] sprites = new Sprite[2];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/2,0,

                                (float)i/2,1f,

                                (1.0f+i)/2,1f,

                                (1.0f+i)/2,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            playerIcon.addSprites(sprites);
        }
        playerIconAnimation = new Animation();
        playerIconAnimation.setFrames(playerIcon.getSprites(0));
        playerIconAnimation.setDelay(200);

        playerIconPos = new Vector3f();

        rooms = new MMRoom[9];
        // icons
        idTexture = glGenTextures();
        ByteBufferImage decoder = Loader.getImage("Textures\\minimap-icons.tga");
        ByteBuffer spritesheetImage = decoder.getBuffer();
        glBindTexture(GL_TEXTURE_2D, idTexture);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        int width = decoder.getWidth();
        int height = decoder.getHeight();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        // transitions
        decoder = Loader.getImage("Textures\\minimap-trans.tga");
        spritesheetImage = decoder.getBuffer();
        idTextureTrans = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, idTextureTrans);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        width = decoder.getWidth();
        height = decoder.getHeight();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        vboVertices = ModelManager.getModel(16,16);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(16, 16);
        }
        pathVboVertices = ModelManager.getModel(4,4);
        if (pathVboVertices == -1) {
            pathVboVertices = ModelManager.createModel(4, 4);
        }
        vboTextures = new int[9];
        for(int i = 0;i<vboTextures.length;i++) {
            float[] texCoords =
                    {
                            (float) i / vboTextures.length, 0,
                            (float) i / vboTextures.length, 1.0f,
                            (i + 1.0f) / vboTextures.length, 1.0f,
                            (i + 1.0f) / vboTextures.length, 0
                    };
            FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
            buffer.put(texCoords);
            buffer.flip();
            vboTextures[i] = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER,vboTextures[i]);
            glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);
        }
        float[] texCoords =
                    {
                            (float) 0, 0,
                            (float) 0 , 1.0f,
                            (1.0f) , 1.0f,
                            (1.0f), 0
                    };


        FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTexturesTrans = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTexturesTrans);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        geometryShader = ShaderManager.getShader("shaders\\geometry");
        if (geometryShader == null){
            geometryShader = ShaderManager.createShader("shaders\\geometry");
        }
        playerIconPos.x = 1770;
        playerIconPos.y = 150;

        playerArrows = new MMPlayerArrow[1];

        int totalAffixes = 5;
        textRenderAffixes = new TextRender[3];
        for (int i = 0; i < textRenderAffixes.length; i++) {
            textRenderAffixes[i] = new TextRender();
        }
        // try to find spritesheet if it was created once
        affixesSpritesheet = SpritesheetManager.getSpritesheet("Textures\\affixes.tga");

        // creating a new spritesheet
        if (affixesSpritesheet == null) {
            affixesSpritesheet = SpritesheetManager.createSpritesheet("Textures\\affixes.tga");
            Sprite[] sprites = new Sprite[totalAffixes];
            for (int i = 0; i < sprites.length; i++) {
                texCoords =
                        new float[]{
                                (float) i / totalAffixes, 0,

                                (float) i / totalAffixes, 1,

                                (1.0f + i) / totalAffixes, 1,

                                (1.0f + i) / totalAffixes, 0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            affixesSpritesheet.addSprites(sprites);
        }
        affixesVboVertices = ModelManager.getModel(32, 32);
        if (affixesVboVertices == -1) {
            affixesVboVertices = ModelManager.createModel(32, 32);
        }
        affixesNames = new String[3];
        yMin = new int[3];
        yMax = new int[3];
        xMin = new int[3];
        xMax = new int[3];
        for(int i = 0;i<3;i++){
            xMin[i] = 1420;
            xMax[i] = 1580;
            yMin[i] = 220 + i*200;
            yMax[i] = 380 + i*200;
        }
        affixDesc = new TextRender();

    }
    public boolean intersects(float x, float y, int i){
        return (x >= xMin[i] && x <= xMax[i] && y >= yMin[i] && y <= yMax[i]);
    }
    public void hover(float mouseX, float mouseY){
        hoverAffix = -1;
        for(int i = 0;i<totalAffixes;i++){
            if(intersects(mouseX,mouseY,i)){
                hoverAffix = affixes[i];
            }
        }
    }

    public void update(TileMap tm){
        Room room = tm.getCurrentRoom();
        int x = room.getX() - 10;
        int y = room.getY() - 10;
        if(displayBigMap){
            playerIconPos.x = 960+x*64;
            playerIconPos.y = 500+y*64;
            affixes = tm.getAffixes();
            totalAffixes = tm.getTotalAffixes();
            for(int i = 0;i<totalAffixes;i++){
                affixesNames[i] = tm.affixConvertToString(affixes[i]);
            }
        } else {
            playerIconPos.x = 1770+x*20;
            playerIconPos.y = 150+y*20;
        }
        playerIconAnimation.update();
    }
    public void update(PlayerMP[] players,TileMap tm){
        for(PlayerMP player : players){
            if(player != null){
                Room room = tm.getRoomByCoords(player.getX(),player.getY());
                if(room != null){
                    if(room.getType() == Room.Shop || room.getType() == Room.Loot){
                        MMRoom mmRoom = room.getMinimapRoom();
                        if(!mmRoom.isEntered()){
                            mmRoom.entered();
                        }
                    }
                }
            }
        }
        if(players[1] != null) playerArrows[0].update(tm);
    }
    // MP function that makes arrow that follows defined player
    public void addPlayerArrow(PlayerMP player){
        for(int i = 0;i<playerArrows.length;i++){
            if(playerArrows[i] == null){
                playerArrows[i] = new MMPlayerArrow(player);
                break;
            }
        }
    }
    // MP function - clearup after disconnect
    public void removePlayerArrow(int idCon){
        for(int i = 0;i<playerArrows.length;i++){
            if(playerArrows[i] != null){
                if(playerArrows[i].isThisHim(idCon)){
                    playerArrows[i] = null;
                    break;
                }
            }
        }
    }
    public void draw() {
        minimapBorders.draw();
        if (displayBigMap) {
            if(hoverAffix != -1){
                String desc = null;
                switch(hoverAffix){
                    case TileMap.BOOSTHP:{
                        desc = "Health of non-boss enemies is increased by 40%";
                        break;
                    }
                    case TileMap.ENEMYREGEN:{
                        desc = "Enemies have got regeneration 2% hp per 5 seconds";
                        break;
                    }
                    case TileMap.INFLATION:{
                        desc = "Shop prices are increased by 100%";
                        break;
                    }
                    case TileMap.BERSERKS:{
                        desc = "Enemies below 20% health are 30% faster";
                        break;
                    }
                    case TileMap.LOWHPPOTS:{
                        desc = "Drop rate for health potions is drastically reduced";
                        break;
                    }
                }
                float x = TextRender.getHorizontalCenter(860,1060,desc,2);
                affixDesc.draw(desc,new Vector3f(x,900,0),2,
                        new Vector3f(0.6784f,0.2901f,0.0941f));
            }
            for(int i = 0;i<totalAffixes;i++){
                Matrix4f target;
                target = new Matrix4f().translate(new Vector3f(1500,300+i*200,0)).scale(5);
                float x = TextRender.getHorizontalCenter(1500-50,1500+50,affixesNames[i],2);
                textRenderAffixes[i].draw(affixesNames[i],new Vector3f(x,225+i*200,0),2,
                            new Vector3f(0.9686f,0.2f,0.09803f));

                shader.bind();
                shader.setUniformi("sampler",0);
                glActiveTexture(GL_TEXTURE0);
                affixesSpritesheet.bindTexture();
                Camera.getInstance().hardProjection().mul(target,target);

                shader.setUniformm4f("projection",target);

                glEnableVertexAttribArray(0);
                glEnableVertexAttribArray(1);

                glBindBuffer(GL_ARRAY_BUFFER, affixesVboVertices);
                glVertexAttribPointer(0,2,GL_INT,false,0,0);

                glBindBuffer(GL_ARRAY_BUFFER,affixesSpritesheet.getSprites(0)[affixes[i]].getVbo());
                glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

                glDrawArrays(GL_QUADS, 0, 4);

                glBindBuffer(GL_ARRAY_BUFFER,0);

                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);
            }
            shader.unbind();
            glBindTexture(GL_TEXTURE_2D,0);
            glActiveTexture(0);

            shader.bind();
            shader.setUniformi("sampler", 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, idTexture);
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    drawRoom(room,960 + room.getX() * 64,500 + room.getY() * 64,4);
                }
            }
            //shader.bind();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, idTextureTrans);
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    MMRoom[] sideRooms = room.getSideRooms();
                    if(sideRooms[1] != null) {
                        if (room.isBottom()) {
                            drawPathUnentered(sideRooms[1],960 + room.getX() * 64,500 + room.getY() * 64 + 32,4,0);
                        }
                    }
                    if(sideRooms[0] != null) {
                        if (room.isTop()) {
                            drawPathUnentered(sideRooms[0],960 + room.getX() * 64,500 + room.getY() * 64 - 32,4,1);
                        }
                    }
                    if(sideRooms[2] != null) {
                        if (room.isLeft()) {
                            drawPathUnentered(sideRooms[2],960 + room.getX() * 64 - 32,500 + room.getY() * 64 ,4,2);
                        }
                    }
                    if(sideRooms[3] != null) {
                        if (room.isRight()) {
                            drawPathUnentered(sideRooms[3],960 + room.getX() * 64 + 32,500 + room.getY() * 64,4,3);
                        }
                    }
                }
            }
            shader.unbind();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
            geometryShader.bind();
            geometryShader.setUniform3f("color", new Vector3f(0.886f,0.6f,0.458f));
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    MMRoom[] sideRooms = room.getSideRooms();
                    if(sideRooms[1] != null) {
                        if (room.isBottom()) {
                            drawPathEntered(sideRooms[1],960 + room.getX() * 64,500 + room.getY() * 64 + 32,4,0);

                        }
                    }
                    if(sideRooms[0] != null) {
                        if (room.isTop()) {
                            drawPathEntered(sideRooms[0],960 + room.getX() * 64,500 + room.getY() * 64 - 32,4,1);
                        }
                    }
                    if(sideRooms[2] != null) {
                        if (room.isLeft()) {
                            drawPathEntered(sideRooms[2],960 + room.getX() * 64 - 32,500 + room.getY() * 64,4,2);
                        }
                    }
                    if(sideRooms[3] != null) {
                        if (room.isRight()) {
                            drawPathEntered(sideRooms[3],960 + room.getX() * 64 + 32,500 + room.getY() * 64,4,3);
                        }
                    }
                }
            }
            geometryShader.unbind();
        } else {
            shader.bind();
            shader.setUniformi("sampler", 0);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, idTexture);
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    drawRoom(room,1770 + room.getX() * 20,150 + room.getY() * 20,1.25f);
                }
            }
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, idTextureTrans);
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    MMRoom[] sideRooms = room.getSideRooms();
                    if(sideRooms[1] != null) {
                        if (room.isBottom()) {
                            drawPathUnentered(sideRooms[1],1770 + room.getX() * 20, 150 + room.getY() * 20 + 10,1.5f,0);
                        }
                    }
                    if(sideRooms[0] != null) {
                        if (room.isTop()) {
                            drawPathUnentered(sideRooms[0],1770 + room.getX() * 20, 150 + room.getY() * 20 - 10,1.5f,1);
                        }
                    }
                    if(sideRooms[2] != null) {
                        if (room.isLeft()) {
                            drawPathUnentered(sideRooms[2],1770 + room.getX() * 20 - 10, 150 + room.getY() * 20,1.5f,2);
                        }
                    }
                    if(sideRooms[3] != null) {
                        if (room.isRight()) {
                            drawPathUnentered(sideRooms[3],1770 + room.getX() * 20 + 10, 150 + room.getY() * 20,1.5f,3);
                        }
                    }
                }
            }
            shader.unbind();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, 0);
            geometryShader.bind();
            geometryShader.setUniform3f("color", new Vector3f(0.886f,0.6f,0.458f));
            for (MMRoom room : rooms) {
                if (room.isDiscovered()) {
                    MMRoom[] sideRooms = room.getSideRooms();
                    if(sideRooms[1] != null) {
                        if (room.isBottom()) {
                            drawPathEntered(sideRooms[1],1770 + room.getX() * 20, 150 + room.getY() * 20 + 10,1.5f,0);
                        }
                    }
                    if(sideRooms[0] != null) {
                        if (room.isTop()) {
                            drawPathEntered(sideRooms[0],1770 + room.getX() * 20, 150 + room.getY() * 20 - 10,1.5f,1);
                        }
                    }
                    if(sideRooms[2] != null) {
                        if (room.isLeft()) {
                            drawPathEntered(sideRooms[2],1770 + room.getX() * 20 - 10, 150 + room.getY() * 20,1.5f,2);
                        }
                    }
                    if(sideRooms[3] != null) {
                        if (room.isRight()) {
                            drawPathEntered(sideRooms[3],1770 + room.getX() * 20 + 10, 150 + room.getY() * 20,1.5f,3);
                        }
                    }
                }
            }
            geometryShader.unbind();
        }
        drawPlayerIcon();

        if(displayBigMap){
            for(MMPlayerArrow arrow : playerArrows){
                if(arrow != null)arrow.draw();
            }
        }
    }
    public void addRoom(MMRoom room, int number){
        rooms[number] = room;
    }

    public void keyPressed(int k){
        if(k == ControlSettings.getValue(ControlSettings.MAP)){
            displayBigMap = true;
            minimapBorders.setScale(7f);
            minimapBorders.setPosition(new Vector3f(960,500,0));
        }
    }
    public void keyReleased(int k){
        if(k == ControlSettings.getValue(ControlSettings.MAP)){
            displayBigMap = false;
            minimapBorders.setScale(2);
            minimapBorders.setPosition(new Vector3f(1770,150,0));

        }
    }
    public void forceHideBigMap(){
        displayBigMap = false;
        minimapBorders.setScale(2);
        minimapBorders.setPosition(new Vector3f(1770,150,0));

    }
    private void drawRoom(MMRoom room, float x, float y, float scale){
        Matrix4f matrixPos = new Matrix4f()
                .translate(new Vector3f(x, y, 0))
                .scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
        shader.setUniformm4f("projection", matrixPos);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

        int texture = (room.isEntered() ? 0 : 4) + room.getType();
        glBindBuffer(GL_ARRAY_BUFFER, vboTextures[texture]);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
    }

    /**
     * same as function drawPathEntered() only that it will draw only paths to unentered rooms
     * @param sideRoom
     * @param x
     * @param y
     * @param scale
     * @param dir
     */
    private void drawPathUnentered(MMRoom sideRoom, float x, float y, float scale,int dir){
        if(sideRoom != null) {
            if (sideRoom.isDiscovered() && !sideRoom.isEntered()) {
                Matrix4f matrixPos = new Matrix4f()
                        .translate(new Vector3f(x, y, 0))
                        .scale(scale);
                if(dir == 1) matrixPos.rotateX((float)Math.PI);
                if(dir == 2) matrixPos.rotateZ((float)Math.PI/2);
                if(dir == 3) matrixPos.rotateZ(-(float)Math.PI/2);
                Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
                shader.setUniformm4f("projection", matrixPos);
                glEnableVertexAttribArray(0);
                glEnableVertexAttribArray(1);

                glBindBuffer(GL_ARRAY_BUFFER, pathVboVertices);
                glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

                glBindBuffer(GL_ARRAY_BUFFER, vboTexturesTrans);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

                glDrawArrays(GL_QUADS, 0, 4);

                glBindBuffer(GL_ARRAY_BUFFER, 0);

                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);

            }
        }
    }

    /**
     * draws a path to entered room
     * @param sideRoom - room to which we are making path
     * @param x - x position of opengl draw
     * @param y - y position of opengl draw
     * @param scale - scale of path draw
     * @param dir - direction in which is path made, 0 - bottom, 1 - top, 2 - left, 3 - right
     */
    //TODO: lze udelat optimalizace, nevykreslovat napr u roomky c.1 cestu doleva kdyz je vykreslena cesta z roomky c.0 do prava
    private void drawPathEntered(MMRoom sideRoom, float x, float y, float scale,int dir){
        if (sideRoom.isDiscovered() && sideRoom.isEntered()) {
            Matrix4f matrixPos = new Matrix4f()
                    .translate(new Vector3f(x, y, 0))
                    .scale(scale);
            if(dir == 1) matrixPos.rotateX((float)Math.PI);
            if(dir == 2) matrixPos.rotateZ((float)Math.PI/2);
            if(dir == 3) matrixPos.rotateZ(-(float)Math.PI/2);
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
    private void drawPlayerIcon(){
        shader.bind();
        glActiveTexture(GL_TEXTURE0);
        playerIcon.bindTexture();
        Matrix4f matrixPos = new Matrix4f()
                .translate(playerIconPos)
                .scale(displayBigMap ? 4 : 1.25f);
        Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
        shader.setUniformm4f("projection", matrixPos);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, playerIconAnimation.getFrame().getVbo());
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public boolean isDisplayBigMap() {
        return displayBigMap;
    }
}
