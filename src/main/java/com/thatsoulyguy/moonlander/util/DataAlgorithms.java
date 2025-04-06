package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.DefaultVertex;
import com.thatsoulyguy.moonlander.world.Chunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
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

    /**
     * Creates an extruded mesh from the non-transparent pixels of the given texture.
     *
     * @param texture the Texture to use (must allow access to its pixel data)
     * @param extrusionDepth the distance to extrude (e.g. 0.1f)
     * @return a new Mesh with front, back, and side faces
     */
    public static @NotNull Pair<@NotNull List<DefaultVertex>, @NotNull List<Integer>> extrudeTextureIntoMeshData(@NotNull Texture texture, float extrusionDepth)
    {
        ByteBuffer pixelData = texture.getBuffer().duplicate().duplicate().order(ByteOrder.nativeOrder());

        pixelData.rewind();

        assert texture.getDimensions() != null;

        int width = texture.getDimensions().x;
        int height = texture.getDimensions().y;

        boolean[][] mask = new boolean[height][width];

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int index = (y * width + x) * 4;
                int alpha = pixelData.get(index + 3) & 0xFF;

                mask[y][x] = (alpha > 0);
            }
        }

        List<Vector2f> contour = extractContour(mask, width, height);

        if (contour.isEmpty())
            throw new IllegalStateException("No non-transparent pixels found in texture.");

        List<Integer> frontIndices = triangulatePolygon(contour);

        List<DefaultVertex> vertices = new ArrayList<>();

        for (Vector2f point : contour)
        {
            Vector3f position = new Vector3f(point.x, point.y, 0.0f);

            Vector2f uv = new Vector2f(point);

            Vector3f normal = new Vector3f(0.0f, 0.0f, 0.0f);

            vertices.add(DefaultVertex.create(position, new Vector3f(1.0f), normal, uv));
        }

        int frontCount = vertices.size();

        for (int i = 0; i < frontCount; i++)
        {
            DefaultVertex frontVertex = vertices.get(i);
            Vector3f pos = new Vector3f(frontVertex.getPosition());

            pos.z += extrusionDepth;

            Vector3f normal = new Vector3f(0.0f, 0.0f, 1.0f);
            vertices.add(DefaultVertex.create(pos, new Vector3f(1.0f), normal, frontVertex.getUVs()));
        }

        List<Integer> indices = getExtrudedIndices(frontIndices, frontCount, contour);

        return new Pair<>(vertices, indices);
    }

    private static @NotNull List<Integer> getExtrudedIndices(List<Integer> frontIndices, int frontCount, List<Vector2f> contour)
    {
        List<Integer> indices = new ArrayList<>(frontIndices);

        for (int i = 0; i < frontIndices.size(); i += 3)
        {
            int i0 = frontIndices.get(i) + frontCount;
            int i1 = frontIndices.get(i + 1) + frontCount;
            int i2 = frontIndices.get(i + 2) + frontCount;

            indices.add(i0);
            indices.add(i2);
            indices.add(i1);
        }

        int contourSize = contour.size();

        for (int i = 0; i < contourSize; i++)
        {
            int next = (i + 1) % contourSize;
            int backCurrent = i + frontCount;
            int backNext = next + frontCount;

            indices.add(i);
            indices.add(next);
            indices.add(backCurrent);

            indices.add(next);
            indices.add(backNext);
            indices.add(backCurrent);
        }

        return indices;
    }

    private static List<Vector2f> extractContour(boolean[][] mask, int width, int height)
    {
        int[] dx = {-1, -1,  0, 1, 1,  1,  0, -1};
        int[] dy = { 0,  1,  1, 1, 0, -1, -1, -1};

        int startX = -1, startY = -1;

        outer:

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                if (mask[y][x])
                {
                    boolean isBoundary = false;

                    for (int j = -1; j <= 1 && !isBoundary; j++)
                    {
                        for (int i = -1; i <= 1; i++)
                        {
                            int nx = x + i, ny = y + j;

                            if (nx < 0 || ny < 0 || nx >= width || ny >= height || !mask[ny][nx])
                            {
                                isBoundary = true;
                                break;
                            }
                        }
                    }

                    if (isBoundary)
                    {
                        startX = x;
                        startY = y;

                        break outer;
                    }
                }
            }
        }
        if (startX == -1)
            return new ArrayList<>();

        List<Vector2f> contour = new ArrayList<>();

        int currentX = startX, currentY = startY;
        int prevDirection = 7;

        contour.add(new Vector2f((float) currentX / (width - 1), (float) currentY / (height - 1)));

        while (true)
        {
            for (int i = 0; i < 8; i++)
            {
                int dir = (prevDirection + 1 + i) % 8;
                int nx = currentX + dx[dir];
                int ny = currentY + dy[dir];

                if (nx < 0 || ny < 0 || nx >= width || ny >= height)
                    continue;

                if (mask[ny][nx])
                {
                    currentX = nx;
                    currentY = ny;

                    prevDirection = (dir + 6) % 8;

                    break;
                }
            }

            if (currentX == startX && currentY == startY)
                break;

            contour.add(new Vector2f((float) currentX / (width - 1), (float) currentY / (height - 1)));
        }

        return contour;
    }

    private static @NotNull List<Integer> triangulatePolygon(@NotNull List<Vector2f> contour)
    {
        int n = contour.size();

        if (n < 3)
            return new ArrayList<>();

        List<Integer> polygon = new ArrayList<>();

        for (int i = 0; i < n; i++)
            polygon.add(i);

        List<Integer> triangles = new ArrayList<>();

        if (computeArea(contour, polygon) < 0)
            Collections.reverse(polygon);

        int count = 0;

        while (polygon.size() > 3 && count < 10000)
        {
            boolean earFound = false;
            int m = polygon.size();

            for (int i = 0; i < m; i++)
            {
                int prevIndex = polygon.get((i + m - 1) % m);
                int currIndex = polygon.get(i);
                int nextIndex = polygon.get((i + 1) % m);

                Vector2f a = contour.get(prevIndex);
                Vector2f b = contour.get(currIndex);
                Vector2f c = contour.get(nextIndex);

                if (isConvex(a, b, c))
                {
                    boolean hasPointInside = false;

                    for (int j = 0; j < m; j++)
                    {
                        if (j == (i + m - 1) % m || j == i || j == (i + 1) % m)
                            continue;

                        Vector2f p = contour.get(polygon.get(j));

                        if (pointInTriangle(p, a, b, c))
                        {
                            hasPointInside = true;
                            break;
                        }
                    }

                    if (!hasPointInside)
                    {
                        triangles.add(prevIndex);
                        triangles.add(currIndex);
                        triangles.add(nextIndex);

                        polygon.remove(i);

                        earFound = true;

                        break;
                    }
                }
            }

            if (!earFound)
                break;

            count++;
        }
        if (polygon.size() == 3)
        {
            triangles.add(polygon.get(0));
            triangles.add(polygon.get(1));
            triangles.add(polygon.get(2));
        }

        return triangles;
    }

    private static float computeArea(List<Vector2f> contour, List<Integer> polygon)
    {
        float area = 0;
        int m = polygon.size();

        for (int i = 0; i < m; i++)
        {
            Vector2f p = contour.get(polygon.get(i));
            Vector2f q = contour.get(polygon.get((i + 1) % m));

            area += p.x * q.y - q.x * p.y;
        }

        return area * 0.5f;
    }

    private static boolean isConvex(Vector2f a, Vector2f b, Vector2f c)
    {
        float cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);

        return cross > 0;
    }

    private static boolean pointInTriangle(Vector2f p, Vector2f a, Vector2f b, Vector2f c)
    {
        float denom = (b.y - c.y) * (a.x - c.x) + (c.x - b.x) * (a.y - c.y);
        float alpha = ((b.y - c.y) * (p.x - c.x) + (c.x - b.x) * (p.y - c.y)) / denom;
        float beta  = ((c.y - a.y) * (p.x - c.x) + (a.x - c.x) * (p.y - c.y)) / denom;
        float gamma = 1.0f - alpha - beta;

        return (alpha > 0 && beta > 0 && gamma > 0);
    }
}