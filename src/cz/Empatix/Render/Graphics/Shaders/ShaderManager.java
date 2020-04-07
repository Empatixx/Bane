package cz.Empatix.Render.Graphics.Shaders;

import java.util.HashMap;

public class ShaderManager {
    private final static HashMap<String,Shader> shaders = new HashMap<>();
    public static Shader getShader(String filepath){
        return shaders.get(filepath);
    }
    public static Shader createShader(String filepath){
        Shader shader = new Shader(filepath);
        shaders.put(filepath,shader);
        return shader;
    }
}
