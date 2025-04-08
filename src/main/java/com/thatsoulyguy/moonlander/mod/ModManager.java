package com.thatsoulyguy.moonlander.mod;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Static
@Manager(Mod.class)
public class ModManager
{
    private static final @NotNull Map<String, Mod> modsMap = new ConcurrentHashMap<>();

    private static final @NotNull List<URLClassLoader> modClassLoaders = new ArrayList<>();

    private ModManager() { }

    @SuppressWarnings("unchecked")
    public static void loadFromDirectory(@NotNull String directory)
    {
        File modsDirectory = new File(directory);

        if (!modsDirectory.exists())
            modsDirectory.mkdirs();

        File[] jarFiles = modsDirectory.listFiles((_, name) -> name.toLowerCase().endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0)
            return;

        try
        {
            URL[] urls = new URL[jarFiles.length];

            for (int i = 0; i < jarFiles.length; i++)
                urls[i] = jarFiles[i].toURI().toURL();

            URLClassLoader classLoader = new URLClassLoader(urls, Mod.class.getClassLoader());

            modClassLoaders.add(classLoader);

            for (File jarFile : jarFiles)
            {
                try (JarFile jar = new JarFile(jarFile))
                {
                    Enumeration<JarEntry> entries = jar.entries();

                    while (entries.hasMoreElements())
                    {
                        JarEntry entry = entries.nextElement();

                        if (entry.getName().endsWith("module-info.class"))
                            continue;

                        if (entry.getName().endsWith(".class") && entry.getName().contains("Application"))
                        {
                            String className = entry.getName()
                                    .replace('/', '.')
                                    .replace('\\', '.')
                                    .replace(".class", "");

                            Class<?> loadedClass = classLoader.loadClass(className);

                            if (Mod.class.isAssignableFrom(loadedClass) && !loadedClass.isInterface() && !Modifier.isAbstract(loadedClass.getModifiers()))
                            {
                                Mod instantiation = ((Class<? extends Mod>) loadedClass).getDeclaredConstructor().newInstance();

                                register(instantiation);

                                System.out.println("Loaded mod '" + instantiation.getDisplayName() + "' (" + instantiation.getRegistryName() + ")!");
                            }
                        }
                    }
                }
                catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void registerPatches()
    {
        modsMap.values().forEach(Mod::registerPatches);
    }

    public static void preInitialize()
    {
        modsMap.values().forEach(Mod::preInitialize);
    }

    public static void initialize()
    {
        modsMap.values().forEach(Mod::initialize);
    }

    public static void update()
    {
        modsMap.values().forEach(Mod::update);
    }

    public static void render()
    {
        modsMap.values().forEach(Mod::render);
    }

    public static void register(@NotNull Mod object)
    {
        modsMap.putIfAbsent(object.getRegistryName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        modsMap.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return modsMap.containsKey(name);
    }

    public static @Nullable Mod get(@NotNull String name)
    {
        return modsMap.getOrDefault(name, null);
    }

    public static @NotNull List<Mod> getAll()
    {
        return List.copyOf(modsMap.values());
    }

    public static void uninitialize()
    {
        modsMap.values().forEach(Mod::uninitialize);

        for (URLClassLoader cl : modClassLoaders)
        {
            try
            {
                cl.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        modClassLoaders.clear();
    }
}