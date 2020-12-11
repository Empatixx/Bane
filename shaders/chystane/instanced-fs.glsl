#version 330

in vec2 tex_coords;
out vec4 gl_FragColor;

void main(){
    gl_FragColor = texture(0, tex_coords);
}