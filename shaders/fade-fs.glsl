#version 130

uniform sampler2D sampler;
uniform vec2 resolution;
uniform float darkness;

void main(){
    gl_FragColor = texture2D(sampler, gl_FragCoord.xy/resolution);
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    uv *=  1.0 - uv.yx;

    float vig = uv.x*uv.y * 15.0; // multiply with sth for intensity

    vig = pow(vig, darkness); // change pow for modifying the extend of the  vignette

    gl_FragColor.xyz *= vec3(vig);
}