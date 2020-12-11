#version 330

in vec3 vertices;
in vec2 textures;
in mat4 projection;
out vec2 tex_coords;

void main(){
    tex_coords = textures;
    gl_Position = projection * vec4(vertices, 1);
}