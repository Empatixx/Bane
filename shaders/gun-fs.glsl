#version 130

uniform sampler2D sampler;
uniform vec2 pos;
uniform vec2 iResolution;

void main(){
    gl_FragColor = vec4(0.0);
    vec2 position = pos;
    position.y = 1080-position.y;
    vec2 fragCoord = gl_FragCoord.xy - position.xy + iResolution/2;
    fragCoord = roundEven(fragCoord);

    if(fragCoord.x < 0 && fragCoord.y < 3. ||
    fragCoord.x < 3. && fragCoord.y < 0.
    ||
    (fragCoord.x > iResolution.x-3. && fragCoord.y > iResolution.y-0. ||
    fragCoord.x > iResolution.x-0. && fragCoord.y > iResolution.y-3.
    ||
    fragCoord.x > iResolution.x-0. && fragCoord.y < 3. ||
    fragCoord.x > iResolution.x-3. && fragCoord.y < 0.
    ||
    fragCoord.x < 0. && fragCoord.y > iResolution.y-3. ||
    fragCoord.x < 3. && fragCoord.y > iResolution.y-0.
    )){
        gl_FragColor = vec4(vec3(0.0),1);
    }
}