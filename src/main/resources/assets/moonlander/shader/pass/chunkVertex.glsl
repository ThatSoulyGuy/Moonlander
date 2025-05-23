#version 410 core

layout(location = 0) in vec3 positionIn;
layout(location = 1) in vec3 colorIn;
layout(location = 2) in vec3 normalIn;
layout(location = 3) in vec2 uvsIn;
layout(location = 4) in vec2 atlasOffsetIn;
layout(location = 5) in vec2 atlasTileSizeIn;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 fragmentPosition;
out vec3 normal;
out vec3 color;
out vec2 mergedUV;
out vec2 atlasOffset;
out vec2 atlasTileSize;

void main()
{
    vec4 viewPosition = view * model * vec4(positionIn, 1.0);
    gl_Position = projection * viewPosition;
    fragmentPosition = viewPosition.xyz;
    normal = transpose(inverse(mat3(view * model))) * normalIn;
    color = colorIn;
    mergedUV = uvsIn;
    atlasOffset = atlasOffsetIn;
    atlasTileSize = atlasTileSizeIn;
}
