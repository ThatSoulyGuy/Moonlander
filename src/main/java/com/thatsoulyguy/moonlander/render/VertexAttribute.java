package com.thatsoulyguy.moonlander.render;

import org.jetbrains.annotations.NotNull;

public record VertexAttribute(@NotNull String name, int count, int type, int offset, boolean isInteger) { }