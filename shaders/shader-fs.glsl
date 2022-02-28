#version 130

uniform sampler2D sampler;

in vec2 tex_coords;

void main(){
    gl_FragColor = texture(sampler, tex_coords);
}