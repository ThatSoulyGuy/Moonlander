package com.thatsoulyguy.moonlander.mod.patch;

@FunctionalInterface
public interface PatchInvoker
{
    void invoke(Object self, Object[] args);
}