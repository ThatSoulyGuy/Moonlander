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
import com.thatsoulyguy.moonlander.ui.MenuManager;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.ManagerLinkedClass;
import com.thatsoulyguy.moonlander.world.TextureAtlas;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CraftingTableMenu extends Menu
{
    private static final @NotNull Texture TEXTURE_TRANSPARENCY = Objects.requireNonNull(TextureManager.get("ui.transparency"));
    private static final @NotNull Texture TEXTURE_SLOT_DARKEN = Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken"));

    private @EffectivelyNotNull Inventory inventory;
    private @EffectivelyNotNull InventoryMenu inventoryMenu;

    private final @NotNull UIElement[][] slotElements = new UIElement[4][9];
    private final @NotNull TextUIElement[][] slotElementTexts = new TextUIElement[4][9];

    private final @NotNull Short[][] craftingSlots = new Short[3][3];
    private final @NotNull Byte[][] craftingSlotCounts = new Byte[3][3];
    private final @NotNull UIElement[][] craftingSlotElements = new UIElement[3][3];
    private final @NotNull TextUIElement[][] craftingSlotTexts = new TextUIElement[3][3];

    private short grabbedItemId = 0;
    private byte grabbedItemCount = 0;
    private @EffectivelyNotNull UIElement grabbedItemElement;
    private @EffectivelyNotNull TextUIElement grabbedItemText;

    private @EffectivelyNotNull UIElement craftingResultElement;
    private @EffectivelyNotNull TextUIElement craftingResultText;
    private short craftingResultSlot = 0;
    private byte craftingResultSlotCount = 0;

    private @EffectivelyNotNull UIPanel menu;

    @Override
    public void initialize()
    {
        menu = UIPanel.create("crafting_table_menu");

        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                craftingSlots[x][y] = ItemRegistry.ITEM_AIR.getId();
                craftingSlotCounts[x][y] = 0;
            }
        }

        UIElement background = menu.addElement(
                UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f, 0.0f), new Vector2f(100.0f, 100.0f))
        );

        background.setTransparent(true);
        background.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));
        background.setStretch(List.of(
                UIElement.Stretch.LEFT,
                UIElement.Stretch.RIGHT,
                UIElement.Stretch.TOP,
                UIElement.Stretch.BOTTOM
        ));

        UIElement inventoryElement = menu.addElement(
                UIElement.create(ImageUIElement.class, "inventory", new Vector2f(0.0f, 0.0f),
                        scaleVector(352.0f * (9.0f / 11.0f), 332.0f * (9.0f / 11.0f))
                )
        );

        inventoryElement.setTransparent(true);
        inventoryElement.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.crafting_table")));
        inventoryElement.setOffset(new Vector2f(0.0f, scale(-35.0f)));

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                ButtonUIElement button = (ButtonUIElement) menu.addElement(
                        createButtonElement(
                                "slot_" + x + "_" + y,
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

                TextUIElement text = (TextUIElement) menu.addElement(
                        createTextElement(
                                "slot_text_" + x + "_" + y,
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

        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
            {
                ButtonUIElement button = (ButtonUIElement) menu.addElement(
                        UIElement.create(
                                ButtonUIElement.class,
                                "crafting_slot_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                scaleVector(32.0f * (9.0f / 11.0f), 32.0f * (9.0f / 11.0f))
                        )
                );
                button.setTransparent(true);
                button.setTexture(TEXTURE_TRANSPARENCY);

                setupCraftingSlotEvents(button, x, y);

                button.setOffset(new Vector2f(
                        x * scale(36.0f * (9.0f / 11.0f)) - scale(82.0f),
                        y * scale(36.0f * (9.0f / 11.0f)) - scale(130.0f)
                ));

                craftingSlotElements[x][y] = button;

                TextUIElement text = (TextUIElement) menu.addElement(
                        UIElement.create(
                                TextUIElement.class,
                                "crafting_slot_text_" + x + "_" + y,
                                new Vector2f(0.0f, 0.0f),
                                scaleVector(18.0f, 18.0f)
                        )
                );

                text.setTransparent(true);
                text.setActive(false);
                text.setText("");
                text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
                text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
                text.setFontSize(18);
                text.build();
                text.setOffset(new Vector2f(
                        x * scale(36.0f * (9.0f / 11.0f)) - scale(75.0f),
                        y * scale(36.0f * (9.0f / 11.0f)) - scale(121.0f)
                ));

                craftingSlotTexts[x][y] = text;
            }
        }

        ButtonUIElement craftingResultButton = (ButtonUIElement) menu.addElement(
                UIElement.create(
                        ButtonUIElement.class,
                        "crafting_result_slot",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(32.0f * (9.0f / 11.0f), 32.0f * (9.0f / 11.0f))
                )
        );

        craftingResultButton.setTransparent(true);
        craftingResultButton.setTexture(TEXTURE_TRANSPARENCY);

        setupCraftingResultSlotEvents(craftingResultButton);
        craftingResultButton.setOffset(new Vector2f(scale(71.5f), scale(-100.5f)));

        craftingResultElement = craftingResultButton;

        TextUIElement resultText = (TextUIElement) menu.addElement(
                UIElement.create(
                        TextUIElement.class,
                        "crafting_result_slot_text",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(18.0f, 18.0f)
                )
        );

        resultText.setTransparent(true);
        resultText.setActive(false);
        resultText.setText("");
        resultText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
        resultText.setFontPath(AssetPath.create("moonlander",  "font/Invasion2-Default.ttf"));
        resultText.setFontSize(18);
        resultText.build();

        resultText.setOffset(new Vector2f(scale(121.5f), scale(-121.0f)));

        craftingResultText = resultText;

        grabbedItemElement = menu.addElement(
                UIElement.create(
                        ImageUIElement.class,
                        "selected_item",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(32.0f * (9.0f / 11.0f), 32.0f * (9.0f / 11.0f))
                )
        );

        grabbedItemElement.setTransparent(true);
        grabbedItemElement.setActive(false);
        grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
        grabbedItemElement.setAlignAndStretch(false);

        grabbedItemText = (TextUIElement) menu.addElement(
                UIElement.create(
                        TextUIElement.class,
                        "selected_item_text",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(18.0f, 18.0f)
                )
        );

        grabbedItemText.setTransparent(true);
        grabbedItemText.setActive(false);
        grabbedItemText.setText("");
        grabbedItemText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
        grabbedItemText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
        grabbedItemText.setFontSize(18);
        grabbedItemText.build();
        grabbedItemText.setAlignAndStretch(false);
    }

    @Override
    public void update()
    {
        if (grabbedItemText.isActive())
            grabbedItemText.setPosition(InputManager.getMousePosition().add(new Vector2f(7.5f, 7.5f)));
        if (grabbedItemElement.isActive())
            grabbedItemElement.setPosition(InputManager.getMousePosition().sub(new Vector2f(16.0f, 16.0f)));

        List<CraftingRecipe> craftingRecipes = CraftingRecipeRegistry.getAll();
        boolean wasMatch = false;

        for (CraftingRecipe recipe : craftingRecipes)
        {
            short[][] primitiveCraftingSlots = new short[craftingSlots.length][craftingSlots[0].length];

            for (int x = 0; x < craftingSlots.length; x++)
            {
                for (int y = 0; y < craftingSlots[x].length; y++)
                    primitiveCraftingSlots[y][craftingSlots.length - 1 - x] = craftingSlots[x][y];
            }

            if (CraftingRecipe.matchesRecipe(recipe, primitiveCraftingSlots))
            {
                short oldCraftingResultSlot = craftingResultSlot;
                byte oldCraftingResultSlotCount = craftingResultSlotCount;

                craftingResultSlot = recipe.getResult().item().getId();
                craftingResultSlotCount = recipe.getResult().count();

                if (oldCraftingResultSlot != craftingResultSlot || craftingResultSlotCount != oldCraftingResultSlotCount)
                    build();

                wasMatch = true;
                break;
            }
        }
        if (!wasMatch)
        {
            byte oldCraftingResultSlotCount = craftingResultSlotCount;

            craftingResultSlotCount = 0;

            if (craftingResultSlotCount != oldCraftingResultSlotCount)
                build();
        }
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_crafting_table";
    }

    public void build()
    {
        inventoryMenu.build();

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (inventory.slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
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
                    slotElements[x][y].setTexture(TEXTURE_TRANSPARENCY);
                    slotElementTexts[x][y].setText("");
                    slotElementTexts[x][y].build();
                    continue;
                }

                if (inventory.slotCounts[x][y] == 1)
                {
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

                if (inventory.slotCounts[x][y] > 1)
                {
                    slotElementTexts[x][y].setText(String.valueOf(inventory.slotCounts[x][y]));
                    slotElementTexts[x][y].build();
                }
            }
        }

        for (int x = 0; x < 3; x++)
        {
            for (int y = 0; y < 3; y++)
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
                    System.err.println("Invalid item detected in menu!");
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
                if (uvs == null)
                {
                    System.err.println("Invalid UVs detected in menu!");
                    continue;
                }
                craftingSlotElements[x][y].setUVs(uvs);
                if (craftingSlotCounts[x][y] > 1)
                {
                    craftingSlotTexts[x][y].setText(String.valueOf(craftingSlotCounts[x][y]));
                    craftingSlotTexts[x][y].build();
                }
            }
        }

        if (craftingResultSlot == ItemRegistry.ITEM_AIR.getId())
            return;

        Item resultItem = ItemRegistry.get(craftingResultSlot);
        if (resultItem == null)
        {
            System.err.println("Invalid item detected in menu!");
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
            System.err.println("Invalid uvs detected in menu!");
            return;
        }
        craftingResultElement.setUVs(uvs);
        if (craftingResultSlotCount > 1)
        {
            craftingResultText.setText(String.valueOf(craftingResultSlotCount));
            craftingResultText.build();
        }
    }

    public void setInventory(@NotNull Inventory inventory)
    {
        this.inventory = inventory;
    }

    public void setInventoryMenu(@NotNull InventoryMenu inventoryMenu)
    {
        this.inventoryMenu = inventoryMenu;
    }

    public void setActive(boolean active)
    {
        menu.setActive(active);
    }

    public boolean isActive()
    {
        return menu.isActive();
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
        button.addOnLeftClickedEvent(() -> handleSlotLeftClick(x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, false));
        button.addOnRightClickedEvent(() -> handleSlotRightClick(x, y, craftingSlots, craftingSlotCounts, craftingSlotElements, button, false));
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
            grabbedItemElement.setUVs(TextureAtlasManager.get("items")
                    .getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(itemArr[x][y])).getTexture(), 90));
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
            grabbedItemCount = 0;
            updateGrabbedItemText();

            itemArr[x][y] = grabbedItemId;
            grabbedItemId = ItemRegistry.ITEM_AIR.getId();

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
            grabbedItemElement.setUVs(TextureAtlasManager.get("items")
                    .getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(itemArr[x][y])).getTexture(), 90));
            grabbedItemElement.setPosition(InputManager.getMousePosition());
            grabbedItemElement.setActive(true);

            updateGrabbedItemText();

            itemArr[x][y] = oldGrabbedId;
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
                grabbedItemElement.setUVs(TextureAtlasManager.get("items")
                        .getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(craftingResultSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                grabbedItemCount += craftingResultSlotCount;
                updateGrabbedItemText();

                for (int x = 0; x < 3; x++)
                {
                    for (int y = 0; y < 3; y++)
                        craftingSlotCounts[x][y]--;
                }

                craftingResultSlotCount = 0;
                build();
            }
        });

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
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

        button.setTexture(TEXTURE_TRANSPARENCY);
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
}