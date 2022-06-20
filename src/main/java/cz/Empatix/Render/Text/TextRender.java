package cz.Empatix.Render.Text;


import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;


public class TextRender {
    public static void load(){
        Loader.loadImage("Textures\\font.tga");
    }

    private static ArrayList<Font> fonts;

    private String text;
    private final int vboVertices;
    private final int vboTexCoords;

    private Shader shader;
    private Matrix4f matrixPos;

    public static void init(){
        fonts = new ArrayList<>();
        loadFont("Textures\\font");

    }

    private static void loadFont(String file){
        try (FileInputStream fos = new FileInputStream(file+".dat");
             BufferedInputStream bos = new BufferedInputStream(fos);
             DataInputStream dos = new DataInputStream(bos)) {
            // cell/map height && width
            int[] data = new int[4];
            for (int i = 0; i < 4; i++) {
                data[i] = flipEndian(dos.readInt());
            }
            // offset char
            int offsetChar = dos.readUnsignedByte();

            int[] widths = new int[256];
            for (int j = 0; j < 256; j++) {
                widths[j] = dos.readUnsignedByte();
            }

            int cols = data[0] / data[2];
            int rows = data[1] / data[3];

            Font createdFont = new Font(data[0],data[1],data[2],data[3],file);



            for(int i = 0;i < createdFont.sizeOfChars();i++){

                int[] vertices =
                        {
                                0,-data[3]/2, // BOTTOM LEFT
                                0,data[3]/2, // BOTTOM TOP
                                data[2],data[3]/2, // RIGHT TOP
                                data[2],-data[3]/2 // BOTTOM RIGHT



                        };

                int currentRow = i/cols;
                int currentCol = i%cols;

                double[] texCoords =
                        {
                                (double) currentCol / cols, (double)currentRow / rows,

                                (double) currentCol / cols, (1.0 + currentRow) / rows,

                                (currentCol+1.0) / cols, (1.0 +currentRow) / rows,

                                (currentCol+1.0) / cols, (double) currentRow / rows
                        };


                FontChar createdChar = new FontChar((char)(offsetChar+i),widths[offsetChar+i],vertices,texCoords);
                createdFont.setChar(createdChar,i);
            }

            fonts.add(createdFont);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static int flipEndian(int val) {
        return (val >>> 24) | (val << 24) | ((val << 8) & 0x00FF0000)
                | ((val >> 8) & 0x0000FF00);
    }

    public TextRender(){
        vboTexCoords = glGenBuffers();
        vboVertices = glGenBuffers();

        shader = ShaderManager.getShader("shaders\\text");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\text");
        }
    }
    public static float getHorizontalCenter(float min, float max,String text,int scale){
        int totalWidth = 0;

        Font font = fonts.get(0);

        for(char c : text.toCharArray()){
            for(FontChar fontChar : font.getChars()){
                if(fontChar.getChar() == c){
                    totalWidth+=fontChar.getWidth();
                }
            }
        }


        float center = min+(max-min)/2;
        totalWidth*=scale;
        return center-totalWidth/2f;
    }

    public void draw(String text, Vector3f pos, int scale, Vector3f color){
        Font font = fonts.get(0);

        if(this.text == null){
            DoubleBuffer textureBuffer = BufferUtils.createDoubleBuffer(text.length()*8);
            IntBuffer verticesBuffer = BufferUtils.createIntBuffer(text.length()*8);


            int shiftWidth = 0;


            for(char c : text.toCharArray()){
                for(FontChar fontChar : font.getChars()){
                    if(fontChar.getChar() == c){
                        int[] vertices = fontChar.getVertices().clone();
                        for(int i = 0;i<8;i+=2){
                            vertices[i]+=shiftWidth;
                        }
                        shiftWidth+=fontChar.getWidth();
                        verticesBuffer.put(vertices);
                        textureBuffer.put(fontChar.getTexCoords());
                        break;
                    }
                }
            }

            textureBuffer.flip();
            verticesBuffer.flip();


            glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
            glBufferData(GL_ARRAY_BUFFER,textureBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);


            glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
            glBufferData(GL_ARRAY_BUFFER,verticesBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);

            this.text = text;
        } else if(!this.text.equals(text)){
            DoubleBuffer textureBuffer = BufferUtils.createDoubleBuffer(text.length()*8);
            IntBuffer verticesBuffer = BufferUtils.createIntBuffer(text.length()*8);


            int shiftWidth = 0;


            for(char c : text.toCharArray()){
                for(FontChar fontChar : font.getChars()){
                    if(fontChar.getChar() == c){
                        int[] vertices = fontChar.getVertices().clone();
                        for(int i = 0;i<8;i+=2){
                            vertices[i]+=shiftWidth;
                        }
                        shiftWidth+=fontChar.getWidth();
                        verticesBuffer.put(vertices);
                        textureBuffer.put(fontChar.getTexCoords());
                        break;
                    }
                }
            }

            textureBuffer.flip();
            verticesBuffer.flip();


            glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
            glBufferData(GL_ARRAY_BUFFER,textureBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);


            glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
            glBufferData(GL_ARRAY_BUFFER,verticesBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);

            this.text = text;
        }

        matrixPos = new Matrix4f().translate(pos);

        shader.bind();
        glActiveTexture(GL_TEXTURE0);
        font.bindTexture();

        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos).scale(scale);
        shader.setUniform3f("color",color);
        shader.setUniformm4f("projection",matrixPos);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, text.length()*4);

