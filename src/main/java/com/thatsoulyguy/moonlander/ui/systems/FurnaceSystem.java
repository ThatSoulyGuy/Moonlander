package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipe;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipeRegistry;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CustomConstructor("create")
public class FurnaceSystem extends Component
{
    private short upperSlot = 0, lowerSlot = 0;
    private byte upperSlotCount = 0, lowerSlotCount = 0;

    private short resultSlot = 0;
    private byte resultSlotCount = 0;

    private static FurnaceSystem instance = null;

    private FurnaceSystem() { }

    @Override
    public void initialize()
    {
        instance = this;

        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        panel.getNotNull("ui.slot_upper_text", TextUIElement.class).getGameObject().setActive(false);
        panel.getNotNull("ui.slot_lower_text", TextUIElement.class).getGameObject().setActive(false);
        panel.getNotNull("ui.slot_result_text", TextUIElement.class).getGameObject().setActive(false);
    }

    @Override
    public void update()
    {
        List<Item> smeltableItems = ItemRegistry.getAll().stream()
                .filter(Item::isSmeltable)
                .toList();

        boolean wasMatch = false;

        for (Item item : smeltableItems)
        {
            if (upperSlot == item.getId() && lowerSlot == ItemRegistry.ITEM_COAL.getId())
            {
                resultSlot = item.getSmeltingResult().getId();
                resultSlotCount = 1;

                Inventory.buildSlot("result", new Inventory.SlotData(item.getSmeltingResult().getId(), (byte) 1), instance.getGameObject());

                wasMatch = true;
                break;
            }
        }

        if (!wasMatch)
        {
            resultSlot = 0;
            resultSlotCount = 0;

            Inventory.buildSlot("result", new Inventory.SlotData((short) 0, (byte) 0), instance.getGameObject());
        }
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        FurnaceSystem instance = FurnaceSystem.getInstance();

        InventorySystem.GrabbedItem grabbedItem = InventorySystem.getInstance().getGrabbedItem();

        if (element.getGameObject().getName().equals("ui.slot_lower") && grabbedItem.image().getGameObject().isActive() && grabbedItem.id().get() != ItemRegistry.ITEM_COAL.getId())
            return;

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

                {
                    if (instance.upperSlotCount > 1)
                        instance.upperSlotCount -= (short) 1;
                    else
                    {
                        instance.upperSlot = 0;
                        instance.upperSlotCount = 0;
                    }

                    Inventory.buildSlot("upper", new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount), instance.getGameObject());

                    if (instance.lowerSlotCount > 1)
                        instance.lowerSlotCount -= (short) 1;
                    else
                    {
                        instance.lowerSlot = 0;
                        instance.lowerSlotCount = 0;
                    }

                    Inventory.buildSlot("lower", new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount), instance.getGameObject());
                }
            }

            return;
        }

        boolean isUpperSlot = element.getGameObject().getName().equals("ui.slot_upper");

        if (isUpperSlot)
        {
            if (grabbedItem.id().get() == 0)
            {
                Inventory.SlotData oldSlotData = new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount);

                instance.upperSlot = 0;
                instance.upperSlotCount = 0;

                Inventory.buildSlot("upper", new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount), instance.getGameObject());

                grabbedItem.id().set(oldSlotData.id());
                grabbedItem.count().set(oldSlotData.count());

                grabbedItem.image().getGameObject().setActive(true);
                grabbedItem.text().getGameObject().setActive(true);

                InventorySystem.getInstance().buildGrabbedItem();
            }
            else if (grabbedItem.image().getGameObject().isActive() && instance.upperSlot == 0)
            {
                instance.upperSlot = grabbedItem.id().get();
                instance.upperSlotCount = grabbedItem.count().get();

                Inventory.buildSlot("upper", new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount), instance.getGameObject());

                grabbedItem.id().set((short) 0);
                grabbedItem.count().set((byte) 0);

                grabbedItem.image().getGameObject().setActive(false);
                grabbedItem.text().getGameObject().setActive(false);
            }
            else if (grabbedItem.image().getGameObject().isActive() && instance.upperSlot == grabbedItem.id().get())
            {
                instance.upperSlot = grabbedItem.id().get();
                instance.upperSlotCount = (byte) (grabbedItem.count().get() + instance.upperSlotCount);

                Inventory.buildSlot("upper", new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount), instance.getGameObject());

                grabbedItem.id().set((short) 0);
                grabbedItem.count().set((byte) 0);

                grabbedItem.image().getGameObject().setActive(false);
                grabbedItem.text().getGameObject().setActive(false);
            }
            else if (grabbedItem.image().getGameObject().isActive() && instance.upperSlot != grabbedItem.id().get())
            {
                Inventory.SlotData oldSlotData = new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount);

                instance.upperSlot = grabbedItem.id().get();
                instance.upperSlotCount = grabbedItem.count().get();

                Inventory.buildSlot("upper", new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount), instance.getGameObject());

                grabbedItem.id().set(oldSlotData.id());
                grabbedItem.count().set(oldSlotData.count());

                InventorySystem.getInstance().buildGrabbedItem();
            }
        }
        else
        {
            if (grabbedItem.id().get() == 0)
            {
                Inventory.SlotData oldSlotData = new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount);

                instance.lowerSlot = 0;
                instance.lowerSlotCount = 0;

                Inventory.buildSlot("lower", new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount), instance.getGameObject());

                grabbedItem.id().set(oldSlotData.id());
                grabbedItem.count().set(oldSlotData.count());

                grabbedItem.image().getGameObject().setActive(true);
                grabbedItem.text().getGameObject().setActive(true);

                InventorySystem.getInstance().buildGrabbedItem();
            }
            else if (grabbedItem.image().getGameObject().isActive() && instance.lowerSlot == 0)
            {
                instance.lowerSlot = grabbedItem.id().get();
                instance.lowerSlotCount = grabbedItem.count().get();

                Inventory.buildSlot("lower", new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount), instance.getGameObject());

                grabbedItem.id().set((short) 0);
                grabbedItem.count().set((byte) 0);

                grabbedItem.image().getGameObject().setActive(false);
                grabbedItem.text().getGameObject().setActive(false);
            }
            else if (grabbedItem.image().getGameObject().isActive() && instance.lowerSlot == grabbedItem.id().get())
            {
                instance.lowerSlot = grabbedItem.id().get();
                instance.lowerSlotCount = (byte) (grabbedItem.count().get() + instance.lowerSlotCount);

                Inventory.buildSlot("lower", new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount), instance.getGameObject());

                grabbedItem.id().set((short) 0);
                grabbedItem.count().set((byte) 0);

                grabbedItem.image().getGameObject().setActive(false);
                grabbedItem.text().getGameObject().setActive(false);
            }
            else if (grabbedItem.image().getGameObject().isActive() && instance.lowerSlot != grabbedItem.id().get())
            {
                Inventory.SlotData oldSlotData = new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount);

                instance.lowerSlot = grabbedItem.id().get();
                instance.lowerSlotCount = grabbedItem.count().get();

                Inventory.buildSlot("lower", new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount), instance.getGameObject());

                grabbedItem.id().set(oldSlotData.id());
                grabbedItem.count().set(oldSlotData.count());

                InventorySystem.getInstance().buildGrabbedItem();
            }
        }
    }

    public static void onRightMousePressed(@NotNull ButtonUIElement element)
    {
        if (element.getGameObject().getName().equals("ui.slot_result"))
            return;

        boolean isUpperSlot = element.getGameObject().getName().equals("ui.slot_upper");

        FurnaceSystem instance = FurnaceSystem.getInstance();

        InventorySystem.GrabbedItem grabbedItem = InventorySystem.getInstance().getGrabbedItem();

        if (isUpperSlot)
        {
            if (grabbedItem.image().getGameObject().isActive() && grabbedItem.id().get() != 0)
            {
                instance.upperSlot = grabbedItem.id().get();
                instance.upperSlotCount = (byte) (instance.upperSlotCount + 1);

                Inventory.buildSlot("upper", new Inventory.SlotData(instance.upperSlot, instance.upperSlotCount), instance.getGameObject());

                CreativeCraftingSystem.checkGrabbedItem(grabbedItem);
            }
        }
        else
        {
            if (grabbedItem.image().getGameObject().isActive() && grabbedItem.id().get() != 0)
            {
                instance.lowerSlot = grabbedItem.id().get();
                instance.lowerSlotCount = (byte) (instance.lowerSlotCount + 1);

                Inventory.buildSlot("lower", new Inventory.SlotData(instance.lowerSlot, instance.lowerSlotCount), instance.getGameObject());

                CreativeCraftingSystem.checkGrabbedItem(grabbedItem);
            }
        }
    }

    public static void onHoverBegin(@NotNull ButtonUIElement element)
    {

    }

    public static void onHoverEnd(@NotNull ButtonUIElement element)
    {

    }

    public static @NotNull FurnaceSystem getInstance()
    {
        return instance;
    }

    public static @NotNull FurnaceSystem create()
    {
        return new FurnaceSystem();
    }
}