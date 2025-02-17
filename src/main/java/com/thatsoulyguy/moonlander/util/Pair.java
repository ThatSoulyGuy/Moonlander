package com.thatsoulyguy.moonlander.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public record Pair<T, A>(@NotNull T t, @NotNull A a) implements Serializable
{
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Pair<?, ?> other = (Pair<?, ?>) obj;

        return t.equals(other.t()) && a.equals(other.a());
    }
}