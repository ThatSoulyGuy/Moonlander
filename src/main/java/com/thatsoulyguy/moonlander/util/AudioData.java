package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AudioData
{
    private @EffectivelyNotNull ByteBuffer data;
    private int format;
    private int sampleRate;

    private static final @NotNull Map<String, AudioData> cache = new ConcurrentHashMap<>();

    private AudioData() { }

    public ByteBuffer getData()
    {
        return data;
    }

    public int getFormat()
    {
        return format;
    }

    public int getSampleRate()
    {
        return sampleRate;
    }

    public static void uninitialize()
    {
        for (AudioData data : cache.values())
            MemoryUtil.memFree(data.data);
    }

    public static @NotNull AudioData create(@NotNull AssetPath path)
    {
        String key = path.getFullPath();

        if (cache.containsKey(key))
            return cache.get(key);

        try
        {
            ByteBuffer vorbisData = ioResourceToByteBuffer(key, 32 * 1024);

            IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(vorbisData, errorBuffer, null);

            if (decoder == MemoryUtil.NULL)
                throw new RuntimeException("Failed to open OGG file. Error: " + errorBuffer.get(0));

            STBVorbisInfo info = STBVorbisInfo.create();

            STBVorbis.stb_vorbis_get_info(decoder, info);

            int channels = info.channels();
            int sampleRate = info.sample_rate();

            int samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);

            ShortBuffer pcm = BufferUtils.createShortBuffer(samples * channels);

            int actualSamples = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);

            pcm.limit(actualSamples * channels);

            STBVorbis.stb_vorbis_close(decoder);

            ByteBuffer data = MemoryUtil.memAlloc(pcm.remaining() * 2);

            while (pcm.hasRemaining())
                data.putShort(pcm.get());

            data.flip();

            int openALFormat;

            if (channels == 1)
                openALFormat = AL10.AL_FORMAT_MONO16;
            else if (channels == 2)
                openALFormat = AL10.AL_FORMAT_STEREO16;
            else
                throw new UnsupportedAudioFileException("Only mono and stereo audio supported");

            AudioData result = new AudioData();

            result.data = data;
            result.format = openALFormat;
            result.sampleRate = sampleRate;

            cache.put(key, result);

            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new AudioData();
        }
    }

    private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws Exception
    {
        InputStream source = AudioData.class.getResourceAsStream(resource);

        if (source == null)
            throw new IllegalArgumentException("Resource not found: " + resource);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        while ((bytesRead = source.read(buffer)) != -1)
            baos.write(buffer, 0, bytesRead);

        source.close();

        byte[] bytes = baos.toByteArray();

        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();

        return byteBuffer;
    }
}