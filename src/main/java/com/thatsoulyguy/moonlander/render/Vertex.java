package com.thatsoulyguy.moonlander.render;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.FloatBuffer;

public interface Vertex extends Serializable
{
    @NotNull VertexLayout getVertexLayout();

    void putAttributeData(int attributeIndex, @NotNull FloatBuffer buffer);
}