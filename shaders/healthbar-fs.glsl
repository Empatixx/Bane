#version 330

uniform float maxX;
uniform float premaxX;
uniform float maxY;
uniform float stepSize;
uniform vec3 color;

out vec4 gl_FragColor;

void main(){
    if (gl_FragCoord.x < maxX){
        if (gl_FragCoord.y > maxY - stepSize*2){ // X: 155 = 250 + 45(fix) - (56*5(scale))/2
            gl_FragColor = vec4(color, 1);
        } else if (gl_FragCoord.y > maxY - stepSize*3){
            gl_FragColor = vec4(color+vec3(0.149,0.068,0.104), 1);
        } else if (gl_FragCoord.y > maxY - stepSize*4){
            gl_FragColor = vec4(color+vec3(0.318,0.168,0.161), 1);
        }
    }
    else if (gl_FragCoord.x < premaxX){
        if (gl_FragCoord.y > maxY - stepSize*2){ // X: 155 = 250 + 45(fix) - (56*5(scale))/2
            gl_FragColor = vec4(.9, color.gb, 1);
        } else if (gl_FragCoord.y > maxY - stepSize*3){
            gl_FragColor = vec4(.9, color.gb, 1);
        } else if (gl_FragCoord.y > maxY - stepSize*4){
            gl_FragColor = vec4(.9, color.gb, 1);
        }
    }
}