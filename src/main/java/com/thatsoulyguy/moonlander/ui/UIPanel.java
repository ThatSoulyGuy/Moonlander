package com.thatsoulyguy.moonlander.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.elements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.FileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@CustomConstructor("create")
public class UIPanel extends Component
{
    private String name;

    private Vector2i offset = new Vector2i(0, 0);

    private PanelAlignment panelAlignment = PanelAlignment.MIDDLE_CENTER;

    private UIPanel() { }

    @Override
    public void initialize()
    {
        getGameObject().getTransform().setLocalScale(new Vector3f(Settings.UI_SCALE.getValue()));
    }

    @Override
    public void update()
    {
        align();
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull PanelAlignment getPanelAlignment()
    {
        return panelAlignment;
    }

    public void setPanelAlignment(@NotNull PanelAlignment panelAlignment)
    {
        this.panelAlignment = panelAlignment;
    }

    public @NotNull Vector2i getOffset()
    {
        return offset;
    }

    public void setOffset(@NotNull Vector2i offset)
    {
        this.offset = offset;
    }

    public <T extends UIElement> @Nullable T get(@NotNull String name, @NotNull Class<T> clazz)
    {
        if (!getGameObject().hasChild(name))
            return null;

        return getGameObject().getChild(name).getComponent(clazz);
    }

    public <T extends UIElement> @NotNull T getNotNull(@NotNull String name, @NotNull Class<T> clazz)
    {
        return getGameObject().getChild(name).getComponentNotNull(clazz);
    }

    private void align()
    {
        Vector2i windowDimensions = Window.getDimensions();

        float windowWidth = windowDimensions.x;
        float windowHeight = windowDimensions.y;

        Collection<GameObject> children = getGameObject().getChildren();

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (GameObject child : children)
        {
            Vector3f pos = child.getTransform().getLocalPosition();
            Vector3f scale = child.getTransform().getLocalScale();

            float halfWidth = scale.x / 2f;
            float halfHeight = scale.y / 2f;

            minX = Math.min(minX, pos.x - halfWidth);
            maxX = Math.max(maxX, pos.x + halfWidth);
            minY = Math.min(minY, pos.y - halfHeight);
            maxY = Math.max(maxY, pos.y + halfHeight);
        }

        float panelWidth = maxX - minX;
        float panelHeight = maxY - minY;

        final float centerOffsetX = windowWidth / 2f - (minX + panelWidth / 2f);
        final float centerOffsetY = windowHeight / 2f - (minY + panelHeight / 2f);

        float offsetX, offsetY;

        switch (panelAlignment)
        {
            case UPPER_LEFT ->
            {
                offsetX = 0 - minX;
                offsetY = 0 - minY;
            }

            case UPPER_CENTER ->
            {
                offsetX = centerOffsetX;
                offsetY = 0 - minY;
            }

            case UPPER_RIGHT ->
            {
                offsetX = windowWidth - maxX;
                offsetY = 0 - minY;
            }

            case MIDDLE_LEFT ->
            {
                offsetX = 0 - minX;
                offsetY = centerOffsetY;
            }

            case MIDDLE_CENTER ->
            {
                offsetX = centerOffsetX;
                offsetY = centerOffsetY;
            }

            case MIDDLE_RIGHT ->
            {
                offsetX = windowWidth - maxX;
                offsetY = centerOffsetY;
            }

            case LOWER_LEFT ->
            {
                offsetX = 0 - minX;
                offsetY = windowHeight - maxY;
            }

            case LOWER_CENTER ->
            {
                offsetX = centerOffsetX;
                offsetY = windowHeight - maxY;
            }

            case LOWER_RIGHT ->
            {
                offsetX = windowWidth - maxX;
                offsetY = windowHeight - maxY;
            }

            default ->
            {
                offsetX = centerOffsetX;
                offsetY = centerOffsetY;
            }
        }

        getGameObject().getTransform().setLocalPosition(new Vector3f(offsetX + offset.x, offsetY - offset.y, 0));
    }

    private static @NotNull Vector2f parseVector2f(@NotNull String input)
    {
        String trimmed = input.trim();

        if (trimmed.startsWith("[") && trimmed.endsWith("]"))
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        else
            throw new IllegalArgumentException("Input must be enclosed in square brackets: " + input);

        String[] parts = trimmed.split(",");

        if (parts.length != 2)
            throw new IllegalArgumentException("Input must have exactly two numbers: " + input);

        try
        {
            float x = Float.parseFloat(parts[0].trim());
            float y = Float.parseFloat(parts[1].trim());

            return new Vector2f(x, y);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid number format in input: " + input, e);
        }
    }

    private static @NotNull Class<? extends UIElement> parseType(@NotNull String input)
    {
        return switch (input)
        {
            case "image" -> ImageUIElement.class;
            case "text" -> TextUIElement.class;
            case "button" -> ButtonUIElement.class;
            default -> throw new IllegalStateException("Unexpected value: " + input);
        };
    }

    public static @NotNull TextUIElement.TextAlignment parseTextAlignment(@NotNull String input)
    {
        return switch (input.toLowerCase())
        {
            case "upper_left" -> TextUIElement.TextAlignment.UPPER_LEFT;
            case "upper_center" -> TextUIElement.TextAlignment.UPPER_CENTER;
            case "upper_right" -> TextUIElement.TextAlignment.UPPER_RIGHT;
            case "middle_left" -> TextUIElement.TextAlignment.MIDDLE_LEFT;
            case "middle_center" -> TextUIElement.TextAlignment.MIDDLE_CENTER;
            case "middle_right" -> TextUIElement.TextAlignment.MIDDLE_RIGHT;
            case "lower_left" -> TextUIElement.TextAlignment.LOWER_LEFT;
            case "lower_center" -> TextUIElement.TextAlignment.LOWER_CENTER;
            case "lower_right" -> TextUIElement.TextAlignment.LOWER_RIGHT;

            default -> throw new IllegalArgumentException("Unknown alignment: " + input);
        };
    }

    @Override
    public void uninitialize()
    {
        UIManager.unregister(getName());
    }

    public static @NotNull GameObject fromJson(@NotNull String name, @NotNull AssetPath path)
    {
        GameObject result = createGameObject(name);

        Gson gson = new Gson();

        Type listType = new TypeToken<List<JsonEntry>>() {}.getType();
        List<JsonEntry> entries = gson.fromJson(FileHelper.readFile(path.getFullPath()), listType);

        Map<String, List<com.thatsoulyguy.moonlander.util.Pair<String, String>>> rawElements = entries.stream()
            .collect(Collectors.groupingBy(
                    JsonEntry::getName,
                    LinkedHashMap::new,
                    Collectors.mapping(
                            entry -> new com.thatsoulyguy.moonlander.util.Pair<>(entry.getProperty().getKey(), entry.getProperty().getValue()),
                            Collectors.toList()
                    )
            ));

        for (List<com.thatsoulyguy.moonlander.util.Pair<String, String>> entry : rawElements.values())
        {
            String elementName = entry.get(0).a();
            Vector2f position = parseVector2f(entry.get(1).a());

            position.y = -position.y;

            Vector2f dimensions = parseVector2f(entry.get(2).a());
            Class<? extends UIElement> type = parseType(entry.get(3).a());

            if (type == ImageUIElement.class)
            {
                GameObject element = UIElement.createGameObject("ui." + elementName, type, position, dimensions, result);
                element.getComponentNotNull(ImageUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get(entry.get(4).a())));
            }
            else if (type == TextUIElement.class)
            {
                GameObject element = UIElement.createGameObject("ui." + elementName, type, position, dimensions, result);

                TextUIElement text = element.getComponentNotNull(TextUIElement.class);

                text.setFontSize(Integer.parseInt(entry.get(4).a()));
                text.setAlignment(parseTextAlignment(entry.get(5).a()));
                text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
                text.setText(entry.get(6).a());

                text.build();
            }
            else if (type == ButtonUIElement.class)
            {
                GameObject element = UIElement.createGameObject("ui." + elementName, type, position, dimensions, result);
                ButtonUIElement button = element.getComponentNotNull(ButtonUIElement.class);

                button.setTexture(Objects.requireNonNull(TextureManager.get(entry.get(4).a())));
                button.setOnLeftPressedCallback(entry.get(5).a());
                button.setOnRightPressedCallback(entry.get(6).a());
                button.setOnHoverBeginCallback(entry.get(7).a());
                button.setOnHoverEndCallback(entry.get(8).a());
                button.setOnLeftReleasedCallback(entry.get(9).a());
                button.setOnRightReleasedCallback(entry.get(10).a());
            }
        }

        return result;
    }

    public static @NotNull UIPanel create(@NotNull String name)
    {
        UIPanel result = new UIPanel();

        result.name = name;

        UIManager.register(result);

        return result;
    }

    public static @NotNull GameObject createGameObject(@NotNull String name)
    {
        GameObject result = UIManager.getCanvas().addChild(GameObject.create(name, Layer.UI));

        result.addComponent(create(name));

        result.setTransient(true);

        return result;
    }

    public enum PanelAlignment
    {
        UPPER_LEFT,
        UPPER_CENTER,
        UPPER_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,
        LOWER_LEFT,
        LOWER_CENTER,
        LOWER_RIGHT
    }
}