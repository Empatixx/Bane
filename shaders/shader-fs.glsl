#version 330

uniform sampler2D sampler;

in vec2 tex_coords;
out vec4 gl_FragColor;

void main(){
    gl_FragColor = texture(sampler, tex_coords);
    //gl_FragColor *= vec4(1.0,1.0,1.0,0.5);
}