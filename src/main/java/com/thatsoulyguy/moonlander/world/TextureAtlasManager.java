package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(TextureAtlas.class)
public class TextureAtlasManager
{
    private static final @NotNull ConcurrentMap<String, TextureAtlas> textureAtlasMap = new ConcurrentHashMap<>();

    private TextureAtlasManager() { }

    public static void register(@NotNull TextureAtlas object)
    {
        textureAtlasMap.putIfAbsent(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        textureAtlasMap.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return textureAtlasMap.containsKey(name);
    }

    public static @Nullable TextureAtlas get(@NotNull String name)
    {
        if (!textureAtlasMap.containsKey(name))
            return null;

        return textureAtlasMap.get(name);
    }

    public static @NotNull List<TextureAtlas> getAll()
    {
        return List.copyOf(textureAtlasMap.values());
    }

    public static void uninitialize()
    {
        textureAtlasMap.values().forEach(TextureAtlas::uninitialize_NoOverride);

        textureAtlasMap.clear();
    }
}