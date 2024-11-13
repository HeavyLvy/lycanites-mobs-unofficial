#version 330

in vec4 vertexColor;

out vec4 fragmentColor;

void main() {
    if (vertexColor.a == 0.0f) {
        discard;
    }
    fragmentColor = vertexColor;
}

/*
#version 330

in vec4 vertexColor;
in vec2 texCoord;
out vec4 fragmentColor;
uniform sampler2D u_texture;
void main() {
    if (vertexColor.a == 0.0f) {
        discard;
    }
    vec4 sampled = texture(u_texture, texCoord);
    fragmentColor = vertexColor * sampled;
}*/
