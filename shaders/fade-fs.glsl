#version 330

uniform sampler2D sampler;
uniform vec2 resolution;
uniform float value;

out vec4 gl_FragColor;

void main(){
    vec2 uv = gl_FragCoord.xy/resolution;
    if(uv.x > value && uv.x < 1-value){
        gl_FragColor = texture2D(sampler, gl_FragCoord.xy/resolution);
    } else {
        gl_FragColor *=vec4(vec3(0.0),1);
    }

}