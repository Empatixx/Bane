#version 330

uniform sampler2D sampler;
uniform vec2 resolution;
uniform float darkness;

out vec4 gl_FragColor;

void main(){
    gl_FragColor = texture2D(sampler, gl_FragCoord.xy/resolution);
    gl_FragColor.rgb*=vec3(darkness);
}