package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.world.Chunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Static
public class DataAlgorithms
{
    private DataAlgorithms() { }

    public static float[] getAmbientOcclusionLighting(@NotNull Vector3i position, @NotNull Vector3i normal, short[][][] blocks)
    {
        float[] lighting = new float[4];

        if (normal.x == 0 && normal.y == 0 && normal.z > 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position, -1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, 1, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1, 1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position, -1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, 1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1, 1, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1, 1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position,  1,  0, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, 1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1, 1, blocks);
        }
        else if (normal.x == 0 && normal.y == 0 && normal.z < 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1, -1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1, -1, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position, -1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1, -1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position, -1,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1, -1, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1, -1, blocks);
        }
        else if (normal.x == 0 && normal.y > 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  0,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  0,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  0,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  0, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  0, blocks);
        }
        else if (normal.x == 0 && normal.y < 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  0, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  1, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  0, -1,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  0, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position, -1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position, -1, -1, -1, blocks)
                    * ambientOcclusionAlgorithm(position,  0, -1,  0, blocks);
        }
        else if (normal.x > 0 && normal.y == 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  1,  0,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  0, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  1,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  1,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position,  1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1, -1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  1,  0,  0, blocks);
        }
        else if (normal.x < 0 && normal.y == 0 && normal.z == 0)
        {
            lighting[0] = ambientOcclusionAlgorithm(position,  0,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1, -1, blocks);

            lighting[1] = ambientOcclusionAlgorithm(position,  0,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  1,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  0, blocks);

            lighting[2] = ambientOcclusionAlgorithm(position,  0,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  0,  1, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  1,  1, blocks);

            lighting[3] = ambientOcclusionAlgorithm(position, -1,  0,  0, blocks)
                    * ambientOcclusionAlgorithm(position,  0,  0, -1, blocks)
                    * ambientOcclusionAlgorithm(position, -1,  0, -1, blocks);
        }
        else
            lighting[0] = lighting[1] = lighting[2] = lighting[3] = 1.0f;

        return lighting;
    }

    public static @NotNull Map<String, Vector2f[]> generateCubeUVs(@NotNull Vector3f cubeSize, @NotNull Vector2i textureSize, @NotNull Vector2i uvOrigin)
    {
        float x = cubeSize.x;
        float y = cubeSize.y;
        float z = cubeSize.z;

        float texW = textureSize.x;
        float texH = textureSize.y;

        BiFunction<Float, Float, Vector2f> uv = (px, py) -> new Vector2f(px / texW, py / texH);

        float topOx = uvOrigin.x + z;
        float topOy = uvOrigin.y;

        Vector2f[] topFace = new Vector2f[]
        {
            uv.apply(topOx, topOy),
            uv.apply(topOx + x, topOy),
            uv.apply(topOx + x, topOy + z),
            uv.apply(topOx, topOy + z)
        };

        float botOx = uvOrigin.x + z + x;
        float botOy = uvOrigin.y;

        Vector2f[] bottomFace = new Vector2f[]
        {
            uv.apply(botOx, botOy),
            uv.apply(botOx + x, botOy),
            uv.apply(botOx + x, botOy + z),
            uv.apply(botOx, botOy + z)
        };

        float rightOx = uvOrigin.x;
        float rightOy = uvOrigin.y + z;

        Vector2f[] rightFace = new Vector2f[]
        {
            uv.apply(rightOx, rightOy),
            uv.apply(rightOx + z, rightOy),
            uv.apply(rightOx + z, rightOy + y),
            uv.apply(rightOx, rightOy + y)
        };

        float frontOx = uvOrigin.x + z;
        float frontOy = uvOrigin.y + z;

        Vector2f[] frontFace = new Vector2f[]
        {
            uv.apply(frontOx, frontOy),
            uv.apply(frontOx + x, frontOy),
            uv.apply(frontOx + x, frontOy + y),
            uv.apply(frontOx, frontOy + y)
        };

        float leftOx = uvOrigin.x + z + x;
        float leftOy = uvOrigin.y + z;

        Vector2f[] leftFace = new Vector2f[]
        {
            uv.apply(leftOx, leftOy),
            uv.apply(leftOx + z, leftOy),
            uv.apply(leftOx + z, leftOy + y),
            uv.apply(leftOx, leftOy + y)
        };

        float backOx = uvOrigin.x + z + x + z;
        float backOy = uvOrigin.y + z;

        Vector2f[] backFace = new Vector2f[]
        {
            uv.apply(backOx, backOy),
            uv.apply(backOx + x, backOy),
            uv.apply(backOx + x, backOy + y),
            uv.apply(backOx, backOy + y)
        };

        LinkedHashMap<String, Vector2f[]> uvMap = new LinkedHashMap<>();

        uvMap.put("top", topFace);
        uvMap.put("bottom", bottomFace);
        uvMap.put("right", rightFace);
        uvMap.put("front", frontFace);
        uvMap.put("left", leftFace);
        uvMap.put("back", backFace);

        return uvMap;
    }

    public static @NotNull Map<String, Vector3f[]> generateCubeVertices(@NotNull Vector3f size)
    {
        float x1 = 0.0f, x2 = size.x;
        float y1 = 0.0f, y2 = size.y;
        float z1 = 0.0f, z2 = size.z;

        Vector3f v0 = new Vector3f(x1, y1, z1);
        Vector3f v1 = new Vector3f(x2, y1, z1);
        Vector3f v2 = new Vector3f(x2, y2, z1);
        Vector3f v3 = new Vector3f(x1, y2, z1);
        Vector3f v4 = new Vector3f(x1, y1, z2);
        Vector3f v5 = new Vector3f(x2, y1, z2);
        Vector3f v6 = new Vector3f(x2, y2, z2);
        Vector3f v7 = new Vector3f(x1, y2, z2);

        Map<String, Vector3f[]> faces = new LinkedHashMap<>();

        faces.put("front", new Vector3f[]
        {
            new Vector3f(v4),
            new Vector3f(v5),
            new Vector3f(v6),
            new Vector3f(v7)
        });

        faces.put("back", new Vector3f[]
        {
            new Vector3f(v1),
            new Vector3f(v0),
            new Vector3f(v3),
            new Vector3f(v2)
        });

        faces.put("left", new Vector3f[]
        {
            new Vector3f(v0),
            new Vector3f(v4),
            new Vector3f(v7),
            new Vector3f(v3)
        });

        faces.put("right", new Vector3f[]
        {
            new Vector3f(v5),
            new Vector3f(v1),
            new Vector3f(v2),
            new Vector3f(v6)
        });

        faces.put("top", new Vector3f[]
        {
            new Vector3f(v3),
            new Vector3f(v7),
            new Vector3f(v6),
            new Vector3f(v2)
        });

        faces.put("bottom", new Vector3f[]
        {
            new Vector3f(v0),
            new Vector3f(v1),
            new Vector3f(v5),
            new Vector3f(v4)
        });

        return faces;
    }

    private static float ambientOcclusionAlgorithm(@NotNull Vector3i base, int dx, int dy, int dz, short[][][] blocks)
    {
        Vector3i neighbor = new Vector3i(base.x + dx, base.y + dy, base.z + dz);

        return isBlockSolid(neighbor, blocks) ? 0.75f : 1.0f;
    }

    private static boolean isBlockSolid(@NotNull Vector3i position, short[][][] blocks)
    {
        if (position.x < 0 || position.x >= Chunk.SIZE ||
                position.y < 0 || position.y >= Chunk.SIZE ||
                position.z < 0 || position.z >= Chunk.SIZE)
            return false;

        return blocks[position.x][position.y][position.z] != BlockRegistry.BLOCK_AIR.getId();
    }
}