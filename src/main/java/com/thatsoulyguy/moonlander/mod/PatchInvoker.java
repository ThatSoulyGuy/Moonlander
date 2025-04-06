package com.thatsoulyguy.moonlander.mod;

@FunctionalInterface
public interface PatchInvoker
{
    void invoke(Object self, Object[] args);
}