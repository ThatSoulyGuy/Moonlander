package com.thatsoulyguy.moonlander.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@FunctionalInterface
public interface SerializableConsumer<T> extends Serializable
{
    void accept(@NotNull T value);
}
