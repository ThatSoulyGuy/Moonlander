#version 410 core

layout(location = 0) out vec4 gPosition;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gAlbedo;

in vec3 fragmentPosition;
in vec3 normal;
in vec3 color;
in vec2 uvs;

void main()
{
    gPosition = vec4(fragmentPosition, 1.0);

    gNormal = vec4(normalize(normal), 1.0);

    gAlbedo = vec4(0.0f, 0.25f, 0.75f, 0.5f);
}