        glBindBuffer(GL_ARRAY_BUFFER,0);


        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);

    }
    public void drawMap(String text, Vector3f pos, int scale, Vector3f color){
        Font font = fonts.get(0);

        if(this.text == null){
            DoubleBuffer textureBuffer = BufferUtils.createDoubleBuffer(text.length()*8);
            IntBuffer verticesBuffer = BufferUtils.createIntBuffer(text.length()*8);


            int shiftWidth = 0;


            for(char c : text.toCharArray()){
                for(FontChar fontChar : font.getChars()){
                    if(fontChar.getChar() == c){
                        int[] vertices = fontChar.getVertices().clone();
                        for(int i = 0;i<8;i+=2){
                            vertices[i]+=shiftWidth;
                        }
                        shiftWidth+=fontChar.getWidth();
                        verticesBuffer.put(vertices);
                        textureBuffer.put(fontChar.getTexCoords());
                        break;
                    }
                }
            }

            textureBuffer.flip();
            verticesBuffer.flip();


            glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
            glBufferData(GL_ARRAY_BUFFER,textureBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);


            glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
            glBufferData(GL_ARRAY_BUFFER,verticesBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);

            this.text = text;
        } else if(!this.text.equals(text)){
            DoubleBuffer textureBuffer = BufferUtils.createDoubleBuffer(text.length()*8);
            IntBuffer verticesBuffer = BufferUtils.createIntBuffer(text.length()*8);


            int shiftWidth = 0;


            for(char c : text.toCharArray()){
                for(FontChar fontChar : font.getChars()){
                    if(fontChar.getChar() == c){
                        int[] vertices = fontChar.getVertices().clone();
                        for(int i = 0;i<8;i+=2){
                            vertices[i]+=shiftWidth;
                        }
                        shiftWidth+=fontChar.getWidth();
                        verticesBuffer.put(vertices);
                        textureBuffer.put(fontChar.getTexCoords());
                        break;
                    }
                }
            }

            textureBuffer.flip();
            verticesBuffer.flip();


            glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
            glBufferData(GL_ARRAY_BUFFER,textureBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);


            glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
            glBufferData(GL_ARRAY_BUFFER,verticesBuffer,GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);

            this.text = text;
        }

        matrixPos = new Matrix4f().translate(pos).scale(scale);

        shader.bind();
        glActiveTexture(GL_TEXTURE0);
        font.bindTexture();

        Camera.getInstance().projection().mul(matrixPos,matrixPos);
        shader.setUniform3f("color",color);
        shader.setUniformm4f("projection",matrixPos);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, text.length()*4);

        glBindBuffer(GL_ARRAY_BUFFER,0);


        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);

    }
    // use when we want clear vbos of opengl to release allocated vram in graphics card
    public void clearVBOs(){
        glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
        glDeleteBuffers(GL_ARRAY_BUFFER);
        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glDeleteBuffers(GL_ARRAY_BUFFER);
        glBindBuffer(GL_ARRAY_BUFFER,0);
    }

}