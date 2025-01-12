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

    public static final @NotNull CraftingRecipe REFINED_ALUMINUM_INGOT_TO_ALUMINUM_PICKAXE = new CraftingRecipe()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "recipe_refined_aluminum_ingot_to_aluminum_pickaxe";
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

    private static final @NotNull ConcurrentMap<String, CraftingRecipe> recipesByName = new ConcurrentHashMap<>();
    private static final @NotNull ConcurrentMap<Short, CraftingRecipe> recipesById = new ConcurrentHashMap<>();
    
    private CraftingRecipeRegistry() { }

    public static void initialize()
    {
        register(MOON_ROCK_TO_MOON_ROCK_PEBBLE);
        register(MOON_ROCK_PEBBLE_TO_STICK);
        register(MOON_ROCK_TO_CRAFTING_TABLE);
        register(REFINED_ALUMINUM_INGOT_TO_ALUMINUM_PICKAXE);
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