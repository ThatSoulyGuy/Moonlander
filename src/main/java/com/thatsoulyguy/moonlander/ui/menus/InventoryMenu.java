package com.thatsoulyguy.moonlander.ui.menus;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipe;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipeRegistry;
import com.thatsoulyguy.moonlander.input.InputManager;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.ui.Menu;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.world.TextureAtlas;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.concurrent.locks.ReentrantLock;

public class InventoryMenu extends Menu
{
    private static final @NotNull Texture TEXTURE_TRANSPARENCY = Objects.requireNonNull(TextureManager.get("ui.transparency"));
    private static final @NotNull Texture TEXTURE_SLOT_DARKEN = Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken"));

    public int currentSlotSelected = 0;
    public int health = 20;
    public int oxygen = 100;

    private @EffectivelyNotNull Inventory inventory;

    private final @NotNull UIElement[] hotbarElements = new UIElement[9];
    private final @NotNull TextUIElement[] hotbarElementTexts = new TextUIElement[9];

    private final @NotNull UIElement[][] slotElements = new UIElement[4][9];
    private final @NotNull TextUIElement[][] slotElementTexts = new TextUIElement[4][9];

    private final @NotNull Short[][] craftingSlots = new Short[2][2];
    private final @NotNull Byte[][] craftingSlotCounts = new Byte[2][2];
    private final @NotNull UIElement[][] craftingSlotElements = new UIElement[2][2];
    private final @NotNull TextUIElement[][] craftingSlotTexts = new TextUIElement[2][2];

    private short craftingResultSlot = 0;
    private byte craftingResultSlotCount = 0;

    private @EffectivelyNotNull UIElement craftingResultElement;
    private @EffectivelyNotNull TextUIElement craftingResultText;

    private @EffectivelyNotNull UIPanel hud;
    private @EffectivelyNotNull UIPanel survivalMenu;
    private @EffectivelyNotNull UIPanel creativeMenu;
    private @EffectivelyNotNull UIElement hotbarSelector;

    private short grabbedItemId = 0;
    private byte grabbedItemCount = 0;

    private @EffectivelyNotNull UIElement grabbedItemElement;
    private @EffectivelyNotNull TextUIElement grabbedItemText;

    private @EffectivelyNotNull UIElement oxygenDialPointer;
    private final @NotNull UIElement[] heartsElements = new UIElement[10];

    private @EffectivelyNotNull TextUIElement selectedItemHoveringText;

    private @EffectivelyNotNull TextUIElement floatingTitleText;

    private @EffectivelyNotNull UIElement usageElement;

    private final ReentrantLock menuLock = new ReentrantLock(true);

    @Override
    public void initialize()
    {
        menuLock.lock();

        try
        {
            initializeCraftingData();
            createHudPanel();

            hud.addElement(createImageElement("crosshair", new Vector2f(0.0f), scaleVector(16.0f, 16.0f), Objects.requireNonNull(TextureManager.get("ui.crosshair")), true, UIElement.Alignment.CENTER, new Vector2f(0.0f, 0.0f)));

            floatingTitleText = (TextUIElement) hud.addElement(createTextElement(
                    "hovering_title_text",
                    new Vector2f(0.0f, 0.0f),
                    scaleVector(100.0f, 50.0f),
                    "",
                    scaleFont(18),
                    new Vector3f(1.0f),
                    true,
                    false,
                    UIElement.Alignment.CENTER,
                    new Vector2f(0.0f, -50.0f)
            ));

            floatingTitleText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            usageElement = hud.addElement(createImageElement("usage", new Vector2f(0.0f), scaleVector(32.0f, 32.0f), Objects.requireNonNull(TextureManager.get("ui.usage.fist")), true, UIElement.Alignment.TOP, new Vector2f(0.0f, 0.0f)));

            createHotbarUI();
            createOxygenAndHeartsUI();
            createSelectedItemHoveringText();

            createSurvivalMenu();
            createCreativeMenu();

            survivalMenu.setActive(false);
            creativeMenu.setActive(false);

            floatingTitleText.setActive(true);
        }
        finally
        {
            menuLock.unlock();
        }
    }

