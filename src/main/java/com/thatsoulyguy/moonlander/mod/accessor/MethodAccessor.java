package com.thatsoulyguy.moonlander.mod.accessor;

@FunctionalInterface
public interface MethodAccessor<T>
{
    T invoke(Object instance, Object... args);
}