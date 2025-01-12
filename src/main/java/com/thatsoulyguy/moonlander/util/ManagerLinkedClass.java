package com.thatsoulyguy.moonlander.util;

import org.jetbrains.annotations.NotNull;

public interface ManagerLinkedClass
{
    @NotNull Class<?> getManagingClass();
    @NotNull String getManagedItem();
}
