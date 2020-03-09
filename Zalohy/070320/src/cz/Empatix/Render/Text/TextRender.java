package cz.Empatix.Render.Text;


import com.sun.istack.internal.NotNull;
import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;


public class TextRender {

    private static Matrix4f matrixPos;

    private static ArrayList<Font> fonts;

    private static Shader shader;

    public static void init(){
        fonts = new ArrayList<>();
        loadFont("Textures\\font");
        shader = ShaderManager.getShader("shaders\\text");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\text");
        }
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

                int vboVertexes = ModelManager.getModel(data[2],data[3]);
                if (vboVertexes == -1){
                    vboVertexes = ModelManager.createModel(data[2],data[3]);
                }

                int currentRow = i/cols;
                int currentCol = i%cols;

                double[] texCoords =
                        {
                                (double) currentCol / cols, (double)currentRow / rows,

                                (double) currentCol / cols, (1.0 + currentRow) / rows,

                                (currentCol+1.0) / cols, (1.0 +currentRow) / rows,

                                (currentCol+1.0) / cols, (double) currentRow / rows
                        };

                DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
                buffer.put(texCoords);
                buffer.flip();
                int vboTextures = glGenBuffers();

                glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
                glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER,0);

                FontChar createdChar = new FontChar((char)(offsetChar+i),widths[offsetChar+i],vboVertexes,vboTextures);
                createdFont.setChar(createdChar,i);
            }

            fonts.add(createdFont);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param camera - camera for which we will display text [Dimensions]
     * @param text - String that we want to render
     * @param pos - Position of text
     * @param scale - Scaling of text
     */
    public static void renderText(Camera camera, String text,@NotNull Vector3f pos, int scale, @NotNull Vector3f color){
        Font font = fonts.get(0);
        matrixPos = new Matrix4f().translate(pos).scale(scale);

        shader.bind();

        glActiveTexture(GL_TEXTURE0);
        font.bindTexture();

        shader.setUniformi("sampler",0);
        shader.setUniform3f("color",color);
        camera.hardProjection().mul(matrixPos,matrixPos);

        for(char c : text.toCharArray()){
            shader.setUniformm4f("projection",matrixPos);
            for (FontChar fontc : font.getChars()){
                if (fontc.getChar() == c){

                    glEnableVertexAttribArray(0);
                    glEnableVertexAttribArray(1);


                    glBindBuffer(GL_ARRAY_BUFFER,fontc.getVerticlesVBO());
                    glVertexAttribPointer(0,2,GL_INT,false,0,0);

                    glBindBuffer(GL_ARRAY_BUFFER,fontc.getTexcoordsVBO());
                    glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

                    glDrawArrays(GL_QUADS, 0, 4);

                    glBindBuffer(GL_ARRAY_BUFFER,0);

                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);

                    // shifting width of char
                    matrixPos.translate(fontc.getWidth(),0,0);
                    break;
                }
            }

        }
        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);

    }

    private static int flipEndian(int val) {
        return (val >>> 24) | (val << 24) | ((val << 8) & 0x00FF0000)
                | ((val >> 8) & 0x0000FF00);
    }
}