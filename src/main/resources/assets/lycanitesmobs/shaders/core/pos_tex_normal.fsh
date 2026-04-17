#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform vec2 OverlayUv;
uniform vec2 LightUv;

in vec2 vTexCoord;
in vec3 vNormalVS;
in vec4 vColor;
in vec3 vViewDir;
in float vertexDistance;

out vec4 fragColor;

uniform float AlphaCutoff;

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform vec3 BaseColor;
uniform float BaseRange;

uniform vec3 VariantColor;
uniform float VariantAmount;

uniform vec3 SunDirectionVS;
uniform vec3 SpecularColor;
uniform float SpecularIntensity;
uniform float SpecularPower;

uniform float LightRangeScale;
uniform float HighlightCompression;
uniform float FearRedGlow;

void main() {
    vec4 tex = texture(Sampler0, vTexCoord);
    if (AlphaCutoff > 0.0 && tex.a <= AlphaCutoff) {
        discard;
    }

    // Variant recolor band
    vec3 base = tex.rgb;
    float dist = distance(base, BaseColor);
    float influence = BaseRange > 0.0 ? clamp(1.0 - dist / BaseRange, 0.0, 1.0) : 0.0;
    float amount = VariantAmount * influence;
    vec3 recolored = mix(base, VariantColor, amount);

    // Split vanilla lightmap into block-only vs sky-only contributions.
    // LightUv.x encodes block light (0..240), LightUv.y encodes sky light.
    // Sampler2 is a 16x16 lightmap combining block (U) and sky (V) channels.
    const float TEX = 1.0 / 16.0;
    vec2 clampedBlock = clamp(vec2(LightUv.x / 256.0, 0.5 * TEX), vec2(0.5 * TEX), vec2(15.5 * TEX));
    vec2 clampedSky   = clamp(vec2(0.5 * TEX, LightUv.y / 256.0), vec2(0.5 * TEX), vec2(15.5 * TEX));
    vec3 blockLight = texture(Sampler2, clampedBlock).rgb;
    vec3 skyLight   = texture(Sampler2, clampedSky).rgb;
    float skyFactor   = max(skyLight.r,   max(skyLight.g,   skyLight.b));
    float blockFactor = max(blockLight.r, max(blockLight.g, blockLight.b));

    // Per-entity light range compression: pulls both ends toward 0.5.
    // scale = 1.0 leaves range untouched; scale < 1.0 dims highs and lifts lows.
    float rangeScale = clamp(LightRangeScale, 0.0, 1.0);
    skyFactor   = 0.5 + (skyFactor   - 0.5) * rangeScale;
    blockFactor = 0.5 + (blockFactor - 0.5) * rangeScale;
    skyLight    = vec3(0.5) + (skyLight   - vec3(0.5)) * rangeScale;
    blockLight  = vec3(0.5) + (blockLight - vec3(0.5)) * rangeScale;

    // Matte lightmap-based lighting (no directional sun shading).
    // SunDirectionVS, SpecularColor, SpecularIntensity, SpecularPower, vNormalVS,
    // and vViewDir are still available as uniforms/varyings for resource pack
    // shader overrides that want to implement custom material lighting.

    // Combine sky and block light contributions.
    // Block light fills the gap left by sky coverage so they don't double-bright.
    vec3 sunContrib   = skyLight * skyFactor;
    vec3 blockContrib = blockLight * blockFactor;
    float sunCoverage = clamp(max(sunContrib.r, max(sunContrib.g, sunContrib.b)), 0.0, 1.0);
    vec3 lightCol = sunContrib + blockContrib * (1.0 - sunCoverage);

    // Keep a global minimum so things aren't fully black in deep dark.
    float minBrightness = 0.15;
    lightCol = max(lightCol, vec3(minBrightness));

    vec3 lit = recolored * lightCol;

    // Fear red glow: red-dominant pixels bypass lighting and become emissive.
    // Detects texels where red channel significantly exceeds green and blue,
    // making creature eyes/markings glow through the fear darkness.
    if (FearRedGlow > 0.0) {
        float redExcess = recolored.r - max(recolored.g, recolored.b);
        float isRed = smoothstep(0.12, 0.35, redExcess) * smoothstep(0.25, 0.45, recolored.r);
        vec3 emissive = recolored * 1.8;
        lit = mix(lit, emissive, isRed * FearRedGlow);
    }

    vec3 finalRgb = lit;

    // Damage red flash: OverlayUv.y == 0 means full red, 10 means no red
    float hurtIntensity = clamp(1.0 - OverlayUv.y / 10.0, 0.0, 1.0);
    if (hurtIntensity > 0.0) {
        finalRgb = mix(finalRgb, vec3(1.0, 0.0, 0.0), hurtIntensity * 0.3);
    }

    // Per-entity highlight rolloff. 0.0 = no compression
    if (HighlightCompression > 0.0) {
        finalRgb = finalRgb / (1.0 + finalRgb * HighlightCompression);
    }

    vec4 preFogColor = vec4(finalRgb, tex.a) * vColor;

    // Fog: blend toward FogColor based on vertex distance.
    // Matches vanilla linear_fog(): smoothstep between FogStart and FogEnd,
    // scaled by FogColor.a (0 = no fog active, 1 = full fog).
    // Covers all fog types: distance, underwater, lava, blindness, void, powder snow, etc.
    if (FogColor.a > 0.0) {
        float fogFactor = vertexDistance <= FogStart ? 0.0
                        : vertexDistance >= FogEnd   ? 1.0
                        : smoothstep(FogStart, FogEnd, vertexDistance);
        fragColor = vec4(mix(preFogColor.rgb, FogColor.rgb, fogFactor * FogColor.a), preFogColor.a);
    } else {
        fragColor = preFogColor;
    }
}
