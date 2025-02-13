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
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class CompositorMenu extends Menu
{
    private static final @NotNull Texture TEXTURE_TRANSPARENCY = Objects.requireNonNull(TextureManager.get("ui.transparency"));
    private static final @NotNull Texture TEXTURE_SLOT_DARKEN = Objects.requireNonNull(TextureManager.get("ui.menu.slot_darken"));

    private @EffectivelyNotNull Inventory inventory;
    private @EffectivelyNotNull InventoryMenu inventoryMenu;

    private final @NotNull UIElement[][] slotElements = new UIElement[4][9];
    private final @NotNull TextUIElement[][] slotElementTexts = new TextUIElement[4][9];

    private @EffectivelyNotNull UIElement upperMaterialElement;
    private @EffectivelyNotNull TextUIElement upperMaterialText;
    private short upperMaterialSlot = 0;
    private byte upperMaterialSlotCount = 0;

    private @EffectivelyNotNull UIElement lowerMaterialElement;
    private @EffectivelyNotNull TextUIElement lowerMaterialText;
    private short lowerMaterialSlot = 0;
    private byte lowerMaterialSlotCount = 0;

    private short grabbedItemId = 0;
    private byte grabbedItemCount = 0;

    private @EffectivelyNotNull UIElement grabbedItemElement;
    private @EffectivelyNotNull TextUIElement grabbedItemText;

    private @EffectivelyNotNull UIElement craftingResultElement;
    private @EffectivelyNotNull TextUIElement craftingResultText;
    private short craftingResultSlot = 0;
    private byte craftingResultSlotCount = 0;

    private UIPanel menu;

    @Override
    public void initialize()
    {
        menu = UIPanel.create("menu_compositor");

        UIElement background = menu.addElement(
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

        menu.addElement(
                createImageElement(
                        "inventory",
                        new Vector2f(0.0f, 0.0f),
                        scaleVector(352.0f * (9.0f/11.0f), 332.0f * (9.0f/11.0f)),
                        Objects.requireNonNull(TextureManager.get("ui.menu.compositor")),
                        true,
                        null,
                        new Vector2f(0.0f, -scale(35.0f))
                )
        );

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                ButtonUIElement button = (ButtonUIElement) menu.addElement(
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

                TextUIElement text = (TextUIElement) menu.addElement(
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

        {
            ButtonUIElement upperMaterialButton = (ButtonUIElement) menu.addElement(
                    UIElement.create(
                            ButtonUIElement.class,
                            "upper_material_slot",
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(32.0f * (9.0f / 11.0f), 32.0f * (9.0f / 11.0f))
                    )
            );

            upperMaterialButton.setTransparent(true);
            upperMaterialButton.setTexture(TEXTURE_TRANSPARENCY);

            setupUpperMaterialSlotEvents(upperMaterialButton);
            upperMaterialButton.setOffset(new Vector2f(scale(-22.0f), scale(-97.0f - (32.0f * (9.0f / 11.0f)))));

            upperMaterialElement = upperMaterialButton;

            TextUIElement upperMaterialText = (TextUIElement) menu.addElement(
                    UIElement.create(
                            TextUIElement.class,
                            "upper_material_slot_text",
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(18.0f, 18.0f)
                    )
            );

            upperMaterialText.setTransparent(true);
            upperMaterialText.setActive(false);
            upperMaterialText.setText("");
            upperMaterialText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
            upperMaterialText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            upperMaterialText.setFontSize(18);
            upperMaterialText.build();

            upperMaterialText.setOffset(new Vector2f(scale(-14.0f), scale(-88.0f - (32.0f * (9.0f / 11.0f)))));

            this.upperMaterialText = upperMaterialText;
        }

        {
            ButtonUIElement lowerMaterialButton = (ButtonUIElement) menu.addElement(
                    UIElement.create(
                            ButtonUIElement.class,
                            "lower_material_slot",
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(32.0f * (9.0f / 11.0f), 32.0f * (9.0f / 11.0f))
                    )

            );

            lowerMaterialButton.setTransparent(true);
            lowerMaterialButton.setTexture(TEXTURE_TRANSPARENCY);

            setupLowerMaterialSlotEvents(lowerMaterialButton);
            lowerMaterialButton.setOffset(new Vector2f(scale(-22.0f), scale(-78.5f)));

            lowerMaterialElement = lowerMaterialButton;

            TextUIElement lowerMaterialText = (TextUIElement) menu.addElement(
                    UIElement.create(
                            TextUIElement.class,
                            "lower_material_slot_text",
                            new Vector2f(0.0f, 0.0f),
                            scaleVector(18.0f, 18.0f)
                    )
            );

            lowerMaterialText.setTransparent(true);
            lowerMaterialText.setActive(false);
            lowerMaterialText.setText("");
            lowerMaterialText.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);
            lowerMaterialText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            lowerMaterialText.setFontSize(18);
            lowerMaterialText.build();

            lowerMaterialText.setOffset(new Vector2f(scale(-14.0f), scale(-67.5f)));

            this.lowerMaterialText = lowerMaterialText;
        }

        {
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
            resultText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            resultText.setFontSize(18);
            resultText.build();

            resultText.setOffset(new Vector2f(scale(121.5f), scale(-121.0f)));

            craftingResultText = resultText;
        }

        {
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
                    String countStr = String.valueOf(inventory.slotCounts[x][y]);
                    slotElementTexts[x][y].setText(countStr);
                    slotElementTexts[x][y].build();
                }
            }
        }

        {
            if (upperMaterialSlot == ItemRegistry.ITEM_AIR.getId())
            {
                upperMaterialElement.setTexture(TEXTURE_TRANSPARENCY);
                upperMaterialText.setText("");
                upperMaterialText.build();
            }
            else
            {
                Item resultItem = ItemRegistry.get(upperMaterialSlot);

                if (resultItem == null)
                    System.err.println("Invalid item detected in menu!");
                else if (upperMaterialSlotCount <= 0)
                {
                    upperMaterialSlot = ItemRegistry.ITEM_AIR.getId();
                    upperMaterialSlotCount = 0;
                    upperMaterialElement.setTexture(TEXTURE_TRANSPARENCY);
                    upperMaterialText.setText("");
                    upperMaterialText.build();
                }
                else
                {
                    if (upperMaterialSlotCount == 1)
                    {
                        upperMaterialText.setText("");
                        upperMaterialText.build();
                    }

                    TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));
                    upperMaterialElement.setTexture(atlas);
                    Vector2f[] uvs = atlas.getSubTextureCoordinates(resultItem.getTexture(), 90);

                    if (uvs == null)
                        System.err.println("Invalid uvs detected in menu!");
                    else
                    {
                        upperMaterialElement.setUVs(uvs);

                        if (upperMaterialSlotCount > 1)
                        {
                            upperMaterialText.setText(String.valueOf(upperMaterialSlotCount));
                            upperMaterialText.build();
                        }
                    }
                }
            }
        }

        {
            if (lowerMaterialSlot == ItemRegistry.ITEM_AIR.getId())
            {
                lowerMaterialElement.setTexture(TEXTURE_TRANSPARENCY);
                lowerMaterialText.setText("");
                lowerMaterialText.build();
            }
            else
            {
                Item resultItem = ItemRegistry.get(lowerMaterialSlot);

                if (resultItem == null)
                    System.err.println("Invalid item detected in menu!");
                else if (lowerMaterialSlotCount <= 0)
                {
                    lowerMaterialSlot = ItemRegistry.ITEM_AIR.getId();
                    lowerMaterialSlotCount = 0;
                    lowerMaterialElement.setTexture(TEXTURE_TRANSPARENCY);
                    lowerMaterialText.setText("");
                    lowerMaterialText.build();
                }
                else
                {
                    if (lowerMaterialSlotCount == 1)
                    {
                        lowerMaterialText.setText("");
                        lowerMaterialText.build();
                    }

                    TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));
                    lowerMaterialElement.setTexture(atlas);
                    Vector2f[] uvs = atlas.getSubTextureCoordinates(resultItem.getTexture(), 90);

                    if (uvs == null)
                        System.err.println("Invalid uvs detected in menu!");
                    else
                    {
                        lowerMaterialElement.setUVs(uvs);

                        if (lowerMaterialSlotCount > 1)
                        {
                            lowerMaterialText.setText(String.valueOf(lowerMaterialSlotCount));
                            lowerMaterialText.build();
                        }
                    }
                }
            }
        }

        {
            if (craftingResultSlot == ItemRegistry.ITEM_AIR.getId())
            {
                craftingResultElement.setTexture(TEXTURE_TRANSPARENCY);
                craftingResultText.setText("");
                craftingResultText.build();
            }
            else
            {
                Item resultItem = ItemRegistry.get(craftingResultSlot);

                if (resultItem == null)
                    System.err.println("Invalid item detected in menu!");
                else if (craftingResultSlotCount <= 0)
                {
                    craftingResultSlot = ItemRegistry.ITEM_AIR.getId();
                    craftingResultSlotCount = 0;
                    craftingResultElement.setTexture(TEXTURE_TRANSPARENCY);
                    craftingResultText.setText("");
                    craftingResultText.build();
                }
                else
                {
                    if (craftingResultSlotCount == 1)
                    {
                        craftingResultText.setText("");
                        craftingResultText.build();
                    }

                    TextureAtlas atlas = Objects.requireNonNull(TextureAtlasManager.get("items"));
                    craftingResultElement.setTexture(atlas);
                    Vector2f[] uvs = atlas.getSubTextureCoordinates(resultItem.getTexture(), 90);

                    if (uvs == null)
                        System.err.println("Invalid uvs detected in menu!");
                    else
                    {
                        craftingResultElement.setUVs(uvs);

                        if (craftingResultSlotCount > 1)
                        {
                            craftingResultText.setText(String.valueOf(craftingResultSlotCount));
                            craftingResultText.build();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update()
    {
        if (grabbedItemText.isActive())
            grabbedItemText.setPosition(InputManager.getMousePosition().add(new Vector2f(7.5f, 7.5f)));

        if (grabbedItemElement.isActive())
            grabbedItemElement.setPosition(InputManager.getMousePosition().sub(new Vector2f(16.0f, 16.0f)));

        List<CraftingRecipe> craftingRecipes = CraftingRecipeRegistry.getAll()
                .stream().filter(CraftingRecipe::isCompositorRecipe)
                .toList();
        boolean wasMatch = false;

        for (CraftingRecipe recipe : craftingRecipes)
        {
            short[][] primitiveCraftingSlots = new short[2][1];

            primitiveCraftingSlots[0][0] = upperMaterialSlot;
            primitiveCraftingSlots[1][0] = lowerMaterialSlot;

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
        return "menu_compositor";
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

                upperMaterialSlotCount--;
                lowerMaterialSlotCount--;

                craftingResultSlotCount = 0;
                build();
            }
        });

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
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

        button.setTexture(TEXTURE_TRANSPARENCY);
        button.setTransparent(true);

        return button;
    }

    private void setupUpperMaterialSlotEvents(@NotNull ButtonUIElement button)
    {
        button.addOnLeftClickedEvent(() ->
        {
            if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
                return;

            if (upperMaterialSlot != ItemRegistry.ITEM_AIR.getId() && grabbedItemId == ItemRegistry.ITEM_AIR.getId())
            {
                grabbedItemId = upperMaterialSlot;
                grabbedItemElement.setTexture((TextureAtlas) upperMaterialElement.getTexture());
                grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(upperMaterialSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                grabbedItemCount = upperMaterialSlotCount;
                updateGrabbedItemText();

                upperMaterialSlotCount = 0;
                build();

                return;
            }

            if (upperMaterialSlot == ItemRegistry.ITEM_AIR.getId() && grabbedItemElement.isActive())
            {
                grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
                grabbedItemElement.setActive(false);

                upperMaterialSlotCount = grabbedItemCount;
                upperMaterialSlot = grabbedItemId;

                grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                grabbedItemCount = 0;

                updateGrabbedItemText();
                build();

                return;
            }

            if (upperMaterialSlot != grabbedItemId && grabbedItemElement.isActive())
            {
                short oldGrabbedId = grabbedItemId;
                byte oldGrabbedCount = grabbedItemCount;

                grabbedItemId = upperMaterialSlot;
                grabbedItemCount = upperMaterialSlotCount;

                grabbedItemElement.setTexture((TextureAtlas) upperMaterialElement.getTexture());
                grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(upperMaterialSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                updateGrabbedItemText();

                upperMaterialSlot  = oldGrabbedId;
                upperMaterialSlotCount = oldGrabbedCount;

                build();
                return;
            }

            if (upperMaterialSlot == grabbedItemId)
            {
                grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
                grabbedItemElement.setActive(false);

                upperMaterialSlotCount = (byte) (upperMaterialSlotCount  + grabbedItemCount);

                grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                grabbedItemCount = 0;

                updateGrabbedItemText();
                build();
            }
        });

        button.addOnRightClickedEvent(() ->
        {
            if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
                return;

            if (grabbedItemCount > 0 && (upperMaterialSlot == ItemRegistry.ITEM_AIR.getId() || upperMaterialSlot == grabbedItemId))
            {
                upperMaterialSlot = grabbedItemId;

                if (upperMaterialSlotCount < 0)
                    upperMaterialSlotCount = 1;
                else
                    upperMaterialSlotCount = (byte) (upperMaterialSlotCount + 1);

                grabbedItemCount--;
                updateGrabbedItemText();

                if (grabbedItemCount <= 0)
                {
                    grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                    grabbedItemElement.setActive(false);
                }

                build();
            }
        });

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void setupLowerMaterialSlotEvents(@NotNull ButtonUIElement button)
    {
        button.addOnLeftClickedEvent(() ->
        {
            if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
                return;

            if (lowerMaterialSlot != ItemRegistry.ITEM_AIR.getId() && grabbedItemId == ItemRegistry.ITEM_AIR.getId())
            {
                grabbedItemId = lowerMaterialSlot;
                grabbedItemElement.setTexture((TextureAtlas) lowerMaterialElement.getTexture());
                grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(lowerMaterialSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                grabbedItemCount = lowerMaterialSlotCount;
                updateGrabbedItemText();

                lowerMaterialSlotCount = 0;
                build();

                return;
            }

            if (lowerMaterialSlot == ItemRegistry.ITEM_AIR.getId() && grabbedItemElement.isActive())
            {
                grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
                grabbedItemElement.setActive(false);

                lowerMaterialSlotCount = grabbedItemCount;
                lowerMaterialSlot = grabbedItemId;

                grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                grabbedItemCount = 0;

                updateGrabbedItemText();
                build();

                return;
            }

            if (lowerMaterialSlot != grabbedItemId && grabbedItemElement.isActive())
            {
                short oldGrabbedId = grabbedItemId;
                byte oldGrabbedCount = grabbedItemCount;

                grabbedItemId = lowerMaterialSlot;
                grabbedItemCount = lowerMaterialSlotCount;

                grabbedItemElement.setTexture((TextureAtlas) lowerMaterialElement.getTexture());
                grabbedItemElement.setUVs(TextureAtlasManager.get("items").getSubTextureCoordinates(Objects.requireNonNull(ItemRegistry.get(lowerMaterialSlot)).getTexture(), 90));
                grabbedItemElement.setPosition(InputManager.getMousePosition());
                grabbedItemElement.setActive(true);

                updateGrabbedItemText();

                lowerMaterialSlot  = oldGrabbedId;
                lowerMaterialSlotCount = oldGrabbedCount;

                build();
                return;
            }

            if (lowerMaterialSlot == grabbedItemId)
            {
                grabbedItemElement.setTexture(TEXTURE_TRANSPARENCY);
                grabbedItemElement.setActive(false);

                lowerMaterialSlotCount = (byte) (lowerMaterialSlotCount  + grabbedItemCount);

                grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                grabbedItemCount = 0;

                updateGrabbedItemText();
                build();
            }
        });

        button.addOnRightClickedEvent(() ->
        {
            if (grabbedItemId == 0 && (button.getTexture() == TEXTURE_TRANSPARENCY || button.getTexture() == TEXTURE_SLOT_DARKEN))
                return;

            if (grabbedItemCount > 0 && (lowerMaterialSlot == ItemRegistry.ITEM_AIR.getId() || lowerMaterialSlot == grabbedItemId))
            {
                lowerMaterialSlot = grabbedItemId;

                if (lowerMaterialSlotCount < 0)
                    lowerMaterialSlotCount = 1;
                else
                    lowerMaterialSlotCount = (byte) (lowerMaterialSlotCount + 1);

                grabbedItemCount--;
                updateGrabbedItemText();

                if (grabbedItemCount <= 0)
                {
                    grabbedItemId = ItemRegistry.ITEM_AIR.getId();
                    grabbedItemElement.setActive(false);
                }

                build();
            }
        });

        button.addOnHoveringBeginEvent(() -> handleSlotHoverBegin(button));
        button.addOnHoveringEndEvent(() -> handleSlotHoverEnd(button));
    }

    private void setupInventorySlotEvents(@NotNull ButtonUIElement button, int x, int y)
    {
        button.addOnLeftClickedEvent(() -> handleSlotLeftClick(x, y, inventory.slots, inventory.slotCounts, slotElements, button, false));
        button.addOnRightClickedEvent(() -> handleSlotRightClick(x, y, inventory.slots, inventory.slotCounts, slotElements, button, false));
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