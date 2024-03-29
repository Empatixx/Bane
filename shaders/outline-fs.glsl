#version 130
uniform sampler2D sampler;
uniform vec2 stepSize;
uniform float outlineAlpha;
uniform vec3 color;


in vec2 tex_coords;

void main(){
    float alpha = 4*texture2D( sampler, tex_coords ).a;
    alpha -= texture2D( sampler, tex_coords + vec2( stepSize.x, 0.0f ) ).a;
    alpha -= texture2D( sampler, tex_coords + vec2( -stepSize.x, 0.0f ) ).a;
    alpha -= texture2D( sampler, tex_coords + vec2( 0.0f, stepSize.y ) ).a;
    alpha -= texture2D( sampler, tex_coords + vec2( 0.0f, -stepSize.y ) ).a;
    // calculate resulting color
    gl_FragColor = vec4( color, outlineAlpha*alpha );
}