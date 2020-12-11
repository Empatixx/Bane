#version 330

uniform sampler2D sampler;
uniform vec2 size;

out vec4 gl_FragColor;

void main(){
    vec2 uv = gl_FragCoord.xy;
    gl_FragColor = texture2D(texture, uv / size);

}