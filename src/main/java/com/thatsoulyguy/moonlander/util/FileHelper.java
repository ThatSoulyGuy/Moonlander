package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

@Static
public class FileHelper
{
    private FileHelper() { }

    public static @Nullable String getExtension(@NotNull String path)
    {
        int index = path.lastIndexOf('.');

        if(index == -1)
            return null;

        return path.substring(index + 1);
    }

    public static @NotNull String getName(@NotNull String path)
    {
        int index = path.lastIndexOf('/');

        if(index == -1)
            return path;

        return path.substring(index + 1);
    }

    public static @Nullable String getDirectory(@NotNull String path)
    {
        int index = path.lastIndexOf('/');

        if(index == -1)
            return null;

        return path.substring(0, index);
    }

    public static @NotNull String getFileName(@NotNull String path)
    {
        int index = path.lastIndexOf('.');

        if(index == -1)
            return path;

        return path.substring(0, index);
    }

    public static @NotNull String readFile(@NotNull String path)
    {
        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(FileHelper.class.getResourceAsStream(path)))))
        {
            String line = "";

            while ((line = reader.readLine()) != null)
                result.append(line).append("\n");
        }
        catch (IOException e)
        {
            System.err.println("Couldn't find the file at " + path);
        }

        return result.toString();
    }

    public static @NotNull String getPersistentDataPath(@NotNull String appName)
    {
        try
        {
            String userHome = System.getProperty("user.home");
            String os = System.getProperty("os.name").toLowerCase();
            String path;

            if (os.contains("win"))
            {
                String appData = System.getenv("APPDATA");
                path = appData != null ? appData + File.separator + appName : userHome + File.separator + appName;
            }
            else if (os.contains("mac"))
                path = userHome + "/Library/Application Support/" + appName;
            else
                path = userHome + "/.local/share/" + appName;

            File dir = new File(path);

            if (!dir.exists())
            {
                if (!dir.mkdirs())
                    throw new IOException("Failed to create directory: " + path);
            }

            return dir.getAbsolutePath();
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }

    public static ByteBuffer loadResourceAsByteBuffer(String resourcePath)
    {
        try (InputStream inputStream = FileHelper.class.getResourceAsStream(resourcePath))
        {
            if (inputStream == null)
                throw new IOException("Resource not found: " + resourcePath);

            return readInputStreamToByteBuffer(inputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    private static ByteBuffer readInputStreamToByteBuffer(InputStream inputStream) throws IOException
    {
        try (ReadableByteChannel channel = Channels.newChannel(inputStream))
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);

            while (channel.read(buffer) != -1)
            {
                if (buffer.remaining() == 0)
                {
                    ByteBuffer expanded = ByteBuffer.allocateDirect(buffer.capacity() * 2);

                    buffer.flip();

                    expanded.put(buffer);

                    buffer = expanded;
                }
            }

            buffer.flip();
            return buffer;
        }
    }
}