package cz.Empatix.Graphics.Shaders;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL40.*;

public class Shader {
    private final int program;

    private final HashMap<String,Integer> locations;

    public Shader(String filepath){
        program = glCreateProgram();

        final int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, readfile(filepath+".vs"));
        glCompileShader(vertexShader);
        // if vertex shader is compiled wrong
        if(glGetShaderi(vertexShader,GL_COMPILE_STATUS) != 1){
            System.err.println(glGetShaderInfoLog(vertexShader));
        }

        final int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, readfile(filepath+".fs"));
        glCompileShader(fragmentShader);
        // if frag shader is compiled wrong
        if(glGetShaderi(fragmentShader,GL_COMPILE_STATUS) != 1){
            System.err.println(glGetShaderInfoLog(fragmentShader));
        }

        glAttachShader(program,vertexShader);
        glAttachShader(program,fragmentShader);

        glBindAttribLocation(program,0,"vertices");
        glBindAttribLocation(program,1,"textures");

        glLinkProgram(program);
        if(glGetProgrami(program,GL_LINK_STATUS) != 1){
            System.err.println(glGetProgramInfoLog(program));
            System.exit(1);
        }
        glValidateProgram(program);
        if(glGetProgrami(program,GL_VALIDATE_STATUS) != 1){
            System.err.println(glGetProgramInfoLog(program));
            System.exit(1);
        }
        locations = new HashMap<>();
    }
    private String readfile(String filepath){
        StringBuilder string = new StringBuilder();
        BufferedReader bufferedReader;
        try{
            bufferedReader = new BufferedReader
                    (
                    new FileReader(new File(filepath))
                    );
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                string.append(line);
                string.append("\n");
            }
            bufferedReader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return string.toString();
    }
    public void bind(){
        glUseProgram(program);
    }
    public void setUniform(String name, int value){
        int location;
        if (locations.get(name) == null){
            location = glGetUniformLocation(program,name);
            locations.put(name,location);
        } else {
            location = locations.get(name);
        }
        if (location != -1){
            glUniform1i(location,value);
        }
    }
    public void setUniform(String name, Matrix4f value){
        int location;
        if (locations.get(name) == null){
            location = glGetUniformLocation(program,name);
            locations.put(name,location);
        } else {
            location = locations.get(name);
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        if (location != -1){
            glUniformMatrix4fv(location,false,buffer);
        }
    }
    public void unbind(){
        glUseProgram(0);
    }
}
