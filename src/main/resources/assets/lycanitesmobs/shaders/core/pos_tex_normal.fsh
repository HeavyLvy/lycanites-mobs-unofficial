#version 150
uniform sampler2D Sampler0;

uniform vec2 OverlayUv;
uniform vec2 LightUv;

in vec2 vTexCoord;
in vec3 vNormalVS;
in vec4 vColor;

out vec4 fragColor;

uniform float AlphaCutoff;

uniform vec3 BaseColor;
uniform float BaseRange;

uniform vec3 VariantColor;
uniform float VariantAmount;

void main() {
    vec4 tex = texture(Sampler0, vTexCoord);
    if (AlphaCutoff > 0.0 && tex.a <= AlphaCutoff) {
        discard;
    }

    vec3 N = normalize(vNormalVS);
    vec3 L = normalize(vec3(0.0, 0.0, 1.0));
    float ndotl = max(dot(N, L), 0.0);

    vec3 base = tex.rgb;

    float dist = distance(base, BaseColor);
    float influence = clamp(1.0 - dist / BaseRange, 0.0, 1.0);

    float amount = VariantAmount * influence;
    vec3 recolored = mix(base, VariantColor, amount);

    vec3 lit = mix(recolored * 0.5, recolored, ndotl);

    fragColor = vec4(lit, tex.a) * vColor;
}