    @Override
    public void update()
    {
        menuLock.lock();

        try
        {
            if (currentSlotSelected > 8)
                currentSlotSelected = 0;

            if (currentSlotSelected < 0)
                currentSlotSelected = 8;

            if (grabbedItemText.isActive())
                grabbedItemText.setPosition(InputManager.getMousePosition().add(new Vector2f(7.5f, 7.5f)));

            if (grabbedItemElement.isActive())
                grabbedItemElement.setPosition(InputManager.getMousePosition().sub(new Vector2f(16.0f, 16.0f)));

            String itemName = Objects.requireNonNull(ItemRegistry.get(inventory.slots[0][currentSlotSelected])).getDisplayName();

            if (!selectedItemHoveringText.getText().equals(itemName))
            {
                selectedItemHoveringText.setText(itemName);
                selectedItemHoveringText.build();
            }

            selectedItemHoveringText.setOffset(new Vector2f(hotbarSelector.getOffset().x, -scale(35.0f) * 4.0f));

            updateHearts();

            oxygenDialPointer.setRotation(-90.0f + (oxygen * 1.8f));

            updateCraftingResult();

            hotbarSelector.setOffset(new Vector2f((currentSlotSelected * scale(40.0f)) - scale(160.0f), scale(-7.0f)));
        }
        finally
        {
            menuLock.unlock();
        }
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_inventory";
    }

    public void setInventory(@NotNull Inventory inv)
    {
        this.inventory = inv;
    }

    public void setSurvivalMenuActive(boolean active)
    {
        survivalMenu.setActive(active);
    }

    public boolean isSurvivalMenuActive()
    {
        return survivalMenu.isActive();
    }

    public void setCreativeMenu(boolean active)
    {
        creativeMenu.setActive(active);
    }

    public boolean isCreativeMenuActive()
    {
        return creativeMenu.isActive();
    }

    public void setUsageType(@NotNull UsageType type)
    {
        switch (type)
        {
            case FIST -> usageElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.usage.fist")));
            case PICKAXE -> usageElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.usage.pickaxe")));
            case SWORD -> usageElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.usage.sword")));
        }
    }

