package com.thatsoulyguy.moonlander.render;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Texture.class)
public class TextureManager
{
    private static final @NotNull ConcurrentMap<String, Texture> registeredTextures = new ConcurrentHashMap<>();

    private TextureManager() { }

    public static void register(@NotNull Texture object)
    {
        registeredTextures.put(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        registeredTextures.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return registeredTextures.containsKey(name);
    }

    public static @Nullable Texture get(@NotNull String name)
    {
        return registeredTextures.getOrDefault(name, null);
    }

    public static @NotNull List<Texture> getAll()
    {
        return List.copyOf(registeredTextures.values());
    }

    public static void uninitialize()
    {
        registeredTextures.values().forEach(Texture::uninitialize_NoOverride);

        registeredTextures.clear();
    }
}