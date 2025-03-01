package com.thatsoulyguy.moonlander.item;

import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class Item
{
    private static short idCounter = 0;

    private final short id;

    public Item()
    {
        id = idCounter++;
    }

    public void onInteractedWith(@NotNull Entity interactor) { }

    public abstract @NotNull String getRegistryName();

    public abstract @NotNull String getDisplayName();

    public abstract @NotNull String getTexture();

    public abstract @NotNull Vector3f getColor();

    public abstract boolean isBlockItem();

    public abstract boolean isSmeltable();

    public @NotNull Item getSmeltingResult()
    {
        return ItemRegistry.ITEM_AIR;
    }

    public @NotNull Block getAssociatedBlock()
    {
        return BlockRegistry.BLOCK_AIR;
    }

    public @NotNull Tool getToolType()
    {
        return Tool.NONE;
    }

    public float getAccossiatedModifier()
    {
        return 1.0f;
    }

    public short getId()
    {
        return id;
    }
}