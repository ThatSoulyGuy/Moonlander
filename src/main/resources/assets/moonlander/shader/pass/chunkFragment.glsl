#version 410 core

layout(location = 0) out vec4 gPosition;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gAlbedo;

in vec3 fragmentPosition;
in vec3 normal;
in vec3 color;
in vec2 mergedUV;
in vec2 atlasOffset;
in vec2 atlasTileSize;

const double pad = 0.001;

uniform sampler2D diffuse;

void main()
{
    gPosition = vec4(fragmentPosition, 1.0);
    gNormal = vec4(normalize(normal), 1.0);

    dvec2 uv = fract(dvec2(mergedUV.x, mergedUV.y));

    uv = uv * (1.0 - 2.0 * pad) + pad;

    dvec2 tiledUV = dvec2(atlasOffset) + uv * dvec2(atlasTileSize);

    gAlbedo = texture(diffuse, vec2(tiledUV)) * vec4(color, 1.0);
}
