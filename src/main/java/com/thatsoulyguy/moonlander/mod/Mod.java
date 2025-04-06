package com.thatsoulyguy.moonlander.mod;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Mod
{
    @NotNull String getRegistryName();

    @NotNull String getDisplayName();

    @NotNull Collection<String> getAuthors();

    @NotNull String getDescription();

    void registerPatches();

    void preInitialize();

    void initialize();

    void update();

    void render();

    void uninitialize();
}