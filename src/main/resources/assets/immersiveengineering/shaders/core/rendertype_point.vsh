#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;

out float vertexDistance;
out vec4 vertexColor;

void main() {
	vec4 worldRelative = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * worldRelative;
    gl_Position.xy = gl_Position.xy + 100 * Normal.xy / ScreenSize;

    vertexDistance = length(worldRelative.xyz);
    vertexColor = Color;
}
