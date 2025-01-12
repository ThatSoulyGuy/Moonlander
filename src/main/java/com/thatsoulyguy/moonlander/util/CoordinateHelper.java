package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.world.Chunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * A class for coordinate conversions
 * <p>
 * Annotates: {@code @Static}
 */
@Static
public class CoordinateHelper
{
    private CoordinateHelper() { }

    /**
     * Convert world coordinates (float) to chunk coordinates (int).
     *
     * @param worldCoordinates The world position (float precision).
     * @return The chunk coordinates that contain the given world position.
     */
    public static @NotNull Vector3i worldToChunkCoordinates(@NotNull Vector3f worldCoordinates)
    {
        int x = (int) Math.floor(worldCoordinates.x / Chunk.SIZE);
        int y = (int) Math.floor(worldCoordinates.y / Chunk.SIZE);
        int z = (int) Math.floor(worldCoordinates.z / Chunk.SIZE);

        return new Vector3i(x, y, z);
    }

    /**
     * Convert chunk coordinates (int) to the corresponding world position (float).
     * This gives the world-space origin of that chunk (the corner with minimal coordinates).
     *
     * @param chunkCoordinates The chunk coordinates.
     * @return The world-space position of the chunk’s origin.
     */
    public static @NotNull Vector3f chunkToWorldCoordinates(@NotNull Vector3i chunkCoordinates)
    {
        float x = chunkCoordinates.x * (float) Chunk.SIZE;
        float y = chunkCoordinates.y * (float) Chunk.SIZE;
        float z = chunkCoordinates.z * (float) Chunk.SIZE;

        return new Vector3f(x, y, z);
    }

    /**
     * Convert world coordinates (float) to global block coordinates (int).
     * The global block coordinates identify which block in the local chunk grid
     * the world position falls into. This depends on chunk coordinates and
     * returns a continuous local indexing of blocks.
     *
     * @param worldCoordinates The world position (float precision).
     * @return The local block coordinates as integers.
     */
    public static @NotNull Vector3i worldToBlockCoordinates(@NotNull Vector3f worldCoordinates)
    {
        int x = (int) Math.floor(worldCoordinates.x);
        int y = (int) Math.floor(worldCoordinates.y);
        int z = (int) Math.floor(worldCoordinates.z);

        x = mod(x, Chunk.SIZE);
        y = mod(y, Chunk.SIZE);
        z = mod(z, Chunk.SIZE);

        return new Vector3i(x, y, z);
    }

    /**
     * Convert world coordinates (float) to global block coordinates (int).
     * The global block coordinates identify which block in the entire world grid
     * the world position falls into. This does not depend on chunk coordinates and
     * returns a continuous global indexing of blocks.
     *
     * @param worldCoordinates The world position (float precision).
     * @return The global block coordinates as integers.
     */
    public static @NotNull Vector3i worldToGlobalBlockCoordinates(@NotNull Vector3f worldCoordinates)
    {
        int x = (int) Math.floor(worldCoordinates.x);
        int y = (int) Math.floor(worldCoordinates.y);
        int z = (int) Math.floor(worldCoordinates.z);

        return new Vector3i(x, y, z);
    }

    /**
     * Convert local block coordinates within a given chunk to world coordinates.
     * Given the chunk index and the block index within that chunk,
     * find the world position of the block’s "lowest" corner (integral position).
     * <p>
     * If you want the center of the block, you could add 0.5f to each coordinate after calculation.
     *
     * @param blockCoordinates The block coordinates within the chunk
     *                         (range typically from 0 to Chunk.SIZE-1 in each dimension).
     * @param chunkCoordinates The chunk coordinates in which the block resides.
     * @return The corresponding world coordinates (float).
     */
    public static @NotNull Vector3f blockToWorldCoordinates(@NotNull Vector3i blockCoordinates, @NotNull Vector3i chunkCoordinates)
    {
        float x = (chunkCoordinates.x * (float) Chunk.SIZE) + blockCoordinates.x;
        float y = (chunkCoordinates.y * (float) Chunk.SIZE) + blockCoordinates.y;
        float z = (chunkCoordinates.z * (float) Chunk.SIZE) + blockCoordinates.z;

        return new Vector3f(x, y, z);
    }

    /**
     * Convert global block coordinates to world coordinates (float).
     * This assumes that the global block coordinates are already continuous across the grid.
     *
     * @param globalBlockCoordinates The global block coordinates.
     * @return The corresponding world coordinates (float).
     */
    public static @NotNull Vector3f globalBlockToWorldCoordinates(@NotNull Vector3i globalBlockCoordinates)
    {
        float x = (float) Math.ceil(globalBlockCoordinates.x);
        float y = (float) Math.ceil(globalBlockCoordinates.y);
        float z = (float) Math.ceil(globalBlockCoordinates.z);

        return new Vector3f(x, y, z);
    }

    private static int mod(int a, int n)
    {
        int result = a % n;

        if (result < 0)
            result += n;

        return result;
    }
}