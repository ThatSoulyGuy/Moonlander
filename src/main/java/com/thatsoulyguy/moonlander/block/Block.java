package com.thatsoulyguy.moonlander.block;

import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.item.Tool;
import com.thatsoulyguy.moonlander.world.Chunk;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;

public abstract class Block
{
    private static short idCounter = 0;

    private final short id;

    public Block()
    {
        id = idCounter++;
    }

    public void onPlaced(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition) { }

    public void onTick(@NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition) { }

    public void onInteractedWith(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition) { }

    public void onBroken(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition) { }

    public abstract @NotNull String getRegistryName();

    public abstract @NotNull String getDisplayName();

    public abstract float getHardness();

    public abstract float getResistance();

    public abstract @NotNull String[] getTextures();

    public abstract @NotNull Vector3f[] getColors();

    public abstract boolean isInteractable();

    public abstract boolean updates();

    public abstract boolean isSolid();

    public abstract @NotNull List<AudioClip> getMiningAudioClips();

    public abstract @NotNull List<AudioClip> getBrokenAudioClips();

    public @NotNull Item getAssociatedItem()
    {
        return ItemRegistry.ITEM_AIR;
    }

    public @NotNull Tool toolRequired()
    {
        return Tool.NONE;
    }

    public short getId()
    {
        return id;
    }
}