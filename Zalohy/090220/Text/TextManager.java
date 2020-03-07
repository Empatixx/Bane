package cz.Empatix.Render.Text;

import cz.Empatix.Entity.Player;
import cz.Empatix.Graphics.ByteBufferImage;
import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;

public class TextManager {

    public static final int classicFont = 0;

    private static ArrayList<Font> fonts;


    private static Shader shader;
    private int[] data;
    public TextManager(){
        fonts = new ArrayList<>();
        addFont("Textures\\font");
        shader = ShaderManager.createShader("shaders\\shader");
    }

    public void addFont(String name){
        File file = new File(name);
        try (FileInputStream fos = new FileInputStream(file+".dat");
             BufferedInputStream bos = new BufferedInputStream(fos);
             DataInputStream dos = new DataInputStream(bos)) {
            // cell/map height && width
            data = new int[4];
            for (int i = 0;i  < 4;i++){
                data[i] = flipEndian(dos.readInt());
            }
            // offset char
            int offsetChar = dos.readUnsignedByte();

            int[] widths = new int[256];
            for(int j = 0; j < 256;j++){
                widths[j] = dos.readUnsignedByte();
            }

            ByteBufferImage decoder = new ByteBufferImage();
            ByteBuffer spritesheetImage = decoder.decodeImage(file+".tga");

            int idTexture = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, idTexture);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

            STBImage.stbi_image_free(spritesheetImage);

            Font createFont = new Font(data[0], data[1], data[2], data[3],idTexture);

            int cols = data[0] / data[2];
            int rows = data[1] / data[3];

            for (int i = 0; i < createFont.countChars();i++){
                double[] texCoords =
                        {
                                (double) i%cols / cols, (double) i/cols / rows,

                                (double) i%cols / cols, (1.0 + (double)i/cols) / rows,

                                (1.0 + i%cols) / cols, (1.0 + (double)i/cols) / rows,

                                (1.0 + i%cols) / cols, (double) i/cols / rows
                        };

                DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
                buffer.put(texCoords);
                buffer.flip();
                int vboTextures = glGenBuffers();


                glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
                glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER,0);


                int vboVertexes = (ModelManager.getModel(widths[offsetChar], data[3]));
                if(vboVertexes == -1){
                    vboVertexes = ModelManager.createModel(widths[offsetChar], data[3]);
                }


                Character Character = new Character((char)offsetChar, vboTextures,vboVertexes);

                offsetChar++;
                createFont.setCharacter(Character,i);

                fonts.add(createFont);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void renderText(String text,int font, Camera camera, Vector3f pos){
        Matrix4f mpos = new Matrix4f().translate(pos);
        Font textfont = fonts.get(font);

        shader.bind();
        shader.setUniform("sampler",0);
        shader.setUniform("projection",camera.projection().mul(mpos));

        glActiveTexture(GL_TEXTURE0);
        textfont.bindTexture();
        for(char c : text.toCharArray()){
            for(Character character : textfont.getCharacters()){
                if (character.getChar() == c){

                    glEnableVertexAttribArray(0);
                    glEnableVertexAttribArray(1);

                    glBindBuffer(GL_ARRAY_BUFFER,character.bindVex());
                    glVertexAttribPointer(0,2,GL_INT,false,0,0);

                    glBindBuffer(GL_ARRAY_BUFFER,character.bindTex());
                    glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

                    glDrawArrays(GL_QUADS, 0, 4);

                    glBindBuffer(GL_ARRAY_BUFFER,0);

                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);
                    break;
                }
            }
        }
        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);



    }



    private int flipEndian(int val) {
        return (val >>> 24) | (val << 24) | ((val << 8) & 0x00FF0000)
                | ((val >> 8) & 0x0000FF00);
    }
}
