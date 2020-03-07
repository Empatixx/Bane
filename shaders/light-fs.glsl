#version 330

struct Light
{
    vec2 position;
    vec3 color;
    float intensity;
};

uniform int lightCount;
uniform Light lights[256];
uniform sampler2D texture;
uniform vec2 size;

in vec2 tex_coords;

out vec4 gl_FragColor;

void main()
{

    vec3 lAtt = vec3(5.5,0.001,0.0003);

    // coords of screen
    vec2 uv = gl_FragCoord.xy;

    // darkness
    vec4 outc = vec4(0.5);

    for (int i = 0; i < lightCount; i++)
    {
        vec2 lightPos = vec2(lights[i].position.x,size.y - lights[i].position.y);
        float dist = distance(lightPos, uv);
        float att = 1.0 / (lAtt.y * dist + lAtt.x + lAtt.z * dist * dist);
        outc+=vec4(vec3(att),1) * lights[i].intensity * vec4(lights[i].color,1);
    }

    vec4 pixel = texture2D(texture, uv / size);

    gl_FragColor = pixel * outc;


}
