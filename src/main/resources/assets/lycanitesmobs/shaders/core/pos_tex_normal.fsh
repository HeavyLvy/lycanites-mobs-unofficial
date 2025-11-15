#version 150
uniform sampler2D Sampler0;

uniform vec2 OverlayUv;
uniform vec2 LightUv;

in vec2 vTexCoord;
in vec3 vNormalVS;
in vec4 vColor;

out vec4 fragColor;
uniform float AlphaCutoff;
void main() {
    vec4 tex = texture(Sampler0, vTexCoord);
    if (AlphaCutoff > 0.0 && tex.a <= AlphaCutoff) {
        discard;
    }
    float ndotl = max(dot(normalize(vNormalVS), vec3(0.0, 0.0, 1.0)), 0.0);
    vec3 lit = mix(tex.rgb * 0.5, tex.rgb, ndotl);
    lit *= 1.5;
    fragColor = vec4(lit, tex.a) * vColor;
}
