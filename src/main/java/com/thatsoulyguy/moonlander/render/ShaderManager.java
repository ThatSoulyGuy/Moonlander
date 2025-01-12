package com.thatsoulyguy.moonlander.render;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Shader.class)
public class ShaderManager
{
    private static final @NotNull ConcurrentMap<String, Shader> registeredShaders = new ConcurrentHashMap<>();

    private ShaderManager() { }

    public static void register(@NotNull Shader object)
    {
        registeredShaders.put(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        registeredShaders.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return registeredShaders.containsKey(name);
    }

    public static @Nullable Shader get(@NotNull String name)
    {
        return registeredShaders.getOrDefault(name, null);
    }

    public static @NotNull List<Shader> getAll()
    {
        return List.copyOf(registeredShaders.values());
    }

    public static void uninitialize()
    {
        registeredShaders.values().forEach(Shader::uninitialize_NoOverride);

        registeredShaders.clear();
    }
}