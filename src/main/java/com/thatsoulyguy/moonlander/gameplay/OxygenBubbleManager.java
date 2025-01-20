package com.thatsoulyguy.moonlander.gameplay;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Static
@Manager(OxygenBubble.class)
public class OxygenBubbleManager
{
    private static final @NotNull List<OxygenBubble> oxygenBubbleList = new ArrayList<>();

    public static void register(@NotNull OxygenBubble object)
    {
        oxygenBubbleList.add(object);
    }

    public static void unregister(@NotNull OxygenBubble object)
    {
        oxygenBubbleList.remove(object);
    }

    public static boolean has(@NotNull OxygenBubble object)
    {
        return oxygenBubbleList.contains(object);
    }

    public static @Nullable OxygenBubble get(int index)
    {
        return oxygenBubbleList.get(index);
    }

    public static @NotNull List<OxygenBubble> getAll()
    {
        return List.copyOf(oxygenBubbleList);
    }

    public static void uninitialize() { }
}