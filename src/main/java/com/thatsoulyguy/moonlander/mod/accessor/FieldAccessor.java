package com.thatsoulyguy.moonlander.mod.accessor;

import org.jetbrains.annotations.NotNull;

public interface FieldAccessor<T>
{
    T get(@NotNull Object instance);

    void set(@NotNull Object instance, T value);
}