package com.thatsoulyguy.moonlander.ui;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Static
@Manager(UIPanel.class)
public class UIManager
{
    private static GameObject canvas;

    private static final @NotNull Map<String, UIPanel> uiPanelsMap = new ConcurrentHashMap<>();

    private UIManager() { }

    public static void initialize()
    {
        canvas = GameObject.create("ui.canvas", Layer.UI);

        canvas.setTransient(true);
    }

    public static void register(@NotNull UIPanel object)
    {
        uiPanelsMap.putIfAbsent(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        uiPanelsMap.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return uiPanelsMap.containsKey(name);
    }

    public static @Nullable UIPanel get(@NotNull String name)
    {
        return uiPanelsMap.getOrDefault(name, null);
    }

    public static @NotNull List<UIPanel> getAll()
    {
        return List.copyOf(uiPanelsMap.values());
    }

    public static void uninitialize() { }

    public static @NotNull GameObject getCanvas()
    {
        return canvas;
    }
}