package com.thatsoulyguy.moonlander.crafting;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(CraftingRecipe.class)
public class CraftingRecipeRegistry
{
    public static final @NotNull CraftingRecipe MOON_ROCK_TO_MOON_ROCK_PEBBLE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_moon_rock_to_moon_rock_pebble";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'm', ItemRegistry.ITEM_SOFT_MOON_ROCK_BLOCK
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'm' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_MOON_ROCK_PEBBLE, (byte) 4);
        }
    };

    public static final @NotNull CraftingRecipe MOON_ROCK_PEBBLE_TO_STICK = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_moon_rock_pebble_to_stick";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'p', ItemRegistry.ITEM_MOON_ROCK_PEBBLE
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'p' },
                new char[] { 'p' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_STICK, (byte) 4);
        }
    };

    public static final @NotNull CraftingRecipe MOON_ROCK_TO_CRAFTING_TABLE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_moon_rock_to_crafting_table";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'p', ItemRegistry.ITEM_SOFT_MOON_ROCK_BLOCK
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'p', 'p' },
                new char[] { 'p', 'p' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_CRAFTING_TABLE_BLOCK, (byte) 1);
        }
    };

    public static final @NotNull CraftingRecipe REFINED_ALUMINUM_INGOT_AND_STICK_TO_ALUMINUM_PICKAXE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_refined_aluminum_ingot_and_stick_to_aluminum_pickaxe";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                's', ItemRegistry.ITEM_STICK,
                'a', ItemRegistry.ITEM_REFINED_ALUMINUM_INGOT
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'a', 'a', 'a' },
                new char[] { '#', 's', '#' },
                new char[] { '#', 's', '#' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_ALUMINIUM_PICKAXE, (byte) 1);
        }
    };

    public static final @NotNull CraftingRecipe IRON_INGOT_AND_STICK_TO_ALUMINUM_PICKAXE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_iron_ingot_and_stick_to_aluminum_pickaxe";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                's', ItemRegistry.ITEM_STICK,
                'i', ItemRegistry.ITEM_IRON_INGOT
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'i', 'i', 'i' },
                new char[] { '#', 's', '#' },
                new char[] { '#', 's', '#' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_IRON_PICKAXE, (byte) 1);
        }
    };

    public static final @NotNull CraftingRecipe IRON_INGOT_TO_BUCKET = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_iron_ingot_to_bucket";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'i', ItemRegistry.ITEM_IRON_INGOT
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { '#', '#', '#' },
                new char[] { 'i', '#', 'i' },
                new char[] { '#', 'i', '#' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_EMPTY_BUCKET, (byte) 1);
        }
    };

    public static final @NotNull CraftingRecipe REFINED_ALUMINUM_INGOT_AND_REDSTONE_DUST_AND_MOON_ROCK_TO_OXYGEN_GENERATOR = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_refined_aluminum_ingot_and_redstone_dust_and_moon_rock_to_oxygen_generator";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'a', ItemRegistry.ITEM_REFINED_ALUMINUM_INGOT,
                'r', ItemRegistry.ITEM_REDSTONE_DUST,
                'm', ItemRegistry.ITEM_MOON_ROCK_BLOCK
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'a', 'a', 'a' },
                new char[] { 'm', 'r', 'm' },
                new char[] { 'm', 'm', 'm' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_OXYGEN_GENERATOR_BLOCK, (byte) 1);
        }
    };

    public static final @NotNull CraftingRecipe STONE_AND_REDSTONE_TO_FURNACE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_stone_and_redstone_to_furnace";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                's', ItemRegistry.ITEM_STONE_BLOCK,
                'r', ItemRegistry.ITEM_REDSTONE_DUST
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 's', 's', 's' },
                new char[] { 's', 'r', 's' },
                new char[] { 's', 's', 's' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_FURNACE_BLOCK, (byte) 1);
        }
    };

    public static final @NotNull CraftingRecipe MOON_ROCK_AND_REDSTONE_TO_FURNACE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_moon_rock_and_redstone_to_furnace";
        }

        @Override
        public @NotNull Map<Character, Item> getKeyDefinitions()
        {
            return Map.of
            (
                'm', ItemRegistry.ITEM_MOON_ROCK_BLOCK,
                'r', ItemRegistry.ITEM_REDSTONE_DUST
            );
        }

        @Override
        public char[][] getRecipeGrid()
        {
            return new char[][]
            {
                new char[] { 'm', 'm', 'm' },
                new char[] { 'm', 'r', 'm' },
                new char[] { 'm', 'm', 'm' }
            };
        }

        @Override
        public @NotNull Result getResult()
        {
            return new Result(ItemRegistry.ITEM_FURNACE_BLOCK, (byte) 1);
        }
    };

    private static final @NotNull ConcurrentMap<String, CraftingRecipe> recipesByName = new ConcurrentHashMap<>();
    private static final @NotNull ConcurrentMap<Short, CraftingRecipe> recipesById = new ConcurrentHashMap<>();
    
    private CraftingRecipeRegistry() { }

    public static void initialize()
    {
        register(MOON_ROCK_TO_MOON_ROCK_PEBBLE);
        register(MOON_ROCK_PEBBLE_TO_STICK);
        register(MOON_ROCK_TO_CRAFTING_TABLE);
        register(REFINED_ALUMINUM_INGOT_AND_REDSTONE_DUST_AND_MOON_ROCK_TO_OXYGEN_GENERATOR);
        register(REFINED_ALUMINUM_INGOT_AND_STICK_TO_ALUMINUM_PICKAXE);
        register(IRON_INGOT_AND_STICK_TO_ALUMINUM_PICKAXE);
        register(IRON_INGOT_TO_BUCKET);
        register(STONE_AND_REDSTONE_TO_FURNACE);
        register(MOON_ROCK_AND_REDSTONE_TO_FURNACE);
    }

    public static void register(@NotNull CraftingRecipe object)
    {
        recipesByName.putIfAbsent(object.getRegistryName(), object);
        recipesById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        CraftingRecipe CraftingRecipe = recipesByName.getOrDefault(name, null);

        if (CraftingRecipe == null)
            return;

        recipesByName.remove(CraftingRecipe.getRegistryName());
        recipesById.remove(CraftingRecipe.getId());
    }

    public static boolean has(@NotNull String name)
    {
        return recipesByName.containsKey(name);
    }

    public static @Nullable CraftingRecipe get(@NotNull String name)
    {
        return recipesByName.getOrDefault(name, null);
    }

    public static @Nullable CraftingRecipe get(short id)
    {
        return recipesById.getOrDefault(id, null);
    }

    public static @NotNull List<CraftingRecipe> getAll()
    {
        return List.copyOf(recipesByName.values());
    }

    public static void uninitialize() { }
}