#version 130

uniform sampler2D sampler;
uniform float spawn;

in vec2 tex_coords;

void main(){
    gl_FragColor = texture(sampler, tex_coords);
    gl_FragColor.rgba *= vec4(vec3(spawn),0.9);
}