package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipe;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipeRegistry;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.List;

@CustomConstructor("create")
public class SurvivalCraftingSystem extends Component
{
    private final short[][] slots = new short[2][2];
    private final byte[][] slotCounts = new byte[2][2];

    private short resultSlot = 0;
    private byte resultSlotCount = 0;

    private static SurvivalCraftingSystem instance = null;

    private SurvivalCraftingSystem() { }

    @Override
    public void initialize()
    {
        instance = this;

        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
                panel.getNotNull("ui.slot_" + x + "_" + y + "_text", TextUIElement.class).getGameObject().setActive(false);
        }

        panel.getNotNull("ui.slot_result_text", TextUIElement.class).getGameObject().setActive(false);
    }

    @Override
    public void update()
    {
        List<CraftingRecipe> recipes = CraftingRecipeRegistry.getAll();
        boolean wasMatch = false;

        for (CraftingRecipe recipe : recipes)
        {
            boolean match = CraftingRecipe.matchesRecipe(
                    recipe,
                    Arrays.stream(slots)
                            .map(subArray ->
                            {
                                short[] arr = new short[subArray.length];

                                System.arraycopy(subArray, 0, arr, 0, subArray.length);

                                return arr;
                            })
                            .toArray(short[][]::new)
            );

            if (match)
            {
                resultSlot = recipe.getResult().item().getId();
                resultSlotCount = recipe.getResult().count();

                Inventory.buildSlot("result", new Inventory.SlotData(resultSlot, resultSlotCount), instance.getGameObject());

                wasMatch = true;

                break;
            }
        }

        if (!wasMatch)
        {
            resultSlot = 0;
            resultSlotCount = 0;

            Inventory.buildSlot("result", new Inventory.SlotData(resultSlot, resultSlotCount), instance.getGameObject());
        }
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        SurvivalCraftingSystem instance = SurvivalCraftingSystem.getInstance();

        InventorySystem.GrabbedItem grabbedItem = InventorySystem.getInstance().getGrabbedItem();

        if (element.getGameObject().getName().equals("ui.slot_result"))
        {
            if (instance.resultSlot != 0 && grabbedItem.id().get() == 0)
            {
                Inventory.SlotData oldSlotData = new Inventory.SlotData(instance.resultSlot, instance.resultSlotCount);

                instance.resultSlot = 0;
                instance.resultSlotCount = 0;

                Inventory.buildSlot("result", new Inventory.SlotData((short) 0, (byte) 0), instance.getGameObject());

                grabbedItem.id().set(oldSlotData.id());
                grabbedItem.count().set(oldSlotData.count());

                grabbedItem.image().getGameObject().setActive(true);
                grabbedItem.text().getGameObject().setActive(true);

                InventorySystem.getInstance().buildGrabbedItem();

                for (int x = 0; x < 2; x++)
                {
                    for (int y = 0; y < 2; y++)
                    {
                        if (instance.slotCounts[x][y] > 1)
                            instance.slotCounts[x][y] -= (short) 1;
                        else
                        {
                            instance.slots[x][y] = 0;
                            instance.slotCounts[x][y] = 0;
                        }

                        instance.build(new Vector2i(x, y), new Inventory.SlotData(instance.slots[x][y], instance.slotCounts[x][y]));
                    }
                }
            }

            return;
        }

        Vector2i position = InventorySystem.parseSlot(element.getGameObject().getName());

        if (grabbedItem.id().get() == 0)
        {
            Inventory.SlotData oldSlotData = instance.getSlot(position);

            instance.setSlot(position, new Inventory.SlotData((short) 0, (byte) 0));

            grabbedItem.id().set(oldSlotData.id());
            grabbedItem.count().set(oldSlotData.count());

            grabbedItem.image().getGameObject().setActive(true);
            grabbedItem.text().getGameObject().setActive(true);

            InventorySystem.getInstance().buildGrabbedItem();
        }
        else if (grabbedItem.image().getGameObject().isActive() && instance.getSlot(position).id() == 0)
        {
            instance.setSlot(position, new Inventory.SlotData(grabbedItem.id().get(), grabbedItem.count().get()));

            grabbedItem.id().set((short) 0);
            grabbedItem.count().set((byte) 0);

            grabbedItem.image().getGameObject().setActive(false);
            grabbedItem.text().getGameObject().setActive(false);
        }
        else if (grabbedItem.image().getGameObject().isActive() && instance.getSlot(position).id() == grabbedItem.id().get())
        {
            instance.setSlot(position, new Inventory.SlotData(grabbedItem.id().get(), (byte) (grabbedItem.count().get() + instance.getSlot(position).count())));

            grabbedItem.id().set((short) 0);
            grabbedItem.count().set((byte) 0);

            grabbedItem.image().getGameObject().setActive(false);
            grabbedItem.text().getGameObject().setActive(false);
        }
        else if (grabbedItem.image().getGameObject().isActive() && instance.getSlot(position).id() != grabbedItem.id().get())
        {
            Inventory.SlotData oldSlotData = instance.getSlot(position);

            instance.setSlot(position, new Inventory.SlotData(grabbedItem.id().get(), grabbedItem.count().get()));

            grabbedItem.id().set(oldSlotData.id());
            grabbedItem.count().set(oldSlotData.count());

            InventorySystem.getInstance().buildGrabbedItem();
        }
    }

    public static void onRightMousePressed(@NotNull ButtonUIElement element)
    {
        if (element.getGameObject().getName().equals("ui.slot_result"))
            return;

        Vector2i position = InventorySystem.parseSlot(element.getGameObject().getName());
        SurvivalCraftingSystem instance = SurvivalCraftingSystem.getInstance();

        InventorySystem.GrabbedItem grabbedItem = InventorySystem.getInstance().getGrabbedItem();

        if (grabbedItem.image().getGameObject().isActive() && grabbedItem.id().get() != 0)
        {
            instance.setSlot(position, new Inventory.SlotData(grabbedItem.id().get(), (byte) (instance.getSlot(position).count() + 1)));

            CreativeCraftingSystem.checkGrabbedItem(grabbedItem);
        }
    }

    public static void onHoverBegin(@NotNull ButtonUIElement element)
    {

    }

    public static void onHoverEnd(@NotNull ButtonUIElement element)
    {

    }

    private void build(@NotNull Vector2i position, @NotNull Inventory.SlotData data)
    {
        Inventory.buildSlotWithPosition(position, data, getGameObject());
    }

    private @NotNull Inventory.SlotData getSlot(@NotNull Vector2i position)
    {
        return new Inventory.SlotData(slots[position.x][position.y], slotCounts[position.x][position.y]);
    }

    private void setSlot(@NotNull Vector2i position, @NotNull Inventory.SlotData data)
    {
        slots[position.x][position.y] = data.id();
        slotCounts[position.x][position.y] = data.count();

        build(position, data);
    }

    public static @NotNull SurvivalCraftingSystem getInstance()
    {
        return instance;
    }

    public static @NotNull SurvivalCraftingSystem create()
    {
        return new SurvivalCraftingSystem();
    }
}