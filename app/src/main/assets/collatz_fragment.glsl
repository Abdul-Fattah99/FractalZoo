//from https://www.shadertoy.com/view/Ms3XDn
precision mediump float;

//uniform sampler1D tex;
uniform float centerX;
uniform float centerY;
uniform float scale;
uniform float iterations;
uniform vec2 resolution;
#define maxiter 1024
// set viewing parameters
vec2 center = vec2(centerX, centerY);
float pi = 3.141592653;

// hyperbolic cosine
float cosh(float x) {
    return (exp(x) + exp(-x))/2.0;
}

// hyperbolic sine
float sinh(float x) {
    return (exp(x) - exp(-x))/2.0;
}

// complex multiplication
vec2 cmul(vec2 a, vec2 b) {
    return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
}

// complex cosine
vec2 ccos(vec2 a) {
    return vec2(cos(a.x) * cosh(a.y), -sin(a.x) * sinh(a.y));
}

void main()
{
    // initialize z with pixel coordinates
	vec2 z = (gl_FragCoord.xy / resolution.y - vec2(resolution.x * 0.5 / resolution.y, 0.5)) * scale - center;
	int n = 0;
    for (int i = 0; i < maxiter; ++i) {
        if (i > int(iterations)) break;
        // bail out if z gets too big
        if (length(z) > 16.0) {
            n = i;
            break;
        }
        // do one step of generalized Collatz function
        z = (vec2(1.0,0.0) + 4.0 * z - cmul(vec2(1.0,0.0) + 2.0 * z, ccos(pi * z))) / 4.0;
    }
    // colour pixel according to escape time
    float t = log(float(n + 1)) / log(1024.0);
    gl_FragColor = vec4(float(n != 0) * vec3(sqrt(t), t, 1.0 - sqrt(t)),1.0);
}