    public void addItem(short item, byte count)
    {
        menuLock.lock();

        try
        {
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 9; y++)
                {
                    if (inventory.slots[x][y] == item && inventory.slotCounts[x][y] <= 63)
                    {
                        inventory.slotCounts[x][y] = (byte) (inventory.slotCounts[x][y] + count);
                        build();

                        return;
                    }
                    else if (inventory.slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                    {
                        inventory.slots[x][y] = item;
                        inventory.slotCounts[x][y] = count;
                        build();

                        return;
                    }
                }
            }
        }
        finally
        {
            menuLock.unlock();
        }
    }

    public void setSlot(@NotNull Vector2i position, short item, byte count)
    {
        menuLock.lock();

        try
        {
            if (!isPositionValid(position))
                return;

            if (inventory.slots[position.x][position.y] == item && inventory.slotCounts[position.x][position.y] == count)
                return;

            inventory.slots[position.x][position.y] = item;
            inventory.slotCounts[position.x][position.y] = count;

            build();
        }
        finally
        {
            menuLock.unlock();
        }
    }

    public @Nullable SlotData getSlot(@NotNull Vector2i position)
    {
        if (!isPositionValid(position))
            return null;

        return new SlotData(inventory.slots[position.x][position.y], inventory.slotCounts[position.x][position.y]);
    }

    public void setFloatingTitleText(@NotNull String text)
    {
        floatingTitleText.setText(text);
        floatingTitleText.build();
    }

    public void build()
    {
        menuLock.lock();

        try
        {
            buildHotbarAndInventory();

            buildCraftingSlots();

            buildCraftingResult();
        }
        finally
        {
            menuLock.unlock();
        }
    }

    private void initializeCraftingData()
    {
        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
            {
                craftingSlots[x][y] = 0;
                craftingSlotCounts[x][y] = 0;
            }
        }
    }

    private void createHudPanel()
    {
        hud = UIPanel.create("hud");
    }

    private void createHotbarUI()
    {
        hud.addElement(
                createImageElement(
                        "hotbar",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(362.0f, 42.0f),
                        Objects.requireNonNull(TextureManager.get("ui.hotbar")),
                        true,
                        UIElement.Alignment.BOTTOM,
                        new Vector2f(0.0f, -scale(8.0f))
                )
        );

        hotbarSelector = hud.addElement(
                createImageElement(
                        "hotbar_selector",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(46.0f, 46.0f),
                        Objects.requireNonNull(TextureManager.get("ui.hotbar_selector")),
                        true,
                        UIElement.Alignment.BOTTOM,
                        new Vector2f(0.0f, 0.0f)
                )
        );

        for (int y = 0; y < 9; y++)
        {
            UIElement slotElement = hud.addElement(
                    createImageElement(
                            "slot_0_" + y,
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(28.0f, 28.0f),
                            TEXTURE_TRANSPARENCY,
                            true,
                            UIElement.Alignment.BOTTOM,
                            new Vector2f(y * scale(40.0f) - scale(160.0f), -scale(14.0f))
                    )
            );

            hotbarElements[y] = slotElement;
        }

        for (int y = 0; y < 9; y++)
        {
            TextUIElement textElement = (TextUIElement) hud.addElement(
                    createTextElement(
                            "slot_text_0_" + y,
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(18.0f, 18.0f),
                            "",
                            scaleFont(20),
                            new Vector3f(1.0f),
                            true,
                            true,
                            UIElement.Alignment.BOTTOM,
                            new Vector2f(y * scale(40.0f) - scale(144.5f), -scale(9.45f))
                    )
            );

            textElement.setActive(false);
            hotbarElementTexts[y] = textElement;
        }
    }

    private void createOxygenAndHeartsUI()
    {
        hud.addElement(
                createImageElement(
                        "oxygen_dial",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(35.0f, 19.0f).mul(1.5f),
                        Objects.requireNonNull(TextureManager.get("ui.menu.oxygen_dial")),
                        true,
                        UIElement.Alignment.BOTTOM,
                        new Vector2f(-262.0f + scale(20.0f), -scale(110.0f))
                )
        );

        hud.addElement(
                createTextElement(
                        "oxygen_dial_text",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(50, 50),
                        "O₂",
                        scaleFont(14),
                        new Vector3f(0.0f, 0.45f, 0.75f),
                        true,
                        true,
                        UIElement.Alignment.BOTTOM,
                        scaleVector(-141.0f, -93.0f)
                )
        );

        oxygenDialPointer = hud.addElement(
                createImageElement(
                        "oxygen_pointer",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(2.0f, 12.0f).mul(1.5f),
                        Objects.requireNonNull(TextureManager.get("ui.menu.oxygen_pointer")),
                        true,
                        UIElement.Alignment.BOTTOM,
                        new Vector2f(-262.0f + scale(20.0f), -scale(114.5f))
                )
        );
        oxygenDialPointer.setPivot(new Vector2f(0.5f, 0.9f));
        oxygenDialPointer.setRotation(-90.0f);

        hud.addElement(
                createImageElement(
                        "oxygen_dial_ball",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(35.0f, 19.0f).mul(1.5f),
                        Objects.requireNonNull(TextureManager.get("ui.menu.oxygen_dial_ball")),
                        true,
                        UIElement.Alignment.BOTTOM,
                        new Vector2f(-262.0f + scale(20.0f), -scale(110.0f))
                )
        );

        for (int h = 0; h < 10; h++)
        {
            hud.addElement(
                    createImageElement(
                            "empty_heart_" + h,
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(17.0f, 16.0f),
                            Objects.requireNonNull(TextureManager.get("ui.menu.empty_heart")),
                            true,
                            UIElement.Alignment.BOTTOM,
                            new Vector2f(((19.5f * scale(9.0f / 11.0f)) * h) - 262.0f, -scale(75.0f))
                    )
            );
        }

        for (int h = 0; h < 10; h++)
        {
            UIElement fullHeart = hud.addElement(
                    createImageElement(
                            "heart_" + h,
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(15.0f, 14.0f),
                            Objects.requireNonNull(TextureManager.get("ui.menu.full_heart")),
                            true,
                            UIElement.Alignment.BOTTOM,
                            new Vector2f(((19.5f * scale(9.0f / 11.0f)) * h) - 262.0f, -scale(77.0f))
                    )
            );

            heartsElements[h] = fullHeart;
        }
    }

    private void createSelectedItemHoveringText()
    {
        selectedItemHoveringText = (TextUIElement) hud.addElement(
                createTextElement(
                        "selected_item_hovering_text",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(400, 20),
                        "",
                        scaleFont(15),
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        true,
                        true,
                        UIElement.Alignment.BOTTOM,
                        new Vector2f(0.0f, 0.0f)
                )
        );
    }

    private void createSurvivalMenu()
    {
        survivalMenu = UIPanel.create("survival_menu");

        UIElement background = survivalMenu.addElement(
                createImageElement(
                        "background",
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(100.0f, 100.0f),
                        Objects.requireNonNull(TextureManager.get("ui.background")),
                        true,
                        null,
                        new Vector2f(0.0f, 0.0f)
                )
        );
        background.setStretch(List.of(
                UIElement.Stretch.LEFT,
                UIElement.Stretch.RIGHT,
                UIElement.Stretch.TOP,
                UIElement.Stretch.BOTTOM
        ));

        survivalMenu.addElement(
                createImageElement(
                        "inventory",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(352.0f * (9.0f/11.0f), 332.0f * (9.0f/11.0f)),
                        Objects.requireNonNull(TextureManager.get("ui.menu.survival_inventory")),
                        true,
                        null,
                        new Vector2f(0.0f, -scale(35.0f))
                )
        );

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                ButtonUIElement button = (ButtonUIElement) survivalMenu.addElement(
                        createButtonElement(
                                "survival_slot_" + x + "_" + y,
                                scaleVector(32.0f * (9.0f/11.0f), 32.0f * (9.0f/11.0f))
                        )
                );

                setupInventorySlotEvents(button, x, y);

                if (x == 0)
                {
                    button.setOffset(new Vector2f(
                            y * scale(36.0f * (9.0f/11.0f)) - scale(117.5f),
                            scale(75.0f)
                    ));
                }
                else
                {
                    button.setOffset(new Vector2f(
                            y * scale(36.0f * (9.0f/11.0f)) - scale(117.5f),
                            scale(68.0f) - (x * scale(36.0f * (9.0f/11.0f)))
                    ));
                }

                slotElements[x][y] = button;

                TextUIElement text = (TextUIElement) survivalMenu.addElement(
                        createTextElement(
                                "survival_slot_text_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                scaleVector(18.0f, 18.0f),
                                "",
                                scaleFont(18),
                                new Vector3f(1.0f),
                                true,
                                false,
                                null,
                                new Vector2f(0.0f, 0.0f)
                        )
                );

                text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

                if (x == 0)
                {
                    text.setOffset(new Vector2f(
                            y * scale(29.5f) - scale(111.0f),
                            scale(71.0f) + scale(12.45f)
                    ));
                }
                else
                {
                    text.setOffset(new Vector2f(
                            y * scale(29.5f) - scale(111.0f),
                            (scale(65.0f) + scale(12.45f)) - (x * scale(36.0f * (9.0f/11.0f)))
                    ));
                }

                slotElementTexts[x][y] = text;
            }
        }

        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
            {
                ButtonUIElement button = (ButtonUIElement) survivalMenu.addElement(
                        createButtonElement(
                                "crafting_slot_" + x + "_" + y,
                                scaleVector(32.0f * (9.0f/11.0f), 32.0f * (9.0f/11.0f))
                        )
                );

                setupCraftingSlotEvents(button, x, y);

                button.setOffset(new Vector2f(
                        (y * scale(36.0f * (9.0f/11.0f))) + scale(13.0f),
                        (x * scale(36.0f * (9.0f/11.0f))) - scale(115.0f)
                ));

                craftingSlotElements[x][y] = button;

                TextUIElement text = (TextUIElement) survivalMenu.addElement(
                        createTextElement(
                                "crafting_slot_text_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                scaleVector(18.0f, 18.0f),
                                "",
                                scaleFont(18),
                                new Vector3f(1.0f),
                                true,
                                false,
                                null,
                                new Vector2f(0.0f, 0.0f)
                        )
                );

                text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

                text.setOffset(new Vector2f(
                        y * scale(29.5f) + scale(19.0f),
                        (x * scale(36.0f * (9.0f/11.0f))) - (scale(95.0f) + scale(12.45f))
                ));

                craftingSlotTexts[x][y] = text;
            }
        }

        ButtonUIElement craftingResultButton = (ButtonUIElement) survivalMenu.addElement(
                createButtonElement(
                        "crafting_result_slot",
                        scaleVector(32.0f * (9.0f/11.0f), 32.0f * (9.0f/11.0f))
                )
        );

        setupCraftingResultSlotEvents(craftingResultButton);
        craftingResultButton.setOffset(new Vector2f(scale(104.5f), scale(-99.0f)));
        craftingResultElement = craftingResultButton;

        TextUIElement resultText = (TextUIElement) survivalMenu.addElement(
                createTextElement(
                        "crafting_result_slot_text",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(18.0f, 18.0f),
                        "",
                        scaleFont(18),
                        new Vector3f(1.0f),
                        true,
                        false,
                        null,
                        scaleVector(112.0f, -87.0f)
                )
        );

        resultText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
        craftingResultText = resultText;

        grabbedItemElement = survivalMenu.addElement(
                createImageElement(
                        "selected_item",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(32.0f * (9.0f/11.0f), 32.0f * (9.0f/11.0f)),
                        TEXTURE_TRANSPARENCY,
                        true,
                        null,
                        new Vector2f(0.0f, 0.0f)
                )
        );
        grabbedItemElement.setActive(false);
        grabbedItemElement.setAlignAndStretch(false);

        grabbedItemText = (TextUIElement) survivalMenu.addElement(
                createTextElement(
                        "selected_item_text",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(16.0f, 15.5f),
                        "",
                        scaleFont(20),
                        new Vector3f(1.0f),
                        true,
                        false,
                        null,
                        new Vector2f(0.0f, 0.0f)
                )
        );

        grabbedItemText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
        grabbedItemText.setAlignAndStretch(false);
        grabbedItemText.setActive(false);
    }

    private void createCreativeMenu()
    {
        creativeMenu = UIPanel.create("creative_menu");
        creativeMenu.setActive(false);
    }

    private void buildHotbarAndInventory()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (inventory.slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    if (x == 0)
                    {
                        hotbarElements[y].setTexture(TEXTURE_TRANSPARENCY);
                        hotbarElementTexts[y].setActive(false);
                    }

                    slotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();

                    continue;
                }

                Item item = ItemRegistry.get(inventory.slots[x][y]);

                if (item == null)
                {
                    System.err.println("Invalid item detected in menu!");
                    continue;
                }

                if (inventory.slotCounts[x][y] <= 0)
                {
                    inventory.slots[x][y] = ItemRegistry.ITEM_AIR.getId();
                    inventory.slotCounts[x][y] = 0;

                    if (x == 0)
                    {
                        hotbarElements[y].setTexture(TEXTURE_TRANSPARENCY);
                        hotbarElementTexts[y].setActive(false);
                    }

                    slotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();

                    continue;
                }

                if (inventory.slotCounts[x][y] == 1)
                {
                    if (x == 0)
                        hotbarElementTexts[y].setActive(false);

                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();
                }

                TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));

                slotElements[x][y].setTexture(atlas);

                Vector2f[] uvs = atlas.getSubTextureCoordinates(item.getTexture(), 90);

                if (uvs == null)
                {
                    System.err.println("Invalid UVs detected in menu!");
                    continue;
                }

                slotElements[x][y].setUVs(uvs);

                if (x == 0)
                {
                    hotbarElements[y].setTexture(atlas);
                    hotbarElements[y].setUVs(uvs);
                }

                if (inventory.slotCounts[x][y] > 1)
                {
                    String countStr = String.valueOf(inventory.slotCounts[x][y]);

                    if (x == 0)
                    {
                        hotbarElementTexts[y].setActive(true);
                        hotbarElementTexts[y].setText(countStr);
                        hotbarElementTexts[y].build();
                    }

                    slotElementTexts[x][y].setText(countStr);
                    slotElementTexts[x][y].build();
                }
            }
        }
    }

    private void buildCraftingSlots()
    {
        for (int x = 0; x < 2; x++)
        {
            for (int y = 0; y < 2; y++)
            {
                if (craftingSlots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    craftingSlotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    craftingSlotTexts[x][y].setText("");
                    craftingSlotTexts[x][y].build();

                    continue;
                }

                Item item = ItemRegistry.get(craftingSlots[x][y]);

                if (item == null)
                {
                    System.err.println("Invalid item in crafting!");
                    continue;
                }

                if (craftingSlotCounts[x][y] <= 0)
                {
                    craftingSlots[x][y] = ItemRegistry.ITEM_AIR.getId();
                    craftingSlotCounts[x][y] = 0;
                    craftingSlotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    craftingSlotTexts[x][y].setText("");
                    craftingSlotTexts[x][y].build();

                    continue;
                }

                if (craftingSlotCounts[x][y] == 1)
                {
                    craftingSlotTexts[x][y].setText("");
                    craftingSlotTexts[x][y].build();
                }

                TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));
                craftingSlotElements[x][y].setTexture(atlas);

                Vector2f[] uvs = atlas.getSubTextureCoordinates(item.getTexture(), 90);
                if (uvs == null) {
                    System.err.println("Invalid UVs in crafting!");
                    continue;
                }
                craftingSlotElements[x][y].setUVs(uvs);

                if (craftingSlotCounts[x][y] > 1)
                {
                    String countStr = String.valueOf(craftingSlotCounts[x][y]);
                    craftingSlotTexts[x][y].setText(countStr);
                    craftingSlotTexts[x][y].build();
                }
            }
        }
    }

    private void buildCraftingResult()
    {
        if (craftingResultSlot == ItemRegistry.ITEM_AIR.getId())
            return;

        Item resultItem = ItemRegistry.get(craftingResultSlot);

        if (resultItem == null)
        {
            System.err.println("Invalid item in crafting result!");
            return;
        }

        if (craftingResultSlotCount <= 0)
        {
            craftingResultSlot = ItemRegistry.ITEM_AIR.getId();
            craftingResultSlotCount = 0;
            craftingResultElement.setTexture(TEXTURE_TRANSPARENCY);
            craftingResultText.setText("");
            craftingResultText.build();

            return;
        }

        if (craftingResultSlotCount == 1)
        {
            craftingResultText.setText("");
            craftingResultText.build();
        }

        TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));
        craftingResultElement.setTexture(atlas);
        Vector2f[] uvs = atlas.getSubTextureCoordinates(resultItem.getTexture(), 90);

        if (uvs == null)
        {
            System.err.println("Invalid UVs for crafting result!");
            return;
        }

        craftingResultElement.setUVs(uvs);

        if (craftingResultSlotCount > 1)
        {
            craftingResultText.setText(String.valueOf(craftingResultSlotCount));
            craftingResultText.build();
        }
    }

    private void updateHearts()
    {
        if (health >= 20)
        {
            for (int h = 0; h < 10; h++)
            {
                heartsElements[h].setActive(true);
                heartsElements[h].setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.full_heart")));
            }

            return;
        }

        for (int h = 10; h > 0; h--)
        {
            int heartIndex = h - 1;
            int heartValue = Math.max(0, Math.min(2, health - (heartIndex * 2)));

            switch (heartValue)
            {
                case 2 ->
                {
                    heartsElements[heartIndex].setActive(true);
                    heartsElements[heartIndex].setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.full_heart")));
                }

                case 1 ->
                {
                    heartsElements[heartIndex].setActive(true);
                    heartsElements[heartIndex].setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.half_heart")));
                }

                default -> heartsElements[heartIndex].setActive(false);
            }
        }
    }

    private void updateCraftingResult()
    {
        List<CraftingRecipe> recipes = CraftingRecipeRegistry.getAll();
        boolean wasMatch = false;

        for (CraftingRecipe recipe : recipes)
        {
            boolean match = CraftingRecipe.matchesRecipe(
                    recipe,
                    Arrays.stream(craftingSlots)
                            .map(subArray ->
                            {
                                short[] arr = new short[subArray.length];

                                for (int i = 0; i < subArray.length; i++)
                                    arr[i] = subArray[i];

                                return arr;
                            })
                            .toArray(short[][]::new)
            );
            if (match)
            {
                short oldSlot = craftingResultSlot;
                byte oldCount = craftingResultSlotCount;

                craftingResultSlot = recipe.getResult().item().getId();
                craftingResultSlotCount = recipe.getResult().count();

                if (oldSlot != craftingResultSlot || craftingResultSlotCount != oldCount)
                    build();

                wasMatch = true;

                break;
            }
        }

        if (!wasMatch)
        {
            byte oldCount = craftingResultSlotCount;

            craftingResultSlotCount = 0;

            if (oldCount != 0)
                build();
        }
    }

    private void setupInventorySlotEvents(@NotNull ButtonUIElement button, int x, int y)
    {
        button.addOnLeftClickedEvent(() -> handleSlotLeftClick(x, y, inventory.slots, inventory.slotCounts, slotElements, button, false));
        button.addOnRightClickedEvent(() -> handleSlotRightClick(x, y, inventory.slots, inventory.slotCounts, slotElements, button, false));
        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void setupCraftingSlotEvents(@NotNull ButtonUIElement button, int x, int y)
    {
        button.addOnLeftClickedEvent(() -> handleSlotLeftClick(x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, true));
        button.addOnRightClickedEvent(() -> handleSlotRightClick(x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, true));
        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void setupCraftingResultSlotEvents(@NotNull ButtonUIElement button)
    {
        button.addOnLeftClickedEvent(() ->
        {
            if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
                return;

            if (craftingResultSlot != ItemRegistry.ITEM_AIR.getId())
            {
                grabbedItemId = craftingResultSlot;
                grabbedItemElement.setTexture((TextureAtlas) craftingResultElement.getTexture());
                grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(craftingResultSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                grabbedItemCount += craftingResultSlotCount;
                updateGrabbedItemText();

                craftingSlotCounts[0][0]--;
                craftingSlotCounts[1][0]--;
                craftingSlotCounts[0][1]--;
                craftingSlotCounts[1][1]--;

                craftingResultSlotCount = 0;
                build();
            }
        });

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void handleSlotLeftClick(int x, int y, @NotNull Short[][] itemArr, @NotNull Byte[][] countArr, @NotNull UIElement[][] uiArr, @NotNull UIElement button, boolean crafting)
    {
        if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
            return;

        if (itemArr[x][y] != ItemRegistry.ITEM_AIR.getId() && grabbedItemId == ItemRegistry.ITEM_AIR.getId())
        {
            grabbedItemId = itemArr[x][y];
            grabbedItemElement.setTexture((TextureAtlas) uiArr[x][y].getTexture());
            grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(itemArr[x][y])).getTexture(), 90));
            grabbedItemElement.setPosition(InputManager.getMousePosition());
            grabbedItemElement.setActive(true);

            grabbedItemCount = countArr[x][y];
            updateGrabbedItemText();

            countArr[x][y] = 0;
            build();

            return;
        }

        if (itemArr[x][y] == ItemRegistry.ITEM_AIR.getId() && grabbedItemElement.isActive())
        {
            grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
            grabbedItemElement.setActive(false);

            countArr[x][y] = grabbedItemCount;
            itemArr[x][y] = grabbedItemId;

            grabbedItemId = ItemRegistry.ITEM_AIR.getId();
            grabbedItemCount = 0;

            updateGrabbedItemText();
            build();

            return;
        }

        if (itemArr[x][y] != grabbedItemId && grabbedItemElement.isActive())
        {
            short oldGrabbedId = grabbedItemId;
            byte oldGrabbedCount = grabbedItemCount;

            grabbedItemId = itemArr[x][y];
            grabbedItemCount = countArr[x][y];

            grabbedItemElement.setTexture((TextureAtlas) uiArr[x][y].getTexture());
            grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(itemArr[x][y])).getTexture(), 90));
            grabbedItemElement.setPosition(InputManager.getMousePosition());
            grabbedItemElement.setActive(true);

            updateGrabbedItemText();

            itemArr[x][y]  = oldGrabbedId;
            countArr[x][y] = oldGrabbedCount;

            build();
            return;
        }

        if (itemArr[x][y] == grabbedItemId)
        {
            grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
            grabbedItemElement.setActive(false);

            countArr[x][y] = (byte) (countArr[x][y] + grabbedItemCount);

            grabbedItemId = ItemRegistry.ITEM_AIR.getId();
            grabbedItemCount = 0;

            updateGrabbedItemText();
            build();
        }
    }

    private void handleSlotRightClick(int x, int y, @NotNull Short[][] itemArr, @NotNull Byte[][] countArr, @NotNull UIElement[][] uiArr, @NotNull UIElement button, boolean crafting)
    {
        if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
            return;

        if (grabbedItemCount > 0 && (itemArr[x][y] == ItemRegistry.ITEM_AIR.getId() || itemArr[x][y] == grabbedItemId))
        {
            itemArr[x][y] = grabbedItemId;

            if (countArr[x][y] < 0)
                countArr[x][y] = 1;
            else
                countArr[x][y] = (byte) (countArr[x][y] + 1);

            grabbedItemCount--;
            updateGrabbedItemText();

            if (grabbedItemCount <= 0)
            {
                grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                grabbedItemElement.setActive(false);
            }

            build();
        }
    }

    private void handleSlotHoverBegin(@NotNull UIElement button)
    {
        if (button.getTexture() == TEXTURE_TRANSPARENCY)
            button.setTexture(TEXTURE_SLOT_DARKEN);
        else
            button.setColor(new Vector3f(0.82f));
    }

    private void handleSlotHoverEnd(@NotNull UIElement button)
    {
        if (button.getTexture() == TEXTURE_SLOT_DARKEN)
            button.setTexture(TEXTURE_TRANSPARENCY);
        else
            button.setColor(new Vector3f(1.0f));
    }

    private void updateGrabbedItemText()
    {
        if (grabbedItemCount > 1)
        {
            grabbedItemText.setText(String.valueOf(grabbedItemCount));
            grabbedItemText.build();
            grabbedItemText.setActive(true);
        }
        else
        {
            grabbedItemText.setText("");
            grabbedItemText.build();
            grabbedItemText.setActive(false);
        }
    }

    private boolean isPositionValid(@NotNull Vector2i position)
    {
        return position.x >= 0 && position.x < inventory.slots.length && position.y >= 0 && position.y < inventory.slots[0].length;
    }

    private UIElement createImageElement(@NotNull String name, @NotNull Vector2f position, @NotNull Vector2f size, @NotNull Texture texture, boolean transparent, UIElement.Alignment alignment, @NotNull Vector2f offset)
    {
        ImageUIElement element = UIElement.create(ImageUIElement.class, name, position, size);

        element.setTexture(texture);
        element.setTransparent(transparent);

        if (alignment != null)
            element.setAlignment(alignment);

        element.setOffset(offset);

        return element;
    }

    private TextUIElement createTextElement(@NotNull String name, @NotNull Vector2f position, @NotNull Vector2f size, @NotNull String text, float fontSize, @NotNull Vector3f color, boolean isTransparent, boolean isActive, UIElement.Alignment alignment, @NotNull Vector2f offset)
    {
        TextUIElement element = UIElement.create(TextUIElement.class, name, position, size);

        element.setText(text);
        element.setTransparent(isTransparent);
        element.setActive(isActive);
        element.setColor(color);
        element.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
        element.setFontSize((int) fontSize);
        element.build();
        element.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

        if (alignment != null)
            element.setAlignment(alignment);

        element.setOffset(offset);

        return element;
    }

    private ButtonUIElement createButtonElement(@NotNull String name, @NotNull Vector2f size)
    {
        ButtonUIElement button = UIElement.create(
                ButtonUIElement.class,
                name,
                new Vector2f(0.0f, 0.0f),
                size
        );

        button.setTexture(InventoryMenu.TEXTURE_TRANSPARENCY);
        button.setTransparent(true);

        return button;
    }

    private float scale(float value)
    {
        return value * Settings.UI_SCALE.getValue();
    }

    private @NotNull Vector2f scaleVector(float x, float y)
    {
        return new Vector2f(x, y).mul(Settings.UI_SCALE.getValue());
    }

    private float scaleFont(float baseSize)
    {
        return baseSize * (Settings.UI_SCALE.getValue() - 0.5f);
    }

    public record SlotData(short id, byte count) implements Serializable { }

    public enum UsageType implements Serializable
    {
        FIST,
        PICKAXE,
        SWORD
    }
}
