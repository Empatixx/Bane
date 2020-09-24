#version 330

in vec3 vertices;

void main(){
    gl_Position = vec4(vertices, 1);
}