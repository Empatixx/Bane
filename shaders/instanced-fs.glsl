#version 130

in vec2 tex_coords;
uniform sampler2D sampler;


void main(){
    gl_FragColor = texture(sampler, tex_coords);
}