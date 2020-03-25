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
uniform sampler2D noise;
uniform vec2 size;
uniform float iTime;

in vec2 tex_coords;

out vec4 gl_FragColor;

float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898,12.1414))) * 83758.5453);
}

float noise2(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n);
    vec2 f = mix(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

vec3 ramp(float t) {
    return t <= .5 ? vec3( 1. - t * 1.4, .2, 1.05 ) / t : vec3( .3 * (1. - t) * 2., .2, 1.05 ) / t;
}

float fire(vec2 n) {
    return noise2(n) + noise2(n * 2.1) * .6 + noise2(n * 5.4) * .42;
}

vec3 getLine(vec3 col, vec2 fc, mat2 mtx, float shift){
    float t = iTime;
    vec2 uv = (fc / size.xy) * mtx;

    uv.x += uv.y < .5 ? 23.0 + t * .35 : -11.0 + t * .3;
    uv.y = abs(uv.y - shift);
    uv *= 5.0;

    float q = fire(uv - t * .013) / 2.0;
    vec2 r = vec2(fire(uv + q / 2.0 + t - uv.x - uv.y), fire(uv + q - t));
    vec3 color = vec3(1.0 / (pow(vec3(0.5, 0.0, .1) + 1.61, vec3(4.0))));

    float grad = pow((r.y + r.y) * max(.0, uv.y) + .1, 4.0);
    color = ramp(grad);
    color /= (1.50 + max(vec3(0), color));

    if(color.b < .00000005)
    color = vec3(.0);

    return mix(col, color, color.b);
}

void main()
{

    vec3 lAtt = vec3(5.5,0.001,0.0003);

    // coords of screen
    vec2 uv = gl_FragCoord.xy;

    // darkness
    vec3 outc = vec3(0.5);

    for (int i = 0; i < lightCount; i++)
    {
        vec2 lightPos = vec2(lights[i].position.x,size.y - lights[i].position.y);
        float dist = distance(lightPos, uv);
        float att = 1.0 / (lAtt.y * dist + lAtt.x + lAtt.z * dist * dist);
        outc+=vec3(vec3(att)) * lights[i].intensity * vec3(lights[i].color);
    }

    vec4 pixel = texture2D(texture, uv / size);

    gl_FragColor = pixel * vec4(outc,1);

    float noise = texture2D(noise, uv/size).r;

    gl_FragColor.rgb += mix(-0.5/255.0, 0.5/255.0, noise);

    vec3 color = gl_FragColor.rgb;
    color = getLine(color, gl_FragCoord.xy, mat2(1., 1., 0., 1.), 1.02);
    color = getLine(color, gl_FragCoord.xy, mat2(1., 1., 1., 0.), 1.02);
    color = getLine(color, gl_FragCoord.xy, mat2(1., 1., 0., 1.), -0.02);
    color = getLine(color, gl_FragCoord.xy, mat2(1., 1., 1., 0.), -0.02);

    gl_FragColor = vec4(color, 1.0);

}
