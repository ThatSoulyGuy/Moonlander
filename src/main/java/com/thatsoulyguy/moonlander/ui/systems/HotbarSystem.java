package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Objects;

@CustomConstructor("create")
public class HotbarSystem extends Component
{
    private Inventory inventory;

    private TextUIElement currentlySelectedItemName;

    private int lastSlotIndex;

    private static HotbarSystem instance = null;

    private HotbarSystem() { }

    @Override
    public void initialize()
    {
        instance = this;

        currentlySelectedItemName = UIElement.createGameObject("ui.currently_selected_item_name", TextUIElement.class, new Vector2f(0, 0), new Vector2f(500, 40), UIManager.getCanvas()).getComponentNotNull(TextUIElement.class);

        currentlySelectedItemName.setText("Some");
        currentlySelectedItemName.setFontSize(10);
        currentlySelectedItemName.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
        currentlySelectedItemName.setAlignment(TextUIElement.TextAlignment.MIDDLE_CENTER);

        currentlySelectedItemName.build();

        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        for (int s = 0; s < 9; s++)
            panel.getNotNull("ui.slot_0_" + s + "_text", TextUIElement.class).getGameObject().setActive(false);
    }

    public void generate()
    {
        inventory.registerOnSlotChangedCallback((position, slot) ->
        {
            if (position.x != 0)
                return;

            Inventory.buildSlotWithPosition(position, slot, getGameObject());
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

        if (inventory.currentlySelectedSlotIndex != lastSlotIndex)
        {
            Vector2i dimensions = Window.getDimensions();

            float baseY = dimensions.y;

            float offsetY = -180.0f;

            currentlySelectedItemName.getGameObject().getTransform().setLocalPosition(new Vector3f((float) dimensions.x / 2, baseY + offsetY, 0.0f));
            currentlySelectedItemName.setText(Objects.requireNonNull(ItemRegistry.get(inventory.getCurrentlySelectedSlot().id())).getDisplayName());
            currentlySelectedItemName.build();

            lastSlotIndex = inventory.currentlySelectedSlotIndex;
        }

        panel.getNotNull("ui.hotbar_selector", ImageUIElement.class).getGameObject().getTransform().setLocalPosition(new Vector3f(((inventory.currentlySelectedSlotIndex * 40.0f) - 160.0f), -21.0f, 0.0f));

        if (EntityPlayer.getLocalPlayer().getCurrentHealth() >= 20)
        {
            for (int h = 0; h < 10; h++)
                panel.getNotNull("ui.heart_" + h, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.full_heart")));
        }
        else
        {
            for (int h = 10; h > 0; h--)
            {
                int heartIndex = h - 1;
                int heartValue = Math.max(0, Math.min(2, EntityPlayer.getLocalPlayer().getCurrentHealth() - (heartIndex * 2)));

                switch (heartValue)
                {
                    case 2 -> panel.getNotNull("ui.heart_" + heartIndex, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.full_heart")));

                    case 1 -> panel.getNotNull("ui.heart_" + heartIndex, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.half_heart")));

                    default -> panel.getNotNull("ui.heart_" + heartIndex, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.empty_heart")));
                }
            }
        }

        if (EntityPlayer.getLocalPlayer().getOxygen() >= 20)
        {
            for (int h = 0; h < 10; h++)
                panel.getNotNull("ui.bubble_" + h, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.bubble")));
        }
        else
        {
            for (int h = 10; h > 0; h--)
            {
                int bubbleIndex = h - 1;
                int bubbleValue = Math.max(0, Math.min(2, EntityPlayer.getLocalPlayer().getOxygen() - (bubbleIndex * 2)));

                switch (bubbleValue)
                {
                    case 2 -> panel.getNotNull("ui.bubble_" + bubbleIndex, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.bubble")));

                    case 1 -> panel.getNotNull("ui.bubble_" + bubbleIndex, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.bubble_half")));

                    default -> panel.getNotNull("ui.bubble_" + bubbleIndex, ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
                }
            }
        }
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