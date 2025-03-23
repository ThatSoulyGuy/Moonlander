package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.input.InputManager;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.elements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CustomConstructor("create")
public class InventorySystem extends Component
{
    private transient Inventory inventory;

    private static InventorySystem instance = null;

    private transient GrabbedItem grabbedItem;

    private transient TextUIElement hoveredItemText;

    private InventorySystem() { }

    @Override
    public void initialize()
    {
        instance = this;

        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
                panel.getNotNull("ui.slot_" + x + "_" + y + "_text", TextUIElement.class).getGameObject().setActive(false);
        }

        ImageUIElement grabbedItemImage = UIElement.createGameObject("ui.grabbed_item", ImageUIElement.class, new Vector2f(0.0f, 0.0f), new Vector2f(32.0f, 32.0f).mul(Settings.UI_SCALE.getValue()), UIManager.getCanvas()).getComponentNotNull(ImageUIElement.class);

        TextUIElement grabbedItemText = UIElement.createGameObject("ui.grabbed_item_text", TextUIElement.class, new Vector2f(0.0f, 0.0f), new Vector2f(30.0f, 30.0f).mul(Settings.UI_SCALE.getValue()), UIManager.getCanvas()).getComponentNotNull(TextUIElement.class);

        grabbedItemText.setFontSize(15);
        grabbedItemText.setAlignment(TextUIElement.TextAlignment.MIDDLE_RIGHT);
        grabbedItemText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
        grabbedItemText.setText("0");
        grabbedItemText.build();

        grabbedItem = new GrabbedItem(grabbedItemImage, grabbedItemText, new Wrapper<>((short) 0), new Wrapper<>((byte) 0));

        grabbedItem.image.getGameObject().setActive(false);
        grabbedItem.text.getGameObject().setActive(false);

        hoveredItemText = UIElement.createGameObject("ui.hovered_item", TextUIElement.class, new Vector2f(0, 0), new Vector2f(500, 50), UIManager.getCanvas()).getComponentNotNull(TextUIElement.class);

        hoveredItemText.setText("Some");
        hoveredItemText.setFontSize(15);
        hoveredItemText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
        hoveredItemText.setAlignment(TextUIElement.TextAlignment.MIDDLE_CENTER);

