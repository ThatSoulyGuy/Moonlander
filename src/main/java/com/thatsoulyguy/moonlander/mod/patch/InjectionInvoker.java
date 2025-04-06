package com.thatsoulyguy.moonlander.mod.patch;

@FunctionalInterface
public interface InjectionInvoker
{
    void invoke(Object self, Object[] args);
}