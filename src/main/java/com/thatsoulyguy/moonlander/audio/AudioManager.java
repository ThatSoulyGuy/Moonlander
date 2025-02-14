package com.thatsoulyguy.moonlander.audio;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Static
@Manager(AudioClip.class)
public class AudioManager
{
    private static final @NotNull Map<String, AudioClip> audioClipMap = new ConcurrentHashMap<>();

    private static long device;
    private static long context;

    private AudioManager() { }

    public static void initialize()
    {
        device = ALC10.alcOpenDevice((ByteBuffer) null);

        if (device == 0)
            throw new IllegalStateException("Failed to open the default OpenAL device.");

        int[] attributes = {0};
        context = ALC10.alcCreateContext(device, attributes);

        if (context == 0)
            throw new IllegalStateException("Failed to create OpenAL context.");

        ALC10.alcMakeContextCurrent(context);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);

        AL.createCapabilities(alcCapabilities);
    }

    public static void register(@NotNull AudioClip object)
    {
        audioClipMap.put(object.getName(), object);
    }

    public static void unregister(@NotNull String name)
    {
        audioClipMap.remove(name);
    }

    public static boolean has(@NotNull String name)
    {
        return audioClipMap.containsKey(name);
    }

    public static @Nullable AudioClip get(@NotNull String name)
    {
        return audioClipMap.getOrDefault(name, null).clone();
    }

    public static @NotNull List<AudioClip> getAll()
    {
        return List.copyOf(audioClipMap.values());
    }

    public static void uninitialize()
    {
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}