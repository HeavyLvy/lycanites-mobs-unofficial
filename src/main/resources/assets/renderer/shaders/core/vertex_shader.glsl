#version 150

in vec3 Position;
in vec2 UV;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;

out vec2 vTexCoord;

void main() {
    vTexCoord = (TextureMat * vec4(UV, 0.0, 1.0)).xy;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
