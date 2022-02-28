#version 130

uniform sampler2D sampler;
uniform vec3 color;

in vec2 tex_coords;

void main(){
    gl_FragColor = vec4(color,texture(sampler,tex_coords).a);
}