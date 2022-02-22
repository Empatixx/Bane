package cz.Empatix.Render.Graphics.Shaders;

import cz.Empatix.Render.Postprocessing.Lightning.LightPoint;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL40.*;

public class Shader{
    private final int program;

    private final HashMap<String,Integer> locations;

    Shader(String filepath){
        program = glCreateProgram();

        final int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, readfile(filepath+"-vs.glsl"));
        glCompileShader(vertexShader);
        // if vertex shader is compiled wrong
        if(glGetShaderi(vertexShader,GL_COMPILE_STATUS) != 1){
            System.err.println(glGetShaderInfoLog(vertexShader));
        }

        final int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, readfile(filepath+"-fs.glsl"));
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
    public void setUniformi(String name, int value){
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
    public void setUniformf(String name, float value){
        int location;
        if (locations.get(name) == null){
            location = glGetUniformLocation(program,name);
            locations.put(name,location);
        } else {
            location = locations.get(name);
        }
        if (location != -1){
            glUniform1f(location,value);
        }
    }
    public void setUniformm4f(String name, Matrix4f value){
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
    public void setUniformm3fx2f(String name, Matrix3x2f value){
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
    public void setUniform3f(String name, Vector3f value){
        int location;
        if (locations.get(name) == null){
            location = glGetUniformLocation(program,name);
            locations.put(name,location);
        } else {
            location = locations.get(name);
        }
        if (location != -1){
            glUniform3f(location,value.x,value.y,value.z);
        }
    }
    public void setUniform2f(String name, Vector2f value){
        int location;
        if (locations.get(name) == null){
            location = glGetUniformLocation(program,name);
            locations.put(name,location);
        } else {
            location = locations.get(name);
        }
        if (location != -1){
            glUniform2f(location,value.x,value.y);
        }
    }
    public void setUniformLights(Object[] lights){
        for(int i = 0;i < lights.length;i++){
            LightPoint light = (LightPoint)lights[i];
            Vector3f Color = light.getColor();
            Vector2f Pos = light.getPos();
            float Intensity = light.getIntensity();

            int locationPos;
            if (locations.get("lights["+i+"].position") == null){
                locationPos = glGetUniformLocation(program,"lights["+i+"].position");
                locations.put("lights["+i+"].position",locationPos);
            } else {
                locationPos = locations.get("lights[" + i + "].position");
            }
            int locationIntensity;
            if (locations.get("lights["+i+"].intensity") == null){
                locationIntensity = glGetUniformLocation(program, "lights["+i+"].intensity");
                locations.put("lights["+i+"].intensity",locationIntensity);
            } else {
                locationIntensity = locations.get("lights[" + i + "].intensity");
            }
            int locationColor;
            if (locations.get("lights[" + i + "].color") == null) {
                locationColor = glGetUniformLocation(program, "lights[" + i + "].color");
                locations.put("lights[" + i + "].color", locationColor);
            } else {
                locationColor = locations.get("lights[" + i + "].color");
            }
            if(locationColor != -1)glUniform3f(locationColor,Color.x,Color.y,Color.z);
            if(locationPos != -1)glUniform2f(locationPos,Pos.x,Pos.y);
            if(locationIntensity != -1) glUniform1f(locationIntensity,Intensity);
        }
    }
    public void unbind(){
        glUseProgram(0);
    }
    public void setAttribute(int indexAttribute, String attribute){
        glBindAttribLocation(program,indexAttribute,attribute);
    }
}
