#version 450

precision highp float;
precision highp int;

// Incoming interpolated UV coordinates.
layout (location = 0) in Block
{
    vec2 UV;
};

// Outgoing final color.
layout (location = 0) out vec4 outputColor;

uniform sampler2D textureSampler;

void main()
{
    outputColor = texture(textureSampler, UV).rgba;
}