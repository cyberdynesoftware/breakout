#version 330 core
layout (location = 0) in vec4 vertex; // <vec2 position, vec2 texCoords>

out vec2 TexCoords;
out vec4 ParticleColor;

uniform mat4 projection;
uniform vec3 offset;
uniform vec3 color;

void main()
{
    float scale = 10.0f;
    TexCoords = vertex.zw;
    ParticleColor = vec4(color, 1);
    gl_Position = projection * vec4((vertex.xy * scale) + offset.xy, 0.0, 1.0);
}
