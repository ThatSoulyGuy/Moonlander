package com.thatsoulyguy.moonlander.mod;

@FunctionalInterface
public interface MethodAccessor<T>
{
    T invoke(Object instance, Object... args);
}