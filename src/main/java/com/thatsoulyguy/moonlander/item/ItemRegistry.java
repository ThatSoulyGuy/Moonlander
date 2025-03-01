package com.thatsoulyguy.moonlander.item;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.entity.entities.EntityRocket;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
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
        public @NotNull String getRegistryName()
        {
            return "item_air";
        }

        @Override
        public @NotNull String getDisplayName()
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_MOON_ROCK_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_moon_rock_block";
        }

        @Override
        public @NotNull String getDisplayName()
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
        public boolean isSmeltable()
        {
            return false;
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
        public @NotNull String getRegistryName()
        {
            return "item_soft_moon_rock";
        }

        @Override
        public @NotNull String getDisplayName()
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
        public boolean isSmeltable()
        {
            return false;
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
        public @NotNull String getRegistryName()
        {
            return "item_moon_rock_pebble";
        }

        @Override
        public @NotNull String getDisplayName()
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_STONE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_stone";
        }

        @Override
        public @NotNull String getDisplayName()
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
        public boolean isSmeltable()
        {
            return false;
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
        public @NotNull String getRegistryName()
        {
            return "item_stick";
        }

        @Override
        public @NotNull String getDisplayName()
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_REFINED_ALUMINUM_INGOT = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_refined_aluminum_ingot";
        }

        @Override
        public @NotNull String getDisplayName()
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_ALUMINUM_INGOT = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_aluminum_ingot";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Unrefined Aluminum Ingot";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "aluminum_ingot";
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

        @Override
        public boolean isSmeltable()
        {
            return true;
        }

        @Override
        public @NotNull Item getSmeltingResult()
        {
            return ItemRegistry.ITEM_REFINED_ALUMINUM_INGOT;
        }
    };

    public static final Item ITEM_IRON_INGOT = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_iron_ingot";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron Ingot";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "iron_ingot";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_ALUMINUM_ORE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_aluminum_ore_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Aluminum Ore";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "aluminum_ore_block";
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
        public boolean isSmeltable()
        {
            return true;
        }

        @Override
        public @NotNull Item getSmeltingResult()
        {
            return ItemRegistry.ITEM_ALUMINUM_INGOT;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_ALUMINUM_ORE;
        }
    };

    public static final Item ITEM_IRON_ORE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_iron_ore_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron Ore";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "iron_ore_block";
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
        public boolean isSmeltable()
        {
            return true;
        }

        @Override
        public @NotNull Item getSmeltingResult()
        {
            return ItemRegistry.ITEM_IRON_INGOT;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_IRON_ORE;
        }
    };

    public static final Item ITEM_ALUMINIUM_PICKAXE = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_aluminum_pickaxe";
        }

        @Override
        public @NotNull String getDisplayName()
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Tool getToolType()
        {
            return Tool.PICKAXE;
        }

        @Override
        public float getAccossiatedModifier()
        {
            return 0.3f;
        }
    };

    public static final Item ITEM_ALUMINIUM_SWORD = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_aluminum_sword";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Aluminum Sword";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "iron_sword";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Tool getToolType()
        {
            return Tool.SWORD;
        }

        @Override
        public float getAccossiatedModifier()
        {
            return 5.0f;
        }
    };

    public static final Item ITEM_IRON_PICKAXE = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_iron_pickaxe";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron Pickaxe";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "iron_pickaxe";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Tool getToolType()
        {
            return Tool.PICKAXE;
        }

        @Override
        public float getAccossiatedModifier()
        {
            return 0.4f;
        }
    };

    public static final Item ITEM_REDSTONE_DUST = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_redstone_dust";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Redstone Dust";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "redstone_dust";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_COAL = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_coal";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Coal";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "coal";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_EMPTY_BUCKET = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_empty_bucket";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Bucket";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "bucket";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Tool getToolType()
        {
            return Tool.BUCKET;
        }
    };

    public static final Item ITEM_OIL_BUCKET = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_oil_bucket";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Oil Bucket";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "oil_bucket";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_OIL;
        }

        @Override
        public @NotNull Tool getToolType()
        {
            return Tool.BUCKET;
        }
    };

    public static final Item ITEM_FUEL_BUCKET = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_fuel_bucket";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Fuel Bucket";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "fuel_bucket";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_FUEL;
        }

        @Override
        public @NotNull Tool getToolType()
        {
            return Tool.BUCKET;
        }
    };

    public static final Item ITEM_KNOWLEDGE_BOOK = new Item()
    {
        public final @NotNull List<String> pages = List.of
        (
            """
            Hello! Welcome to the Moonlander Tutorial Guide!
            Here, you will learn how to play and progress
            through the game. Flip to the next page to begin
            the tutorial!
            """,
            """
            Notes:
                You have a meter labeled O₂, which indicates
                your oxygen level. When it is in the green,
                your in safe territory. White means you might
                want to refill, and red means that you really
                need to refill unless you want to die.
                \s
                You have hearts to indicate health. If you run
                out of oxygen, you start to lose health. If you
                lose enough health, you die.
                \s
                Mobs will spawn around. You need to kill them,
                or they will kill you. To kill them, left-click
                them. If you left-click them with a sword, you
                can kill them faster.
                \s
                This guide only provides you with the bare
                minimum to beat the game- not any of the fun
                stuff you could do.
                \s
            Move to the next page to continue...
            """,
            """
            Step I:
                Mine soft moon rock (the top layer of moon rock),
                and then open your inventory. Once there, fill all
                4 slots with the 4 soft moon rock you collected.
                Place the crafting table once you have created it.
                The crafting table will expand your crafting grid
                from 4 slots to 9 slots, a NET gain of 5 slots.
            
            Move to the next page to continue...
            """,
            """
            Step II:
                Mine one more soft moon rock, and then place it into
                the crafting grid. You will see it gives you 4 moon
                rock pebbles. Once you have those, put them into the
                crafting grid in a row of two. Like this:
                                       # # #
                                       # P #
                                       # P #
                P = Moon Rock Pebble
                \s
                Then take the resulting moon rock sticks.
            
            Move to the next page to continue...
            """,
            """
            Step III:
                Using your newly acquired sticks, put them into a
                shape which dictates that 2 sticks are in the center-
                bottom section of the 2nd column of the crafting grid,
                whilst the entire 1st row of the crafting grid is filled
                with the refined aluminum ingots you get at the start of
                the game. It should look like this:
                                       A A A
                                       # S #
                                       # S #
                S = Stick
                A = Refined Aluminum Ingot
            
            Move to the next page to continue...
            """,
            """
            Step IV:
                Now, your need to create a sword to defend yourself against
                mobs. In order to do that, you are going to need 2 refined
                aluminum ingots and 1 stick. You should arrange them as follows:
                                       # A #
                                       # A #
                                       # S #
                S = Stick
                A = Refined Aluminum Ingot
            
            Move to the next page to continue...
            """,
            """
            Step V:
                Using your newly crafted aluminum pickaxe, mine 5 moon rock
                (The darker version of what you mined previously). Once you
                have done that, search around until you find a redstone ore
                block, and mine it. A redstone ore block looks like regular
                stone with little red ovals or "crystals" sticking out of it.
                After you have collected 5 moon rock and 1 redstone dust, then
                take your earnings back to your crafting table and lay out
                the resources you gathered as follows:
                                       A A A
                                       M R M
                                       M M M
                A = Refined Aluminum Ingot
                M = Moon Rock
                R = Redstone Dust
                \s
                Once you have arranged the items as follows, you will have
                created an oxygen generator.
            
            Move to the next page to continue...
            """,
            """
            Step VI:
                Place down the oxygen generator you just crafted. At this point,
                your oxygen may be running low, so you will want to quickly find
                and mine coal ore. Coal ore looks the same as redstone ore, except
                the small red crystals are black and don't shimmer. Mine about 5
                coal ore, and then right-click the oxygen generator whilst holding
                the coal to stock the oxygen generator. Then the oxygen generator
                will emit oxygen for some time. You will need to continually restock
                the oxygen generator from time-to-time until you level it up/establish
                a resource grid.
            
            Move to the next page to continue...
            """,
            """
            Step VII:
                You are going to need to craft a furnace to smelt iron and refine aluminum.
                In order to craft this, you need 8 moon rock and 1 redstone. Moon rock is the
                slightly darker variant of Soft Moon Rock that requires a pickaxe to mine. Place
                the redstone in the center, and surround it with moon rock. Layout:
                                       M M M
                                       M R M
                                       M M M
                M = Moon Rock
                R = Redstone
            
            Move to the next page to continue...
            """,
            """
            Step VIII:
                Now, you are going to need to craft a compositor. In order to craft
                this, you need the following materials: 2 Iron Ingots, 2 Refined
                Aluminum Ingots, 1 Redstone, and 4 Soft Moon Rock. In order to get the
                Iron Ingots, find iron ore. Iron ore looks like brownish-white ovals in the
                stone you see in caves. Mine it, and right-click your furnace with coal to
                stock it, then right click it with iron to smelt the iron. It will take a
                few seconds for the iron to smelt. With the aluminum ore, it looks the
                same as the iron ore except it is reddish-white and not brownish-white. The
                difference is that you need to re-smelt the Aluminum ingot after smelting it
                in order to get Refined Aluminum.
            
            Move to the next page to continue...
            """,
            """
            Step IX:
                Once you have gotten your material, you will need put all of your
                Aluminum Ingots in the top-right and top-left slots of the crafting grid,
                and the 2 Iron ingots in the opposite slots. Then, place the redstone in
                center of te grid, and fill the rest with soft moon rock. Layout:
                                       A M A
                                       M R M
                                       I M I
                A = Refined Aluminum Ingot
                M = Soft Moon Rock
                R = Redstone
                I = Iron Ingot
                \s
                Once you have arranged the items as follows, you will have
                created a compositor.
            
            Move to the next page to continue...
            """,
            """
            Step X:
                Now, you are going to need to create a fuel refiner, in order to refine fuel
                for the rocket you are going to make. For this, you will need 3 refined aluminum
                ingots, 3 moon rock, and 3 iron ingots. They must be laid out as such:
                                       A A A
                                       M M M
                                       I I I
                A = Refined Aluminum Ingot
                M = Moon Rock
                I = Iron Ingot
                \s
                Once you have arranged the items as follows, you will have
                created a fuel refiner.
            
            Move to the next page to continue...
            """,
            """
            Step XI:
                You now have all the machines you need to create a rocket ship to get the h-e-double-l
                out of here! The materials you need to create the rocket are 4 iron blocks, 2 aluminum
                blocks, 2 iron+coal composites, and 1 iron+aluminum composite. Crafting the iron and
                aluminum blocks is as you would expect, just fill the entire grid with iron ingots, and
                for aluminum blocks, fill the entire grid with refined (keyword 'refined') iron ingots.
                As for the iron+coal and iron+aluminum composites, you need to go into your compositor,
                and place 1 iron ingot in the top slot for both, and then have the bottom slot be
                refined aluminum ingots or coal depending on what you want to make. This is the recipe
                for the final rocket ship:
                                       I A A
                                       I A I
                                       C V C
                A = Aluminum Block
                C = Iron+Coal Composite
                V = Iron+Aluminum Composite
                I = Iron Block
                \s
                Once you have arranged the items as follows, you will have
                created the rocket.
            
            Move to the next page to continue...
            """,
            """
            Step XII - FINAL:
                Congratulations for making it this far! Now, all you need to do is place the rocket by
                right-clicking the ground whilst holding it, and you will have beaten the game. Thank you
                so much for playing!
                \s
            END
            """
        );

        @Override
        public void onInteractedWith(@NotNull Entity interactor)
        {
            if (interactor instanceof EntityPlayer player)
            {
                player.getBookMenu().setPages(pages);
                player.setPaused(true);
                player.setBookMenuActive(true);

                if (player.getBookMenu().getText().isEmpty())
                    player.getBookMenu().rebuildPages();
            }
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "item_knowledge_book";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Tutorial Guide";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "knowledge_book";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_CRAFTING_TABLE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_crafting_table_block";
        }

        @Override
        public @NotNull String getDisplayName()
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_CRAFTING_TABLE;
        }
    };

    public static final Item ITEM_FURNACE_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_furnace_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Furnace";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "furnace_block";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_FURNACE;
        }
    };

    public static final Item ITEM_OXYGEN_GENERATOR_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_oxygen_generator_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Oxygen Generator";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "oxygen_generator";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_OXYGEN_GENERATOR;
        }
    };

    public static final Item ITEM_COMPOSITOR_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_compositor_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Compositor";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "compositor_block";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_COMPOSITOR;
        }
    };

    public static final Item ITEM_FUEL_REFINER = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_fuel_refiner";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Fuel Refiner";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "fuel_refiner_block";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_FUEL_REFINER;
        }
    };

    public static final Item ITEM_ALUMINUM_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_aluminum_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Aluminum Block";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "aluminum_block";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_ALUMINUM_BLOCK;
        }
    };

    public static final Item ITEM_IRON_BLOCK = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_iron_block";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron Block";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "iron_block";
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
        public boolean isSmeltable()
        {
            return false;
        }

        @Override
        public @NotNull Block getAssociatedBlock()
        {
            return BlockRegistry.BLOCK_IRON_BLOCK;
        }
    };

    public static final Item ITEM_ROCKET_ENTITY = new Item()
    {
        @Override
        public void onInteractedWith(@NotNull Entity interactor)
        {
            if (interactor instanceof EntityPlayer player)
            {
                World.getLocalWorld().spawnEntity(player.getGameObject().getTransform().getWorldPosition().add(new Vector3f(0.0f, 0.0f, 10.0f)), EntityRocket.class);
                player.getInventoryMenu().setSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected), ItemRegistry.ITEM_AIR.getId(), (byte) 1);
            }
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "item_rocket_entity";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Rocket (completes the game)";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "rocket_entity";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_IRONCOAL_COMPOSITE = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_ironcoal_composite";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron+Coal Composite";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "ironcoal_composite";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
        }
    };

    public static final Item ITEM_IRONALUMINUM_COMPOSITE = new Item()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "item_ironaluminum_composite";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron+Refined Aluminum Ingot Composite";
        }

        @Override
        public @NotNull String getTexture()
        {
            return "ironaluminum_composite";
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

        @Override
        public boolean isSmeltable()
        {
            return false;
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
        register(ITEM_ALUMINUM_ORE_BLOCK);
        register(ITEM_IRON_ORE_BLOCK);
        register(ITEM_STONE_BLOCK);
        register(ITEM_STICK);
        register(ITEM_REFINED_ALUMINUM_INGOT);
        register(ITEM_ALUMINUM_INGOT);
        register(ITEM_ALUMINIUM_SWORD);
        register(ITEM_IRON_INGOT);
        register(ITEM_ALUMINIUM_PICKAXE);
        register(ITEM_IRON_PICKAXE);
        register(ITEM_REDSTONE_DUST);
        register(ITEM_COAL);
        register(ITEM_EMPTY_BUCKET);
        register(ITEM_OIL_BUCKET);
        register(ITEM_FUEL_BUCKET);
        register(ITEM_KNOWLEDGE_BOOK);
        register(ITEM_CRAFTING_TABLE_BLOCK);
        register(ITEM_FURNACE_BLOCK);
        register(ITEM_OXYGEN_GENERATOR_BLOCK);
        register(ITEM_COMPOSITOR_BLOCK);
        register(ITEM_FUEL_REFINER);
        register(ITEM_ALUMINUM_BLOCK);
        register(ITEM_IRON_BLOCK);
        register(ITEM_ROCKET_ENTITY);
        register(ITEM_IRONCOAL_COMPOSITE);
        register(ITEM_IRONALUMINUM_COMPOSITE);
    }

    public static void register(@NotNull Item object)
    {
        itemsByName.putIfAbsent(object.getDisplayName(), object);
        itemsById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        Item item = itemsByName.getOrDefault(name, null);

        if (item == null)
            return;

        itemsByName.remove(item.getDisplayName());
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