#version 330

uniform sampler2D sampler;
uniform vec2 resolution;
uniform vec2 location;
out vec4 gl_FragColor;

#define RADIUS  0.01
#define SAMPLES 3

void main(){
    vec2 uv = (gl_FragCoord.xy-iMouse.xy)/iResolution.xy;
    uv.x *= resolution.x/resolution.y;

    float d = length(uv);

    float c = 0.0;

    if (d < 0.1) {
        c = 1.0;
    }else{
        c = 0.0;
    }

    vec3 pfragColor = vec3(0);

	for (int i = -SAMPLES; i < SAMPLES; i++) {
		for (int j = -SAMPLES; j < SAMPLES; j++) {
			pfragColor += texture(sampler, gl_FragCoord.xy / resolution.xy + vec2(i, j) * (RADIUS/float(SAMPLES))).xyz
				 / pow(float(SAMPLES) * 2., 2.);
		}
    }

    gl_FragColor = vec4(pfragColor * vec3(0.8), 1.0) + vec4(vec3(c), 1.0);
}