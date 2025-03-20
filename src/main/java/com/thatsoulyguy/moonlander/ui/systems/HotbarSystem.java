package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

@CustomConstructor("create")
public class HotbarSystem extends Component
{
    private Inventory inventory;

    private static HotbarSystem instance = null;

    private HotbarSystem() { }

    @Override
    public void initialize()
    {
        instance = this;

        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        for (int s = 0; s < 9; s++)
            panel.getNotNull("ui.slot_0_" + s + "_text", TextUIElement.class).getGameObject().setActive(false);
    }

    public void generate()
    {
        inventory.registerOnSlotChangedCallback((position, slot) ->
        {
            UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

            if (position.x != 0)
                return;

            if (slot.count() <= 0)
            {
                panel.getNotNull("ui.slot_0_" + position.y + "_text", TextUIElement.class).getGameObject().setActive(false);
                panel.getNotNull("ui.slot_0_" + position.y, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
            }
            else
            {
                if (slot.count() > 1)
                {
                    panel.getNotNull("ui.slot_0_" + position.y + "_text", TextUIElement.class).getGameObject().setActive(true);

                    panel.getNotNull("ui.slot_0_" + position.y + "_text", TextUIElement.class).setText(String.valueOf(slot.count()));
                    panel.getNotNull("ui.slot_0_" + position.y + "_text", TextUIElement.class).build();
                }
                else
                    panel.getNotNull("ui.slot_0_" + position.y + "_text", TextUIElement.class).getGameObject().setActive(false);

                panel.getNotNull("ui.slot_0_" + position.y, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureAtlasManager.get("items")).createSubTexture(Objects.requireNonNull(ItemRegistry.get(slot.id())).getTexture(), false));
            }
        });
    }

    @Override
    public void update()
    {
        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        if (inventory.currentlySelectedSlotIndex > 8)
            inventory.currentlySelectedSlotIndex = 0;

        if (inventory.currentlySelectedSlotIndex < 0)
            inventory.currentlySelectedSlotIndex = 8;

        panel.getNotNull("ui.hotbar_selector", ImageUIElement.class).getGameObject().getTransform().setLocalPosition(new Vector3f(((inventory.currentlySelectedSlotIndex * 40.0f) - 160.0f), -21.0f, 0.0f));
    }

    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    public void setInventory(@NotNull Inventory inventory)
    {
        this.inventory = inventory;
    }

    public static @NotNull HotbarSystem getInstance()
    {
        return instance;
    }

    public static @NotNull HotbarSystem create()
    {
        return new HotbarSystem();
    }
}