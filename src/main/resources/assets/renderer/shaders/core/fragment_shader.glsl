#version 150

in vec2 vTexCoord;

uniform sampler2D textureSampler;

out vec4 fragColor;

void main() {
    fragColor = texture(textureSampler, vTexCoord);
}
