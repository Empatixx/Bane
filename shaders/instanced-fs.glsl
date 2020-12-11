#version 330

in vec2 tex_coords;
out vec4 gl_FragColor;
uniform sampler2D sampler;


void main(){
    gl_FragColor = texture(sampler, tex_coords);
}