package com.thatsoulyguy.moonlander.render;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record VertexLayout(@NotNull List<VertexAttribute> attributes, int stride) { }