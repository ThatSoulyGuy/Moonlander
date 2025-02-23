package com.thatsoulyguy.moonlander.ui;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Static
@Manager(UIPanel.class)
public class UIManager
{
    private static final @NotNull ConcurrentMap<String, UIPanel> uiPanelsMap = new ConcurrentHashMap<>();

    private static @EffectivelyNotNull GameObject canvas;

    private UIManager() { }

    public static void initialize()
    {
        canvas = GameObject.create("ui.canvas", Layer.UI);
    }

    public static void register(@NotNull UIPanel object)
    {
        canvas.addChild(object.object);

        uiPanelsMap.putIfAbsent(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        if (!uiPanelsMap.containsKey(name))
        {
            System.err.println("UI element '" + name + "' not found!");
            return;
        }

        canvas.removeChild(canvas.getChild("ui." + name));
        GameObjectManager.unregister("ui." + name);

        uiPanelsMap.remove(name);
    }

    public static void update()
    {
        uiPanelsMap.values().parallelStream().forEach(UIPanel::update);
    }

    public static void serialize(@NotNull String directory)
    {
        File saveFile = new File(directory, "ui.bin");

        File uiPanelsDirectory = new File(directory, "ui");

        if (!uiPanelsDirectory.exists())
            uiPanelsDirectory.mkdirs();

        try (FileOutputStream fileOutputStream = new FileOutputStream(saveFile))
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeInt(uiPanelsMap.size());

            uiPanelsMap.values().forEach((object ->
            {
                try
                {
                    objectOutputStream.writeUTF(object.getName());
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                object.save(uiPanelsDirectory.getAbsolutePath());
            }));

            System.out.println("Saved ui elements");

            objectOutputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to serialize ui! " + exception.getMessage());
        }
    }

    public static void deserialize(@NotNull String directory)
    {
        File saveFile = new File(directory, "ui.bin");
        File uiPanelsDirectory = new File(directory, "ui");

        try (FileInputStream fileInputStream = new FileInputStream(saveFile))
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            int uiPanelsMapSize = objectInputStream.readInt();

            for (int e = 0; e < uiPanelsMapSize; e++)
            {
                String name = objectInputStream.readUTF();

                UIPanel panel = UIPanel.load(new File(uiPanelsDirectory, "ui." + name + ".bin"));

                uiPanelsMap.putIfAbsent(panel.getName(), panel);
            }

            System.out.println("Loaded ui elements");

            objectInputStream.close();
        }
        catch (Exception exception)
        {
            System.err.println("Failed to deserialize ui! " + exception.getMessage());
        }
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

    public static void stop()
    {
        uiPanelsMap.values().forEach(UIPanel::uninitialize);
        uiPanelsMap.clear();

        GameObjectManager.unregister(canvas.getName(), true);
    }

    public static void uninitialize() { }
}