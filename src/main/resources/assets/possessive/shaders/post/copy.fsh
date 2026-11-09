#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

// Plain copy of one target into another (the limbo pass cannot read and write minecraft:main at once).
void main() {
    fragColor = texture(InSampler, texCoord);
}
