#version 410 core

layout(location = 0) out vec4 gPosition;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gAlbedo;

in vec3 position;
in vec3 TexCoords;

uniform samplerCube skybox;

void main()
{
    gPosition = vec4(position, 1.0f);
    gNormal = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    gAlbedo = texture(skybox, TexCoords);
}