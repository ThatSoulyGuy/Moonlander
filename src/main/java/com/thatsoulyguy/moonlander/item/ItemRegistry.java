package com.thatsoulyguy.moonlander.item;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Item.class)
public class ItemRegistry
{
    public static final Item ITEM_AIR = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_air";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(0.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_MOON_ROCK_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_moon_rock_block";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Moon Rock";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "moon_rock_block";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_MOON_ROCK;
        }
    };

    public static final Item ITEM_SOFT_MOON_ROCK_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_soft_moon_rock";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Soft Moon Rock";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "soft_moon_rock_block";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_SOFT_MOON_ROCK;
        }
    };

    public static final Item ITEM_MOON_ROCK_PEBBLE = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_moon_rock_pebble";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Moon Rock Pebble";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "moon_rock_pebble";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_STONE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_stone";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Stone Item";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "stone_block";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_STONE;
        }
    };

    public static final Item ITEM_STICK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_stick";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Stick";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "stick";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_REFINED_ALUMINUM_INGOT = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_refined_aluminum_ingot";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Refined Aluminum Ingot";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "refined_aluminum_ingot";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_ALUMINIUM_PICKAXE = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_aluminum_pickaxe";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Aluminum Pickaxe";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "aluminum_pickaxe";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return false;
        }
    };

    public static final Item ITEM_CRAFTING_TABLE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "item_crafting_table";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Crafting Table";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "crafting_table";
        }

        @Override
        public @NotNull Vector3f getColor()
        {
            return new Vector3f(1.0f);
        }

        @Override
        public boolean isBlockItem()
        {
            return true;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_CRAFTING_TABLE;
        }
    };

    private static final ConcurrentMap<String, Item> itemsByName = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Short, Item> itemsById = new ConcurrentHashMap<>();

    private ItemRegistry() { }

    public static void initialize()
    {
        register(ITEM_AIR);
        register(ITEM_MOON_ROCK_BLOCK);
        register(ITEM_SOFT_MOON_ROCK_BLOCK);
        register(ITEM_MOON_ROCK_PEBBLE);
        register(ITEM_STONE_BLOCK);
        register(ITEM_STICK);
        register(ITEM_REFINED_ALUMINUM_INGOT);
        register(ITEM_ALUMINIUM_PICKAXE);
        register(ITEM_CRAFTING_TABLE_BLOCK);
    }

    public static void register(@NotNull Item object)
    {
        itemsByName.putIfAbsent(object.getRegistryName(), object);
        itemsById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        Item item = itemsByName.getOrDefault(name, null);

        if (item == null)
            return;

        itemsByName.remove(item.getRegistryName());
        itemsById.remove(item.getId());
    }

    public static boolean has(@NotNull String name)
    {
        return itemsByName.containsKey(name);
    }

    public static @Nullable Item get(@NotNull String name)
    {
        return itemsByName.getOrDefault(name, null);
    }

    public static @Nullable Item get(short id)
    {
        return itemsById.getOrDefault(id, null);
    }

    public static @NotNull List<Item> getAll()
    {
        return List.copyOf(itemsByName.values());
    }

    public static void uninitialize() { }
}