#version 330

uniform mat4 projection;

in vec3 vertices;

void main(){
    gl_Position = projection * vec4(vertices, 1);
}