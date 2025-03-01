package com.thatsoulyguy.moonlander.world;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.io.Serializable;

public abstract class RegionalSpawner implements Serializable
{
    public abstract void onSpawnCycle(@NotNull World world, @NotNull Vector3f selectedPosition);

    public static <T extends RegionalSpawner> @NotNull T create(Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Terrain Generator! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}