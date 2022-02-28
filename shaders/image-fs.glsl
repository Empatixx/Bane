#version 130

uniform sampler2D sampler;
uniform float alpha;

in vec2 tex_coords;

void main(){
    gl_FragColor = texture(sampler, tex_coords);
    gl_FragColor.a*=alpha;
}