package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.ManagerLinkedClass;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CustomConstructor("create")
public class TextureAtlas extends Component implements ManagerLinkedClass
{
    private @EffectivelyNotNull String name;
    private @EffectivelyNotNull AssetPath localDirectory;
    private @EffectivelyNotNull String directory;

    private static final int PADDING = 20;

    private int atlasSize;
    private transient ByteBuffer atlasBuffer;

    private final @NotNull transient ConcurrentMap<String, Vector2f[]> subTextureMap;
    private @Nullable transient Texture outputTexture = null;

    private TextureAtlas()
    {
        subTextureMap = new ConcurrentHashMap<>();
    }

    public void generate()
    {
        try
        {
            List<String> imageFiles = listImageFiles(directory);
            if (imageFiles.isEmpty())
            {
                System.err.println("No images found in directory: " + directory);
                return;
            }

            List<ImageData> images = new ArrayList<>();
            long totalArea = 0;

            for (String imagePath : imageFiles)
            {
                ImageData data = loadImageData(imagePath);

                if (data == null)
                {
                    System.err.println("Failed to load image: " + imagePath);
                    continue;
                }

                images.add(data);
                totalArea += (long)data.paddedWidth * (long)data.paddedHeight;
            }

            if (images.isEmpty())
            {
                System.err.println("No valid images loaded.");
                return;
            }

            int atlasSize = (int)Math.ceil(Math.sqrt(totalArea)) + 512;

            images.sort((a, b) ->
            {
                int heightCompare = Integer.compare(b.paddedHeight, a.paddedHeight);

                if (heightCompare != 0)
                    return heightCompare;

                return Integer.compare(b.paddedWidth, a.paddedWidth);
            });

            ByteBuffer atlasBuffer = createAtlasBuffer(images, atlasSize);

            if (atlasBuffer == null)
            {
                System.err.println("Could not pack textures into atlas.");
                return;
            }

            this.outputTexture = Texture.create(name + "_atlas", Texture.Filter.NEAREST, Texture.Wrapping.CLAMP_TO_EDGE, atlasSize, atlasSize, atlasBuffer);

            this.atlasSize = atlasSize;
            this.atlasBuffer = atlasBuffer.duplicate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public @NotNull String getName()
    {
        return name;
    }

    public void setName(@NotNull String name)
    {
        this.name = name;
    }

    public @Nullable Texture getOutputTexture()
    {
        return outputTexture;
    }

    public @Nullable Vector2f[] getSubTextureCoordinates(@NotNull String name)
    {
        return getSubTextureCoordinates(name, 0.0f);
    }

    public @Nullable Vector2f[] getSubTextureCoordinates(@NotNull String name, float rotation)
    {
        Vector2f[] originalUVs = subTextureMap.get(name);

        if (originalUVs == null)
            return null;

        if (rotation == 0.0f)
            return Arrays.copyOf(originalUVs, originalUVs.length);

        Vector2f[] uvs = new Vector2f[originalUVs.length];

        for (int i = 0; i < originalUVs.length; i++)
            uvs[i] = new Vector2f(originalUVs[i].x, originalUVs[i].y);

        float padding = 0.02f;

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

        for (Vector2f uv : uvs)
        {
            if (uv.x < minX)
                minX = uv.x;

            if (uv.y < minY)
                minY = uv.y;

            if (uv.x > maxX)
                maxX = uv.x;

            if (uv.y > maxY)
                maxY = uv.y;
        }

        for (int i = 0; i < uvs.length; i++)
        {
            Vector2f uv = uvs[i];

            float adjustedX = Math.max(minX, Math.min(maxX, uv.x));
            float adjustedY = Math.max(minY, Math.min(maxY, uv.y));

            uvs[i] = new Vector2f(
                    adjustedX + (uv.x > minX && uv.x < maxX ? padding : 0),
                    adjustedY + (uv.y > minY && uv.y < maxY ? padding : 0)
            );
        }

        float centerX = 0.0f;
        float centerY = 0.0f;

        for (Vector2f uv : uvs)
        {
            centerX += uv.x;
            centerY += uv.y;
        }

        centerX /= uvs.length;
        centerY /= uvs.length;

        float radians = (float) Math.toRadians(rotation);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        Vector2f[] rotatedUVs = new Vector2f[uvs.length];

        for (int i = 0; i < uvs.length; i++)
        {
            Vector2f original = uvs[i];

            float dx = original.x - centerX;
            float dy = original.y - centerY;

            float rx = dx * cos - dy * sin;
            float ry = dx * sin + dy * cos;

            rotatedUVs[i] = new Vector2f(centerX + rx, centerY + ry);
        }

        for (Vector2f rotatedUV : rotatedUVs)
        {
            rotatedUV.x = Math.min(Math.max(rotatedUV.x, 0.0f), 1.0f);
            rotatedUV.y = Math.min(Math.max(rotatedUV.y, 0.0f), 1.0f);
        }

        return rotatedUVs;
    }

    public @NotNull Texture createSubTexture(@NotNull String subTextureName)
    {
        Vector2f[] uvs = subTextureMap.get(subTextureName);

        if (uvs == null)
            throw new IllegalArgumentException("Subtexture not found: " + subTextureName);

        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE;
        float maxU = Float.MIN_VALUE, maxV = Float.MIN_VALUE;

        for (Vector2f uv : uvs)
        {
            if (uv.x < minU)
                minU = uv.x;

            if (uv.y < minV)
                minV = uv.y;

            if (uv.x > maxU)
                maxU = uv.x;

            if (uv.y > maxV)
                maxV = uv.y;
        }

        int subX = (int) (minU * atlasSize);
        int subY = (int) (minV * atlasSize);

        int subWidth = (int) ((maxU - minU) * atlasSize);
        int subHeight = (int) ((maxV - minV) * atlasSize);

        if (atlasBuffer == null)
            throw new IllegalStateException("Atlas buffer not available.");

        ByteBuffer subTextureBuffer = ByteBuffer.allocateDirect(subWidth * subHeight * 4).order(ByteOrder.nativeOrder());

        for (int row = 0; row < subHeight; row++)
        {
            int atlasRowStart = ((subY + (subHeight - 1 - row)) * atlasSize + subX) * 4;

            byte[] rowData = new byte[subWidth * 4];
            atlasBuffer.position(atlasRowStart);
            atlasBuffer.get(rowData, 0, subWidth * 4);

            subTextureBuffer.put(rowData);
        }

        subTextureBuffer.flip();

        return Texture.create(subTextureName, Texture.Filter.NEAREST, Texture.Wrapping.CLAMP_TO_EDGE, subWidth, subHeight, subTextureBuffer);
    }

    public @NotNull AssetPath getLocalDirectory()
    {
        return localDirectory;
    }

    public void setLocalDirectory(@NotNull AssetPath localDirectory)
    {
        this.localDirectory = localDirectory;
        this.directory = localDirectory.getFullPath();
    }

    public @NotNull String getDirectory()
    {
        return directory;
    }

    public void setDirectory(@NotNull String directory)
    {
        this.directory = directory;
    }

    private ByteBuffer createAtlasBuffer(List<ImageData> images, int atlasSize)
    {
        ByteBuffer atlasBuffer = ByteBuffer.allocateDirect(atlasSize * atlasSize * 4);
        atlasBuffer.order(ByteOrder.nativeOrder());

        for (int i = 0; i < atlasSize * atlasSize * 4; i++)
            atlasBuffer.put((byte)0x00);

        atlasBuffer.flip();

        int currentX = 0;
        int currentY = 0;
        int rowHeight = 0;

        atlasBuffer = atlasBuffer.duplicate();

        for (ImageData img : images)
        {
            if (currentX + img.paddedWidth > atlasSize)
            {
                currentX = 0;
                currentY += rowHeight;
                rowHeight = 0;
            }

            if (currentY + img.paddedHeight > atlasSize)
            {
                System.err.println("Cannot fit image: " + img.name + " in atlas.");
                return null;
            }

            for (int row = 0; row < img.paddedHeight; row++)
            {
                int srcPos = row * img.paddedWidth * 4;
                int dstPos = ((currentY + row) * atlasSize + currentX) * 4;

                atlasBuffer.position(dstPos);
                atlasBuffer.put(img.pixels, srcPos, img.paddedWidth * 4);
            }

            float u0 = (float)currentX / (float)atlasSize;
            float v0 = (float)currentY / (float)atlasSize;
            float u1 = (float)(currentX + img.paddedWidth) / (float)atlasSize;
            float v1 = (float)(currentY + img.paddedHeight) / (float)atlasSize;

            float invAtlasSize = 1.0f / (float)atlasSize;
            float padded = PADDING * invAtlasSize;

            Vector2f[] uvs = new Vector2f[]
            {
                new Vector2f(u0 + padded, v0 + padded),
                new Vector2f(u0 + padded, v1 - padded),
                new Vector2f(u1 - padded, v1 - padded),
                new Vector2f(u1 - padded, v0 + padded)
            };

            subTextureMap.put(img.name.replace(".png", ""), uvs);

            currentX += img.paddedWidth;

            if (img.paddedHeight > rowHeight)
                rowHeight = img.paddedHeight;
        }

        atlasBuffer.position(0);
        return atlasBuffer;
    }


    private List<String> listImageFiles(String directoryPath)
    {
        List<String> result;

        try (ScanResult scanResult = new ClassGraph().acceptPaths(directoryPath).scan())
        {
            result = scanResult.getAllResources().getPaths();
        }

        result.replaceAll(path -> "/" + path);

        return result;
    }

    private ImageData loadImageData(String resourcePath)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);

            ByteBuffer rawImage = loadResourceAsByteBuffer(resourcePath);

            if (rawImage == null)
                return null;

            ByteBuffer image = STBImage.stbi_load_from_memory(rawImage, widthBuffer, heightBuffer, channelsBuffer, 4);

            if (image == null)
            {
                System.err.println("Failed to load image: " + resourcePath + " Reason: " + STBImage.stbi_failure_reason());
                return null;
            }

            int w = widthBuffer.get(0);
            int h = heightBuffer.get(0);

            byte[] pixels = new byte[w * h * 4];
            image.get(pixels);

            //STBImage.stbi_image_free(image); // Keep as is if freeing causes issues

            String fileName = new File(resourcePath).getName();

            ImageData data = new ImageData();

            data.name = fileName;
            data.originalWidth = w;
            data.originalHeight = h;
            data.paddedWidth = w + 2 * PADDING;
            data.paddedHeight = h + 2 * PADDING;
            data.pixels = createPaddedImage(w, h, data.paddedWidth, data.paddedHeight, pixels);

            return data;
        }
    }

    private byte[] createPaddedImage(int w, int h, int paddedW, int paddedH, byte[] originalPixels)
    {
        byte[] paddedPixels = new byte[paddedW * paddedH * 4];

        Arrays.fill(paddedPixels, (byte)0x00);

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int srcIndex = (y * w + x) * 4;
                int dstIndex = ((y + PADDING) * paddedW + (x + PADDING)) * 4;

                System.arraycopy(originalPixels, srcIndex, paddedPixels, dstIndex, 4);
            }
        }

        for (int x = 0; x < w; x++)
        {
            for (int p = 0; p < PADDING; p++)
            {
                int srcIndex = ((PADDING) * paddedW + (x + PADDING)) * 4;
                int dstIndex = (p * paddedW + (x + PADDING)) * 4;
                System.arraycopy(paddedPixels, srcIndex, paddedPixels, dstIndex, 4);

                srcIndex = ((PADDING + h - 1) * paddedW + (x + PADDING)) * 4;
                dstIndex = ((paddedH - PADDING + p) * paddedW + (x + PADDING)) * 4;
                System.arraycopy(paddedPixels, srcIndex, paddedPixels, dstIndex, 4);
            }
        }

        for (int y = 0; y < paddedH; y++)
        {
            for (int p = 0; p < PADDING; p++)
            {
                int srcIndex = (y * paddedW + PADDING) * 4;
                int dstIndex = (y * paddedW + p) * 4;
                System.arraycopy(paddedPixels, srcIndex, paddedPixels, dstIndex, 4);

                srcIndex = (y * paddedW + PADDING + w - 1) * 4;
                dstIndex = (y * paddedW + paddedW - PADDING + p) * 4;
                System.arraycopy(paddedPixels, srcIndex, paddedPixels, dstIndex, 4);
            }
        }

        return paddedPixels;
    }

    private ByteBuffer loadResourceAsByteBuffer(@NotNull String resourcePath)
    {
        try (InputStream stream = Texture.class.getResourceAsStream(resourcePath))
        {
            if (stream == null)
            {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }

            byte[] bytes = stream.readAllBytes();

            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            buffer.put(bytes);
            buffer.flip();

            return buffer;
        }
        catch (Exception exception)
        {
            System.err.println("Failed to load resource: " + exception.getMessage());
        }

        return null;
    }

    @Override
    public @NotNull Class<?> getManagingClass()
    {
        return TextureAtlasManager.class;
    }

    @Override
    public @NotNull String getManagedItem()
    {
        return name;
    }

    public void uninitialize_NoOverride()
    {
        if (outputTexture != null)
            outputTexture.uninitialize_NoOverride();
    }

    public static @NotNull TextureAtlas create(@NotNull String name, @NotNull AssetPath localPath)
    {
        TextureAtlas result = new TextureAtlas();

        result.setName(name);
        result.setLocalDirectory(localPath);

        result.generate();

        return result;
    }

    private static class ImageData
    {
        String name;
        int originalWidth;
        int originalHeight;
        int paddedWidth;
        int paddedHeight;
        byte[] pixels;
    }
}