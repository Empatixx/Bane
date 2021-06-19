#version 330

uniform mat4 uModelViewProjectionMat;
uniform mat4 uPrevModelViewProjectionMat;

in vec3 vertices;
smooth out vec4 vPosition;
smooth out vec4 vPrevPosition;

void main(){
    vPosition = uModelViewProjectionMat * vec4(vertices, 1);
    vPrevPosition = uPrevModelViewProjectionMat * vec4(vertices, 1);

    gl_Position = vPosition;
}