package com.thatsoulyguy.moonlander.block;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.item.Tool;
import com.thatsoulyguy.moonlander.world.Chunk;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Block.class)
public class BlockRegistry
{
    public static final Block BLOCK_AIR = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_air";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "";
        }

        @Override
        public float getHardness()
        {
            return 0.0f;
        }

        @Override
        public float getResistance()
        {
            return 0;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {

            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
                new Vector3f(0.0f),
            };
        }

        @Override
        public boolean isInteractable()
        {
            return false;
        }
    };

    public static final Block BLOCK_SOFT_MOON_ROCK = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_soft_moon_rock";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Soft Moon Rock";
        }

        @Override
        public float getHardness()
        {
            return 2.65f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "soft_moon_rock",
                "soft_moon_rock",
                "soft_moon_rock",
                "soft_moon_rock",
                "soft_moon_rock",
                "soft_moon_rock"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }

        @Override
        public boolean isInteractable()
        {
            return false;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_SOFT_MOON_ROCK_BLOCK;
        }
    };

    public static final Block BLOCK_MOON_ROCK = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_moon_rock";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Moon Rock";
        }

        @Override
        public float getHardness()
        {
            return 10.65f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "moon_rock",
                "moon_rock",
                "moon_rock",
                "moon_rock",
                "moon_rock",
                "moon_rock"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }

        @Override
        public boolean isInteractable()
        {
            return false;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_MOON_ROCK_BLOCK;
        }

        @Override
        public @NotNull Tool toolRequired()
        {
            return Tool.PICKAXE;
        }
    };

    public static final Block BLOCK_STONE = new Block()
    {
        @Override
        public @NotNull String getDisplayName()
        {
            return "block_stone";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Stone Block";
        }

        @Override
        public float getHardness()
        {
            return 20.25f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "stone",
                "stone",
                "stone",
                "stone",
                "stone",
                "stone"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }

        @Override
        public boolean isInteractable()
        {
            return false;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_STONE_BLOCK;
        }

        @Override
        public @NotNull Tool toolRequired()
        {
            return Tool.PICKAXE;
        }
    };

    public static final Block BLOCK_CRAFTING_TABLE = new Block()
    {
        @Override
        public void onInteractedWith(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            if (interactor instanceof EntityPlayer player)
                player.setCraftingTableMenuActive(!player.isCraftingTableMenuActive());
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "block_crafting_table";
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "Crafting Table";
        }

        @Override
        public float getHardness()
        {
            return 2.84f;
        }

        @Override
        public float getResistance()
        {
            return 0.1f;
        }

        @Override
        public @NotNull String[] getTextures()
        {
            return new String[]
            {
                "crafting_table_top",
                "soft_moon_rock",
                "crafting_table_front",
                "crafting_table_side",
                "crafting_table_side",
                "crafting_table_side"
            };
        }

        @Override
        public @NotNull Vector3f[] getColors()
        {
            return new Vector3f[]
            {
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
                new Vector3f(1.0f),
            };
        }

        @Override
        public boolean isInteractable()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_CRAFTING_TABLE_BLOCK;
        }
    };

    private static final ConcurrentMap<String, Block> blocksByName = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Short, Block> blocksById = new ConcurrentHashMap<>();

    private BlockRegistry() { }

    public static void initialize()
    {
        register(BLOCK_AIR);
        register(BLOCK_SOFT_MOON_ROCK);
        register(BLOCK_MOON_ROCK);
        register(BLOCK_STONE);
        register(BLOCK_CRAFTING_TABLE);
    }

    public static void register(@NotNull Block object)
    {
        blocksByName.putIfAbsent(object.getRegistryName(), object);
        blocksById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        Block block = blocksByName.getOrDefault(name, null);

        if (block == null)
            return;

        blocksByName.remove(block.getRegistryName());
        blocksById.remove(block.getId());
    }

    public static boolean has(@NotNull String name)
    {
        return blocksByName.containsKey(name);
    }

    public static @Nullable Block get(@NotNull String name)
    {
        return blocksByName.getOrDefault(name, null);
    }

    public static @Nullable Block get(short id)
    {
        return blocksById.getOrDefault(id, null);
    }

    public static @NotNull List<Block> getAll()
    {
        return List.copyOf(blocksByName.values());
    }

    public static void uninitialize() { }
}