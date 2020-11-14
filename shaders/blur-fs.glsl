#version 330

uniform sampler2D sampler;
uniform vec2 resolution;
uniform float darkness;

out vec4 gl_FragColor;

vec4 blur(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) {
    vec4 color = vec4(0.0);
    vec2 off1 = vec2(1.411764705882353) * direction;
    vec2 off2 = vec2(3.2941176470588234) * direction;
    vec2 off3 = vec2(5.176470588235294) * direction;
    color += texture2D(image, uv) * 0.1964825501511404;
    vec2 curVec = uv + (off1 / resolution);
    if(curVec.x < 1 && curVec.y < 1){
        color += texture2D(image, uv + (off1 / resolution)) * 0.2969069646728344;
    }
    curVec = uv - (off1 / resolution);
    if(curVec.x > 0 && curVec.y > 0){
         color += texture2D(image, uv - (off1 / resolution)) * 0.2969069646728344;
    }
    curVec = uv + (off2 / resolution);
    if (curVec.x < 1 && curVec.y < 1){
        color += texture2D(image, uv + (off2 / resolution)) * 0.09447039785044732;
    }
    curVec = uv - (off2 / resolution);
    if (curVec.x > 0 && curVec.y > 0){
        color += texture2D(image, uv - (off2 / resolution)) * 0.09447039785044732;
    }
    curVec = uv + (off3 / resolution);
    if (curVec.x < 1 && curVec.y < 1){
        color += texture2D(image, uv + (off3 / resolution)) * 0.010381362401148057;
    }
    curVec = uv - (off3 / resolution);
    if (curVec.x > 0 && curVec.y > 0){
        color += texture2D(image, uv - (off3 / resolution)) * 0.010381362401148057;
    }
    return color;
}
void main(){
    gl_FragColor = blur(sampler, gl_FragCoord.xy/resolution, resolution, vec2(1.5*0.7/darkness, 1.5*0.7/darkness));
    gl_FragColor.rgb*=vec3(darkness);
}