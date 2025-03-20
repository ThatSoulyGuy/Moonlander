package com.thatsoulyguy.moonlander.util;

import java.io.Serializable;

@FunctionalInterface
public interface DoubleConsumer<T, A> extends Serializable
{
    void run(T t, A a);
}