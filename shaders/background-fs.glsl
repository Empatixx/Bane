#version 330

uniform sampler2D sampler;
uniform float alpha;

in vec2 tex_coords;
out vec4 gl_FragColor;

void main(){
    vec4 barva = texture(sampler, tex_coords);
    gl_FragColor = vec4(barva.r,barva.g,barva.b,barva.a*alpha);

}