        hoveredItemText.getGameObject().setActive(false);
    }

    @Override
    public void update()
    {
        Vector3f textOffset = new Vector3f(3.0f, 11.0f, 0.0f).mul(Settings.UI_SCALE.getValue());

        grabbedItem.image.getGameObject().getTransform().setLocalPosition(new Vector3f(InputManager.getMousePosition().x, InputManager.getMousePosition().y, 0.0f));
        grabbedItem.text.getGameObject().getTransform().setLocalPosition(new Vector3f(InputManager.getMousePosition().x, InputManager.getMousePosition().y, 0.0f).add(textOffset, new Vector3f()));

        if (grabbedItem.count.get() <= 0)
        {
            grabbedItem.id.set((short) 0);
            grabbedItem.count.set((byte) 0);

            grabbedItem.image.getGameObject().setActive(false);
            grabbedItem.text.getGameObject().setActive(false);
        }

        Vector2i dimensions = Window.getDimensions();

        float baseX = dimensions.x;
        float baseY = 0;

        float offsetX = -510.0f;
        float offsetY = 60.0f;

        hoveredItemText.getGameObject().getTransform().setLocalPosition(new Vector3f(baseX + offsetX, baseY + offsetY, 0.0f));

        hoveredItemText.getGameObject().setActive(true);
    }

    public void generate()
    {
        inventory.registerOnSlotChangedCallback((position, slot) -> Inventory.buildSlotWithPosition(position, slot, getGameObject()));
    }

    public @NotNull GrabbedItem getGrabbedItem()
    {
        return grabbedItem;
    }

    public void buildGrabbedItem()
    {
        if (grabbedItem.count().get() <= 0)
        {
            grabbedItem.text.getGameObject().setActive(false);
            grabbedItem.image.setTexture(Objects.requireNonNull(TextureManager.get("ui.transparency")));
        }
        else
        {
            if (grabbedItem.count().get() > 1)
            {
                grabbedItem.text.getGameObject().setActive(true);

                grabbedItem.text.setText(String.valueOf(grabbedItem.count.get()));
                grabbedItem.text.build();
            }
            else
                grabbedItem.text.getGameObject().setActive(false);

            grabbedItem.image.setTexture(Objects.requireNonNull(TextureAtlasManager.get("items")).createSubTexture(Objects.requireNonNull(ItemRegistry.get(grabbedItem.id.get())).getTexture(), false));
        }
    }

    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    public void setInventory(@NotNull Inventory inventory)
    {
        this.inventory = inventory;
    }

    public static boolean isSlotFormat(@NotNull String input)
    {
        Pattern pattern = Pattern.compile(".*_(-?\\d+)_(-?\\d+)$");
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }

    public static @NotNull Vector2i parseSlot(@NotNull String input)
    {
        Pattern pattern = Pattern.compile(".*_(-?\\d+)_(-?\\d+)$");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid slot format: " + input);

        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));

        return new Vector2i(x, y);
    }

    public static @NotNull InventorySystem getInstance()
    {
        return instance;
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        Vector2i position = parseSlot(element.getGameObject().getName());
        InventorySystem instance = InventorySystem.getInstance();

        if (instance.grabbedItem.id.get() == 0)
        {
            Inventory.SlotData oldSlotData = instance.inventory.getSlot(position);

            instance.inventory.setSlot(position, new Inventory.SlotData((short) 0, (byte) 0));

            instance.grabbedItem.id.set(oldSlotData.id());
            instance.grabbedItem.count.set(oldSlotData.count());

            instance.grabbedItem.image.getGameObject().setActive(true);
            instance.grabbedItem.text.getGameObject().setActive(true);

            instance.buildGrabbedItem();
        }
        else if (instance.grabbedItem.image.getGameObject().isActive() && instance.inventory.getSlot(position).id() == 0)
        {
            instance.inventory.setSlot(position, new Inventory.SlotData(instance.grabbedItem.id.get(), instance.grabbedItem.count.get()));

            instance.grabbedItem.id.set((short) 0);
            instance.grabbedItem.count.set((byte) 0);

            instance.grabbedItem.image.getGameObject().setActive(false);
            instance.grabbedItem.text.getGameObject().setActive(false);
        }
        else if (instance.grabbedItem.image.getGameObject().isActive() && instance.inventory.getSlot(position).id() == instance.grabbedItem.id.get())
        {
            instance.inventory.setSlot(position, new Inventory.SlotData(instance.grabbedItem.id.get(), (byte) (instance.grabbedItem.count.get() + instance.inventory.getSlot(position).count())));

            instance.grabbedItem.id.set((short) 0);
            instance.grabbedItem.count.set((byte) 0);

            instance.grabbedItem.image.getGameObject().setActive(false);
            instance.grabbedItem.text.getGameObject().setActive(false);
        }
        else if (instance.grabbedItem.image.getGameObject().isActive() && instance.inventory.getSlot(position).id() != instance.grabbedItem.id.get())
        {
            Inventory.SlotData oldSlotData = instance.inventory.getSlot(position);

            instance.inventory.setSlot(position, new Inventory.SlotData(instance.grabbedItem.id.get(), instance.grabbedItem.count.get()));

            instance.grabbedItem.id.set(oldSlotData.id());
            instance.grabbedItem.count.set(oldSlotData.count());

            instance.buildGrabbedItem();
        }
    }

    public static void onRightMousePressed(@NotNull ButtonUIElement element)
    {
        Vector2i position = parseSlot(element.getGameObject().getName());
        InventorySystem instance = InventorySystem.getInstance();

        if (instance.grabbedItem.image.getGameObject().isActive() && instance.grabbedItem.id.get() != 0)
        {
            instance.inventory.setSlot(position, new Inventory.SlotData(instance.grabbedItem.id.get(), (byte) (instance.inventory.getSlot(position).count() + 1)));

            if (instance.grabbedItem.count.get() == 0)
            {
                instance.grabbedItem.id.set((short) 0);

                instance.grabbedItem.image.getGameObject().setActive(false);
                instance.grabbedItem.text.getGameObject().setActive(false);
            }
            else
                instance.grabbedItem.count.set((byte) (instance.grabbedItem.count.get() - 1));

            instance.buildGrabbedItem();
        }
    }

    public static void onHoverBegin(@NotNull ButtonUIElement element)
    {
        if (isSlotFormat(element.getGameObject().getName()))
        {
            Vector2i position = parseSlot(element.getGameObject().getName());
            InventorySystem instance = InventorySystem.getInstance();

            instance.hoveredItemText.setText(Objects.requireNonNull(ItemRegistry.get(instance.inventory.getSlot(position).id())).getDisplayName());
            instance.hoveredItemText.build();
        }
    }

    public static void onHoverEnd(@NotNull ButtonUIElement element)
    {

    }

    public static @NotNull InventorySystem create()
    {
        return new InventorySystem();
    }

    public record GrabbedItem(@NotNull ImageUIElement image, @NotNull TextUIElement text, Wrapper<Short> id, Wrapper<Byte> count) { }

    public static class Wrapper<T>
    {
        private T t;

        public Wrapper(T t)
        {
            this.t = t;
        }

        public T get()
        {
            return t;
        }

        public void set(T t)
        {
            this.t = t;
        }
    }
}