#version 130

struct Light
{
    vec2 position;
    vec3 color;
    float intensity;
};

uniform int lightCount;
uniform float brightness;
uniform int enabled;
uniform Light lights[256];
uniform sampler2D texture;
uniform sampler2D noise;
uniform vec2 size;

in vec2 tex_coords;

out vec4 gl_FragColor;


void main()
{
    if(enabled == 0){
        vec2 uv = gl_FragCoord.xy;
        gl_FragColor = texture2D(texture, uv / size);
    } else {

        vec3 lAtt = vec3(5.5,0.001,0.0003);

        lAtt*=vec3(1920/size.x);

        //vec3 lAtt = vec3(0.96,0.001,0.0005);

        // coords of screen
        vec2 uv = gl_FragCoord.xy;

        // darkness
        vec3 outc = vec3(0.5);
        float radius = 200;

        for (int i = 0; i < lightCount; i++)
        {
            vec2 lightPos = vec2(lights[i].position.x,size.y - lights[i].position.y);
            float dist = distance(lightPos, uv);
            float att = 1.0 / (lAtt.y * dist + lAtt.x + dist * dist * lAtt.z);

            outc+=vec3(vec3(att)) * lights[i].intensity * vec3(lights[i].color);
        }

        vec4 pixel = texture2D(texture, uv / size);
        // brightness
        pixel.rgb+=vec3(brightness/4);

        gl_FragColor = pixel * vec4(outc,1);
        if(lightCount != 0){
            // color banding fix
            float noise = texture2D(noise, uv/size).r;
            gl_FragColor.rgb += mix(-0.5/255.0, 0.5/255.0, noise);
        }

    }
}
