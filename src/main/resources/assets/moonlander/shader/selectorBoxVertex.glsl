#version 410 core

layout(location = 0) in vec3 inPosition;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

out vec3 fragmentPosition;

void main()
{
    gl_Position = projection * view * model * vec4(inPosition, 1.0);

    fragmentPosition = inPosition;
}
