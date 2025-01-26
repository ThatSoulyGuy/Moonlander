package com.thatsoulyguy.moonlander.block;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.gameplay.OxygenBubble;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.item.Tool;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.ui.menus.InventoryMenu;
import com.thatsoulyguy.moonlander.world.Chunk;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Static
@Manager(Block.class)
public class BlockRegistry
{
    public static final Block BLOCK_AIR = new Block()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "block_air";
        }

        @Override
        public @NotNull String getDisplayName()
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

        @Override
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }
    };

    public static final Block BLOCK_SOFT_MOON_ROCK = new Block()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "block_soft_moon_rock";
        }

        @Override
        public @NotNull String getDisplayName()
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
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
        public @NotNull String getRegistryName()
        {
            return "block_moon_rock";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Moon Rock";
        }

        @Override
        public float getHardness()
        {
            return 8.65f;
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
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
        public @NotNull String getRegistryName()
        {
            return "block_stone";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Stone Block";
        }

        @Override
        public float getHardness()
        {
            return 15.25f;
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
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

    public static final Block BLOCK_REDSTONE_ORE = new Block()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "block_redstone_ore";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Redstone Ore";
        }

        @Override
        public float getHardness()
        {
            return 12.25f;
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
                "redstone_ore",
                "redstone_ore",
                "redstone_ore",
                "redstone_ore",
                "redstone_ore",
                "redstone_ore"
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_REDSTONE_DUST;
        }

        @Override
        public @NotNull Tool toolRequired()
        {
            return Tool.PICKAXE;
        }
    };

    public static final Block BLOCK_COAL_ORE = new Block()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "block_coal_ore";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Coal Ore";
        }

        @Override
        public float getHardness()
        {
            return 10.25f;
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
                "coal_ore",
                "coal_ore",
                "coal_ore",
                "coal_ore",
                "coal_ore",
                "coal_ore"
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_COAL;
        }

        @Override
        public @NotNull Tool toolRequired()
        {
            return Tool.PICKAXE;
        }
    };

    public static final Block BLOCK_ALUMINUM_ORE = new Block()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "block_aluminum_ore";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Aluminum Ore";
        }

        @Override
        public float getHardness()
        {
            return 10.25f;
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
                "aluminum_ore",
                "aluminum_ore",
                "aluminum_ore",
                "aluminum_ore",
                "aluminum_ore",
                "aluminum_ore"
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_ALUMINUM_ORE_BLOCK;
        }

        @Override
        public @NotNull Tool toolRequired()
        {
            return Tool.PICKAXE;
        }
    };

    public static final Block BLOCK_IRON_ORE = new Block()
    {
        @Override
        public @NotNull String getRegistryName()
        {
            return "block_iron_ore";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Iron Ore";
        }

        @Override
        public float getHardness()
        {
            return 10.25f;
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
                "iron_ore",
                "iron_ore",
                "iron_ore",
                "iron_ore",
                "iron_ore",
                "iron_ore"
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_IRON_ORE_BLOCK;
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
        public @NotNull String getRegistryName()
        {
            return "block_crafting_table";
        }

        @Override
        public @NotNull String getDisplayName()
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_CRAFTING_TABLE_BLOCK;
        }
    };

    public static final Block BLOCK_FURNACE = new Block()
    {
        private @Nullable EntityPlayer lastInteractor;

        private final @NotNull Map<Vector3i, LinkedList<Short>> furnaceCookingItemsMap = new HashMap<>();
        private final @NotNull Map<Vector3i, LinkedList<Float>> furnaceCookingTimesMap = new HashMap<>();
        private final @NotNull Map<Vector3i, Integer> coalConsumptionMap = new HashMap<>();

        private final float coalConsumptionCooldownTimerStart = 5.0f;
        private float coalConsumptionCooldownTimer;

        @Override
        public void onInteractedWith(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            if (interactor instanceof EntityPlayer player)
            {
                short currentlySelectedSlotId = Objects.requireNonNull(player.getInventoryMenu().getSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected))).id();

                if (currentlySelectedSlotId != ItemRegistry.ITEM_AIR.getId())
                {
                    lastInteractor = player;

                    if (currentlySelectedSlotId == ItemRegistry.ITEM_COAL.getId())
                    {
                        if (coalConsumptionMap.containsKey(globalBlockPosition))
                        {
                            Integer coalCount = coalConsumptionMap.get(globalBlockPosition);

                            coalCount += 1;

                            coalConsumptionMap.put(globalBlockPosition, coalCount);
                        }
                        else
                            coalConsumptionMap.put(globalBlockPosition, 1);

                        player.getInventoryMenu().setSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected), currentlySelectedSlotId, (byte) (Objects.requireNonNull(player.getInventoryMenu().getSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected))).count() - 1));

                        return;
                    }

                    if (!Objects.requireNonNull(ItemRegistry.get(currentlySelectedSlotId)).isSmeltable())
                        return;

                    if (furnaceCookingItemsMap.containsKey(globalBlockPosition))
                        furnaceCookingItemsMap.get(globalBlockPosition).add(currentlySelectedSlotId);
                    else
                        furnaceCookingItemsMap.put(globalBlockPosition, new LinkedList<>(List.of(currentlySelectedSlotId)));

                    if (furnaceCookingTimesMap.containsKey(globalBlockPosition))
                        furnaceCookingTimesMap.get(globalBlockPosition).add(8.0f);
                    else
                        furnaceCookingTimesMap.put(globalBlockPosition, new LinkedList<>(List.of(7.0f)));

                    player.getInventoryMenu().setSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected), currentlySelectedSlotId, (byte) (Objects.requireNonNull(player.getInventoryMenu().getSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected))).count() - 1));
                }
            }
        }

        @Override
        public void onTick(@NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            if (!coalConsumptionMap.containsKey(globalBlockPosition) || lastInteractor == null)
                return;

            if (coalConsumptionCooldownTimer < 0)
            {
                Integer coal = coalConsumptionMap.get(globalBlockPosition);

                coal -= 1;

                coalConsumptionMap.put(globalBlockPosition, coal);

                coalConsumptionCooldownTimer = coalConsumptionCooldownTimerStart;
            }

            if (coalConsumptionMap.get(globalBlockPosition) <= 0)
                return;

            furnaceCookingTimesMap.forEach((position, list) ->
            {
                Map<Integer, Float> changesToBeMade = new HashMap<>();

                list.forEach(time -> changesToBeMade.put(list.indexOf(time), time - 0.01f));

                changesToBeMade.forEach((index, time) -> furnaceCookingTimesMap.get(position).set(index, time));

                List<Integer> cookTimesToBeRemoved = new ArrayList<>();

                list.forEach(time ->
                {
                    if (time <= 0)
                    {
                        lastInteractor.getInventoryMenu().addItem(Objects.requireNonNull(ItemRegistry.get(furnaceCookingItemsMap.get(position).get(list.indexOf(time)))).getSmeltingResult().getId(), (byte) 1);

                        furnaceCookingItemsMap.get(position).remove(list.indexOf(time));

                        cookTimesToBeRemoved.add(list.indexOf(time));
                    }
                });

                cookTimesToBeRemoved.forEach(index -> furnaceCookingTimesMap.get(position).remove((int) index));
            });

            coalConsumptionCooldownTimer -= 0.01f;
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "block_furnace";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Furnace";
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
                "furnace_top",
                "stone",
                "furnace_side",
                "furnace_side",
                "furnace_side",
                "furnace_side"
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
        public boolean updates()
        {
            return true;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_FURNACE_BLOCK;
        }
    };

    public static final Block BLOCK_OXYGEN_GENERATOR = new Block()
    {
        private final @NotNull Map<Vector3i, GameObject> bubbleMap = new HashMap<>();
        private final @NotNull Map<Vector3i, Integer> coalConsumptionMap = new HashMap<>();

        private final float coalConsumptionCooldownTimerStart = 16.0f;
        private float coalConsumptionCooldownTimer;

        {
            coalConsumptionCooldownTimer = coalConsumptionCooldownTimerStart;
        }

        @Override
        public void onPlaced(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            bubbleMap.put(globalBlockPosition, OxygenBubble.createGameObject("oxygen_bubble_" + globalBlockPosition.x + "_" + globalBlockPosition.y + "_" + globalBlockPosition.z, new Vector3f(globalBlockPosition)));
            bubbleMap.get(globalBlockPosition).getComponentNotNull(OxygenBubble.class).setOxygenActive(false);
            coalConsumptionMap.put(globalBlockPosition, 0);
        }

        @Override
        public void onTick(@NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            bubbleMap.get(globalBlockPosition).getComponentNotNull(OxygenBubble.class).setOxygenActive(coalConsumptionMap.get(globalBlockPosition) > 0);

            if (coalConsumptionCooldownTimer < 0)
            {
                if (coalConsumptionMap.get(globalBlockPosition) > 0)
                    coalConsumptionMap.put(globalBlockPosition, coalConsumptionMap.get(globalBlockPosition) - 1);

                coalConsumptionCooldownTimer = coalConsumptionCooldownTimerStart;
            }

            coalConsumptionCooldownTimer -= Time.getDeltaTime();
        }

        @Override
        public void onInteractedWith(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            if (interactor instanceof EntityPlayer player)
            {
                InventoryMenu.SlotData currentSlot = player.getInventoryMenu().getSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected));

                assert currentSlot != null;

                if (currentSlot.id() == ItemRegistry.ITEM_COAL.getId() && currentSlot.count() > 0)
                {
                    coalConsumptionMap.put(globalBlockPosition, coalConsumptionMap.get(globalBlockPosition) + 1);

                    player.getInventoryMenu().setSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected), currentSlot.id(), (byte) (currentSlot.count() - 1));
                }
            }
        }

        @Override
        public void onBroken(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            GameObjectManager.unregister(bubbleMap.get(globalBlockPosition).getName());

            bubbleMap.remove(globalBlockPosition);
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "block_crafting_table";
        }

        @Override
        public @NotNull String getDisplayName()
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
                "oxygen_generator_top",
                "moon_rock",
                "oxygen_generator_front",
                "oxygen_generator_side",
                "oxygen_generator_side",
                "oxygen_generator_side"
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
        public boolean updates()
        {
            return true;
        }

        @Override
        public boolean isSolid()
        {
            return true;
        }

        @Override
        public @NotNull Item getAssociatedItem()
        {
            return ItemRegistry.ITEM_OXYGEN_GENERATOR_BLOCK;
        }
    };

    public static final Block BLOCK_OIL = new Block()
    {
        @Override
        public void onInteractedWith(@NotNull Entity interactor, @NotNull World world, @NotNull Chunk chunk, @NotNull Vector3i globalBlockPosition)
        {
            if (interactor instanceof EntityPlayer player)
            {
                if (Objects.requireNonNull(player.getInventoryMenu().getSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected))).id() == ItemRegistry.ITEM_EMPTY_BUCKET.getId())
                {
                    player.getInventoryMenu().setSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected), ItemRegistry.ITEM_OIL_BUCKET.getId(), (byte) 1);

                    World.getLocalWorld().setBlock(interactor, new Vector3f(globalBlockPosition), BlockRegistry.BLOCK_AIR.getId());
                }
            }
        }

        @Override
        public @NotNull String getRegistryName()
        {
            return "block_oil";
        }

        @Override
        public @NotNull String getDisplayName()
        {
            return "Oil";
        }

        @Override
        public float getHardness()
        {
            return 0.0f;
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
                "oil",
                "oil",
                "oil",
                "oil",
                "oil",
                "oil"
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
        public boolean updates()
        {
            return false;
        }

        @Override
        public boolean isSolid()
        {
            return false;
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
        register(BLOCK_REDSTONE_ORE);
        register(BLOCK_COAL_ORE);
        register(BLOCK_ALUMINUM_ORE);
        register(BLOCK_IRON_ORE);
        register(BLOCK_OIL);
        register(BLOCK_CRAFTING_TABLE);
        register(BLOCK_FURNACE);
        register(BLOCK_OXYGEN_GENERATOR);
    }

    public static void register(@NotNull Block object)
    {
        blocksByName.putIfAbsent(object.getDisplayName(), object);
        blocksById.putIfAbsent(object.getId(), object);
    }

    public static void unregister(@NotNull String name)
    {
        Block block = blocksByName.getOrDefault(name, null);

        if (block == null)
            return;

        blocksByName.remove(block.getDisplayName());
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