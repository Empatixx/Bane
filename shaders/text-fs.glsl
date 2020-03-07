#version 330

uniform sampler2D sampler;
uniform vec3 color;

in vec2 tex_coords;
out vec4 gl_FragColor;

void main(){
    gl_FragColor = vec4(color,texture(sampler,tex_coords).a);
}