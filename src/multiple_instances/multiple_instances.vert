
#version 450

precision highp float;
precision highp int;

// Incoming vertex position, Model Space.
layout (location = 0) in vec3 position;

// Incomin normal
layout (location = 1) in vec2 uv;

// Projection and view matrices.
layout (binding = 1) uniform Transform0
{
    mat4 proj;
    mat4 view;
};

// model matrix
layout (binding = 2) uniform Transform1
{
    mat4 model;
};

// Outgoing color.
layout (location = 0) out Block
{
    vec2 UV;
};

void main() {

    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position,  1)));

    // Set the output UV coordinates
    UV = uv;
}
