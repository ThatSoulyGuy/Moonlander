package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.util.CoordinateHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

@CustomConstructor("create")
public class Chunk extends Component
{
    public static final int SIZE = 16;

    private transient List<ChunkVertex> vertices = new ArrayList<>();
    private transient List<Integer> indices = new ArrayList<>();

    private transient List<Vector3f> renderingVoxelPositions;

    private long[] blockData;
    private List<Short> palette;
    private int bitsPerBlock;

    private boolean modified = false;
    private boolean needsToUpdate = false;

    private Chunk() { }

    @Override
    public void onLoad()
    {
        generate();
    }

    public void generate()
    {
        vertices = new ArrayList<>();
        indices = new ArrayList<>();
        renderingVoxelPositions = new ArrayList<>();

        TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);

        if (textureAtlas == null)
        {
            System.err.println("Texture atlas was not found on chunk object!");
            return;
        }

        greedyMesh(textureAtlas);

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    if (getBlock(x, y, z) == BlockRegistry.BLOCK_AIR.getId())
                        continue;

                    if (Objects.requireNonNull(BlockRegistry.get(getBlock(x, y, z))).updates())
                        needsToUpdate = true;

                    if (isBlockExposed(x, y, z))
                        addRenderingVoxelPosition(x, y, z);
                }
            }
        }

        Mesh mesh = getGameObject().getComponent(Mesh.class);
        VoxelMeshCollider collider = getGameObject().getComponent(VoxelMeshCollider.class);

        if (mesh == null)
        {
            System.err.println("Mesh component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (collider == null)
        {
            System.err.println("VoxelMeshCollider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        mesh.setTransient(true);
        collider.setTransient(true);

        if (!vertices.isEmpty() && !indices.isEmpty())
        {
            mesh.modify(vertList ->
            {
                vertList.clear();
                vertList.addAll(vertices);
            }, idxList ->
            {
                idxList.clear();
                idxList.addAll(indices);
            });

            collider.setVoxels(renderingVoxelPositions);
        }
    }

    @Override
    public void update()
    {
        if (!needsToUpdate)
            return;

        Vector3f chunkWorldPos = getGameObject().getTransform().getWorldPosition();
        int totalBlocks = SIZE * SIZE * SIZE;

        for (int index = 0; index < totalBlocks; index++)
        {
            Vector3i localPos = fromIndex(index);

            short blockId = getBlock(localPos.x, localPos.y, localPos.z);

            if (blockId == BlockRegistry.BLOCK_AIR.getId())
                continue;

            Block block = BlockRegistry.get(blockId);

            if (block != null && block.updates())
            {
                Vector3i globalBlockPos = new Vector3i(
                        (int) (chunkWorldPos.x) + localPos.x,
                        (int) (chunkWorldPos.y) + localPos.y,
                        (int) (chunkWorldPos.z) + localPos.z
                );

                block.onTick(World.getLocalWorld(), this, globalBlockPos);
            }
        }
    }

    private void greedyMesh(@NotNull TextureAtlas textureAtlas)
    {
        greedyMeshPosZ(textureAtlas);
        greedyMeshNegZ(textureAtlas);
        greedyMeshPosY(textureAtlas);
        greedyMeshNegY(textureAtlas);
        greedyMeshPosX(textureAtlas);
        greedyMeshNegX(textureAtlas);
    }

    private void greedyMeshPosZ(@NotNull TextureAtlas textureAtlas)
    {
        int airId = BlockRegistry.BLOCK_AIR.getId();

        for (int z = 0; z < SIZE; z++)
        {
            int[][] mask = new int[SIZE][SIZE];

            for (int x = 0; x < SIZE; x++)
            {
                for (int y = 0; y < SIZE; y++)
                {
                    short blockId = getBlock(x, y, z);
                    short neighbor = getNeighborBlock(x, y, z, new Vector3i(0, 0, 1));

                    if (blockId != airId && neighbor != -1 && (!BlockRegistry.get(neighbor).isSolid() || neighbor == airId))
                        mask[x][y] = blockId;
                    else
                        mask[x][y] = -1;
                }
            }

            boolean[][] visited = new boolean[SIZE][SIZE];

            for (int y = 0; y < SIZE; y++)
            {
                for (int x = 0; x < SIZE; x++)
                {
                    if (visited[x][y] || mask[x][y] == -1)
                        continue;

                    int id = mask[x][y];
                    int w = 1;

                    while (x + w < SIZE && mask[x + w][y] == id && !visited[x + w][y])
                        w++;

                    int h = 1;

                    boolean done = false;

                    while (y + h < SIZE && !done)
                    {
                        for (int k = 0; k < w; k++)
                        {
                            if (mask[x + k][y + h] != id || visited[x + k][y + h])
                            {
                                done = true;
                                break;
                            }
                        }

                        if (!done)
                            h++;
                    }

                    for (int dy = 0; dy < h; dy++)
                        for (int dx = 0; dx < w; dx++)
                            visited[x + dx][y + dy] = true;

                    addMergedFacePosZ(x, y, z, w, h, (short) id, textureAtlas);
                }
            }
        }
    }

    private void greedyMeshNegZ(@NotNull TextureAtlas textureAtlas)
    {
        int airId = BlockRegistry.BLOCK_AIR.getId();

        for (int z = 0; z < SIZE; z++)
        {
            int[][] mask = new int[SIZE][SIZE];

            for (int x = 0; x < SIZE; x++)
            {
                for (int y = 0; y < SIZE; y++)
                {
                    short blockId = getBlock(x, y, z);
                    short neighbor = getNeighborBlock(x, y, z, new Vector3i(0, 0, -1));

                    if (blockId != airId && neighbor != -1 && (!BlockRegistry.get(neighbor).isSolid() || neighbor == airId))
                        mask[x][y] = blockId;
                    else
                        mask[x][y] = -1;
                }
            }

            boolean[][] visited = new boolean[SIZE][SIZE];

            for (int y = 0; y < SIZE; y++)
            {
                for (int x = 0; x < SIZE; x++)
                {
                    if (visited[x][y] || mask[x][y] == -1)
                        continue;

                    int id = mask[x][y];
                    int w = 1;

                    while (x + w < SIZE && mask[x + w][y] == id && !visited[x + w][y])
                        w++;

                    int h = 1;
                    boolean done = false;

                    while (y + h < SIZE && !done)
                    {
                        for (int k = 0; k < w; k++)
                        {
                            if (mask[x + k][y + h] != id || visited[x + k][y + h])
                            {
                                done = true;
                                break;
                            }
                        }

                        if (!done)
                            h++;
                    }

                    for (int dy = 0; dy < h; dy++)
                        for (int dx = 0; dx < w; dx++)
                            visited[x + dx][y + dy] = true;

                    addMergedFaceNegZ(x, y, z, w, h, (short) id, textureAtlas);
                }
            }
        }
    }

    private void greedyMeshPosY(@NotNull TextureAtlas textureAtlas)
    {
        int airId = BlockRegistry.BLOCK_AIR.getId();

        for (int y = 0; y < SIZE; y++)
        {
            int[][] mask = new int[SIZE][SIZE];

            for (int x = 0; x < SIZE; x++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockId = getBlock(x, y, z);
                    short neighbor = getNeighborBlock(x, y, z, new Vector3i(0, 1, 0));

                    if (blockId != airId && neighbor != -1 && (!BlockRegistry.get(neighbor).isSolid() || neighbor == airId))
                        mask[x][z] = blockId;
                    else
                        mask[x][z] = -1;
                }
            }

            boolean[][] visited = new boolean[SIZE][SIZE];

            for (int z = 0; z < SIZE; z++)
            {
                for (int x = 0; x < SIZE; x++)
                {
                    if (visited[x][z] || mask[x][z] == -1)
                        continue;

                    int id = mask[x][z];
                    int w = 1;

                    while (x + w < SIZE && mask[x + w][z] == id && !visited[x + w][z])
                        w++;

                    int h = 1;
                    boolean done = false;

                    while (z + h < SIZE && !done)
                    {
                        for (int k = 0; k < w; k++)
                        {
                            if (mask[x + k][z + h] != id || visited[x + k][z + h])
                            {
                                done = true;
                                break;
                            }
                        }

                        if (!done)
                            h++;
                    }

                    for (int dz = 0; dz < h; dz++)
                        for (int dx = 0; dx < w; dx++)
                            visited[x + dx][z + dz] = true;

                    addMergedFacePosY(x, y, z, w, h, (short) id, textureAtlas);
                }
            }
        }
    }

    private void greedyMeshNegY(@NotNull TextureAtlas textureAtlas)
    {
        int airId = BlockRegistry.BLOCK_AIR.getId();

        for (int y = 0; y < SIZE; y++)
        {
            int[][] mask = new int[SIZE][SIZE];
            for (int x = 0; x < SIZE; x++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockId = getBlock(x, y, z);
                    short neighbor = getNeighborBlock(x, y, z, new Vector3i(0, -1, 0));

                    if (blockId != airId && neighbor != -1 && (!BlockRegistry.get(neighbor).isSolid() || neighbor == airId))
                        mask[x][z] = blockId;
                    else
                        mask[x][z] = -1;
                }
            }

            boolean[][] visited = new boolean[SIZE][SIZE];

            for (int z = 0; z < SIZE; z++)
            {
                for (int x = 0; x < SIZE; x++)
                {
                    if (visited[x][z] || mask[x][z] == -1)
                        continue;

                    int id = mask[x][z];
                    int w = 1;

                    while (x + w < SIZE && mask[x + w][z] == id && !visited[x + w][z])
                        w++;

                    int h = 1;
                    boolean done = false;

                    while (z + h < SIZE && !done)
                    {
                        for (int k = 0; k < w; k++)
                        {
                            if (mask[x + k][z + h] != id || visited[x + k][z + h])
                            {
                                done = true;
                                break;
                            }
                        }

                        if (!done)
                            h++;
                    }

                    for (int dz = 0; dz < h; dz++)
                        for (int dx = 0; dx < w; dx++)
                            visited[x + dx][z + dz] = true;

                    addMergedFaceNegY(x, y, z, w, h, (short) id, textureAtlas);
                }
            }
        }
    }

    private void greedyMeshPosX(@NotNull TextureAtlas textureAtlas)
    {
        int airId = BlockRegistry.BLOCK_AIR.getId();

        for (int x = 0; x < SIZE; x++)
        {
            int[][] mask = new int[SIZE][SIZE];

            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockId = getBlock(x, y, z);
                    short neighbor = getNeighborBlock(x, y, z, new Vector3i(1, 0, 0));

                    if (blockId != airId && neighbor != -1 && (!BlockRegistry.get(neighbor).isSolid() || neighbor == airId))
                        mask[y][z] = blockId;
                    else
                        mask[y][z] = -1;
                }
            }

            boolean[][] visited = new boolean[SIZE][SIZE];

            for (int z = 0; z < SIZE; z++)
            {
                for (int y = 0; y < SIZE; y++)
                {
                    if (visited[y][z] || mask[y][z] == -1)
                        continue;

                    int id = mask[y][z];
                    int w = 1;

                    while (y + w < SIZE && mask[y + w][z] == id && !visited[y + w][z])
                        w++;

                    int h = 1;

                    boolean done = false;

                    while (z + h < SIZE && !done)
                    {
                        for (int k = 0; k < w; k++)
                        {
                            if (mask[y + k][z + h] != id || visited[y + k][z + h])
                            {
                                done = true;
                                break;
                            }
                        }

                        if (!done)
                            h++;
                    }

                    for (int dy = 0; dy < w; dy++)
                        for (int dz = 0; dz < h; dz++)
                            visited[y + dy][z + dz] = true;

                    addMergedFacePosX(x, y, z, w, h, (short) id, textureAtlas);
                }
            }
        }
    }

    private void greedyMeshNegX(@NotNull TextureAtlas textureAtlas)
    {
        int airId = BlockRegistry.BLOCK_AIR.getId();

        for (int x = 0; x < SIZE; x++)
        {
            int[][] mask = new int[SIZE][SIZE];

            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockId = getBlock(x, y, z);
                    short neighbor = getNeighborBlock(x, y, z, new Vector3i(-1, 0, 0));

                    if (blockId != airId && neighbor != -1 && (!BlockRegistry.get(neighbor).isSolid() || neighbor == airId))
                        mask[y][z] = blockId;
                    else
                        mask[y][z] = -1;
                }
            }

            boolean[][] visited = new boolean[SIZE][SIZE];

            for (int z = 0; z < SIZE; z++)
            {
                for (int y = 0; y < SIZE; y++)
                {
                    if (visited[y][z] || mask[y][z] == -1)
                        continue;

                    int id = mask[y][z];
                    int w = 1;

                    while (y + w < SIZE && mask[y + w][z] == id && !visited[y + w][z])
                        w++;

                    int h = 1;

                    boolean done = false;

                    while (z + h < SIZE && !done)
                    {
                        for (int k = 0; k < w; k++)
                        {
                            if (mask[y + k][z + h] != id || visited[y + k][z + h])
                            {
                                done = true;
                                break;
                            }
                        }

                        if (!done)
                            h++;
                    }

                    for (int dy = 0; dy < w; dy++)
                        for (int dz = 0; dz < h; dz++)
                            visited[y + dy][z + dz] = true;

                    addMergedFaceNegX(x, y, z, w, h, (short) id, textureAtlas);
                }
            }
        }
    }

    private void addMergedFacePosZ(int x, int y, int z, int w, int h, short blockId, @NotNull TextureAtlas textureAtlas)
    {
        Vector3f v0 = new Vector3f(x,     y,     z + 1);
        Vector3f v1 = new Vector3f(x,     y + h, z + 1);
        Vector3f v2 = new Vector3f(x + w, y + h, z + 1);
        Vector3f v3 = new Vector3f(x + w, y,     z + 1);

        Block block = BlockRegistry.get(blockId);
        Vector3f color = block.getColors()[2];

        Vector2f[] localUVs = textureAtlas.getSubTextureCoordinates(block.getTextures()[2], 180);
        
        Vector2f base = new Vector2f(localUVs[0]);
        Vector2f tileSize = new Vector2f(localUVs[2]).sub(localUVs[0]);

        Vector2f uv0 = new Vector2f(0, 0);
        Vector2f uv1 = new Vector2f(0, h);
        Vector2f uv2 = new Vector2f(w, h);
        Vector2f uv3 = new Vector2f(w, 0);

        Vector2f atlasOffset = base;
        Vector2f atlasTileSize = tileSize;

        int startIndex = vertices.size();

        vertices.add(ChunkVertex.create(v0, color, new Vector3f(0, 0, 1), uv0, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v1, color, new Vector3f(0, 0, 1), uv1, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v2, color, new Vector3f(0, 0, 1), uv2, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v3, color, new Vector3f(0, 0, 1), uv3, atlasOffset, atlasTileSize));

        indices.add(startIndex);
        indices.add(startIndex + 2);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex);
        indices.add(startIndex + 3);
    }

    private void addMergedFaceNegZ(int x, int y, int z, int w, int h, short blockId, @NotNull TextureAtlas textureAtlas)
    {
        Vector3f v0 = new Vector3f(x,     y,     z);
        Vector3f v1 = new Vector3f(x,     y + h, z);
        Vector3f v2 = new Vector3f(x + w, y + h, z);
        Vector3f v3 = new Vector3f(x + w, y,     z);

        Block block = BlockRegistry.get(blockId);
        Vector3f color = block.getColors()[3];

        Vector2f[] localUVs = textureAtlas.getSubTextureCoordinates(block.getTextures()[3], 180);
        
        Vector2f base = new Vector2f(localUVs[0]);
        Vector2f tileSize = new Vector2f(localUVs[2]).sub(localUVs[0]);

        Vector2f uv0 = new Vector2f(w, 0);
        Vector2f uv1 = new Vector2f(w, h);
        Vector2f uv2 = new Vector2f(0, h);
        Vector2f uv3 = new Vector2f(0, 0);

        Vector2f atlasOffset = base;
        Vector2f atlasTileSize = tileSize;

        int startIndex = vertices.size();

        vertices.add(ChunkVertex.create(v0, color, new Vector3f(0, 0, -1), uv0, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v1, color, new Vector3f(0, 0, -1), uv1, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v2, color, new Vector3f(0, 0, -1), uv2, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v3, color, new Vector3f(0, 0, -1), uv3, atlasOffset, atlasTileSize));

        indices.add(startIndex);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex + 2);
        indices.add(startIndex + 3);
        indices.add(startIndex);
    }

    private void addMergedFacePosY(int x, int y, int z, int w, int h, short blockId, @NotNull TextureAtlas textureAtlas)
    {
        Vector3f v0 = new Vector3f(x,     y + 1, z);
        Vector3f v1 = new Vector3f(x,     y + 1, z + h);
        Vector3f v2 = new Vector3f(x + w, y + 1, z + h);
        Vector3f v3 = new Vector3f(x + w, y + 1, z);

        Block block = BlockRegistry.get(blockId);
        Vector3f color = block.getColors()[0];

        Vector2f[] localUVs = textureAtlas.getSubTextureCoordinates(block.getTextures()[0]);
        
        Vector2f base = new Vector2f(localUVs[0]);
        Vector2f tileSize = new Vector2f(localUVs[2]).sub(localUVs[0]);

        float eps = 0.0001f;

        Vector2f uv0 = new Vector2f(0, 0);
        Vector2f uv1 = new Vector2f(0, h - eps);
        Vector2f uv2 = new Vector2f(w - eps, h - eps);
        Vector2f uv3 = new Vector2f(w - eps, 0);

        Vector2f atlasOffset = base;
        Vector2f atlasTileSize = tileSize;

        int startIndex = vertices.size();

        vertices.add(ChunkVertex.create(v0, color, new Vector3f(0, 1, 0), uv0, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v1, color, new Vector3f(0, 1, 0), uv1, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v2, color, new Vector3f(0, 1, 0), uv2, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v3, color, new Vector3f(0, 1, 0), uv3, atlasOffset, atlasTileSize));

        indices.add(startIndex);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex + 2);
        indices.add(startIndex + 3);
        indices.add(startIndex);
    }

    private void addMergedFaceNegY(int x, int y, int z, int w, int h, short blockId, @NotNull TextureAtlas textureAtlas)
    {
        Vector3f v0 = new Vector3f(x,     y, z);
        Vector3f v1 = new Vector3f(x,     y, z + h);
        Vector3f v2 = new Vector3f(x + w, y, z + h);
        Vector3f v3 = new Vector3f(x + w, y, z);

        Block block = BlockRegistry.get(blockId);
        Vector3f color = block.getColors()[1];

        Vector2f[] localUVs = textureAtlas.getSubTextureCoordinates(block.getTextures()[1]);
        
        Vector2f base = new Vector2f(localUVs[0]);
        Vector2f tileSize = new Vector2f(localUVs[2]).sub(localUVs[0]);

        float eps = 0.0001f;

        Vector2f uv0 = new Vector2f(0,         0);
        Vector2f uv1 = new Vector2f(0,         h - eps);
        Vector2f uv2 = new Vector2f(w - eps,   h - eps);
        Vector2f uv3 = new Vector2f(w - eps,   0);

        Vector2f atlasOffset = base;
        Vector2f atlasTileSize = tileSize;

        int startIndex = vertices.size();
        vertices.add(ChunkVertex.create(v0, color, new Vector3f(0, -1, 0), uv0, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v1, color, new Vector3f(0, -1, 0), uv1, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v2, color, new Vector3f(0, -1, 0), uv2, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v3, color, new Vector3f(0, -1, 0), uv3, atlasOffset, atlasTileSize));

        indices.add(startIndex);
        indices.add(startIndex + 2);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex);
        indices.add(startIndex + 3);
    }

    private void addMergedFacePosX(int x, int y, int z, int w, int h, short blockId, @NotNull TextureAtlas textureAtlas)
    {
        Vector3f v0 = new Vector3f(x + 1, y,     z);
        Vector3f v1 = new Vector3f(x + 1, y + w, z);
        Vector3f v2 = new Vector3f(x + 1, y + w, z + h);
        Vector3f v3 = new Vector3f(x + 1, y,     z + h);

        Block block = BlockRegistry.get(blockId);
        Vector3f color = block.getColors()[4];

        Vector2f[] localUVs = textureAtlas.getSubTextureCoordinates(block.getTextures()[4], -90);
        
        Vector2f base = new Vector2f(localUVs[0]);
        Vector2f tileSize = new Vector2f(localUVs[2]).sub(localUVs[0]);

        float eps = 0.0001f;

        Vector2f uv0 = new Vector2f(0, 0);
        Vector2f uv1 = new Vector2f(0, w - eps);
        Vector2f uv2 = new Vector2f(h - eps, w - eps);
        Vector2f uv3 = new Vector2f(h - eps, 0);

        Vector2f atlasOffset = base;
        Vector2f atlasTileSize = tileSize;

        int startIndex = vertices.size();

        vertices.add(ChunkVertex.create(v0, color, new Vector3f(1, 0, 0), uv0, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v1, color, new Vector3f(1, 0, 0), uv1, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v2, color, new Vector3f(1, 0, 0), uv2, atlasOffset, atlasTileSize));
        vertices.add(ChunkVertex.create(v3, color, new Vector3f(1, 0, 0), uv3, atlasOffset, atlasTileSize));


        indices.add(startIndex);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex + 2);
        indices.add(startIndex + 3);
        indices.add(startIndex);
    }

    private void addMergedFaceNegX(int x, int y, int z, int w, int h, short blockId, @NotNull TextureAtlas textureAtlas)
    {
        Vector3f v0 = new Vector3f(x,     y,     z);
        Vector3f v1 = new Vector3f(x,     y + w, z);
        Vector3f v2 = new Vector3f(x,     y + w, z + h);
        Vector3f v3 = new Vector3f(x,     y,     z + h);

        Block block = BlockRegistry.get(blockId);
        Vector3f color = block.getColors()[5];

        Vector2f[] localUVs = textureAtlas.getSubTextureCoordinates(block.getTextures()[5], -90);

        float eps = 0.0001f;

        Vector2f base = new Vector2f(localUVs[0]);
        Vector2f tileSize = new Vector2f(localUVs[2]).sub(localUVs[0]);

        Vector2f uv0 = new Vector2f(0, 0);
        Vector2f uv1 = new Vector2f(0, w - eps);
        Vector2f uv2 = new Vector2f(h - eps, w - eps);
        Vector2f uv3 = new Vector2f(h - eps, 0);

        int startIndex = vertices.size();

        vertices.add(ChunkVertex.create(v0, color, new Vector3f(-1, 0, 0), uv0, base, tileSize));
        vertices.add(ChunkVertex.create(v1, color, new Vector3f(-1, 0, 0), uv1, base, tileSize));
        vertices.add(ChunkVertex.create(v2, color, new Vector3f(-1, 0, 0), uv2, base, tileSize));
        vertices.add(ChunkVertex.create(v3, color, new Vector3f(-1, 0, 0), uv3, base, tileSize));

        indices.add(startIndex);
        indices.add(startIndex + 2);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex);
        indices.add(startIndex + 3);
    }

    public void setBlock(@NotNull Entity interactor, @NotNull Vector3i blockPosition, short type)
    {
        if (!isValidPosition(blockPosition))
            return;

        synchronized(this)
        {
            modified = true;
            int index = toIndex(blockPosition.x, blockPosition.y, blockPosition.z);

            Vector3i globalBlockCoordinates = CoordinateHelper.worldToGlobalBlockCoordinates(CoordinateHelper.blockToWorldCoordinates(blockPosition, CoordinateHelper.worldToChunkCoordinates(getGameObject().getTransform().getWorldPosition())));

            short airId = BlockRegistry.BLOCK_AIR.getId();

            if (type == airId)
            {
                int airPaletteIndex = palette.indexOf(airId);

                if (airPaletteIndex < 0)
                    airPaletteIndex = 0;

                setPaletteIndex(index, airPaletteIndex);
                BlockRegistry.get(airId).onBroken(interactor, World.getLocalWorld(), this, globalBlockCoordinates);
            }
            else
            {
                int paletteIndex = palette.indexOf(type);
                if (paletteIndex < 0)
                {
                    palette.add(type);

                    int neededBits = Math.max(1, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));

                    if (neededBits > bitsPerBlock)
                    {
                        int oldBits = bitsPerBlock;

                        bitsPerBlock = neededBits;
                        repackBlockData(oldBits);
                    }

                    paletteIndex = palette.size() - 1;
                }
                setPaletteIndex(index, paletteIndex);
                BlockRegistry.get(type).onPlaced(interactor, World.getLocalWorld(), this, globalBlockCoordinates);
            }

            List<Vector3i> neighboringChunkOffsets = List.of
            (
                new Vector3i(0, 1, 0),
                new Vector3i(0, -1, 0),
                new Vector3i(0, 0, 1),
                new Vector3i(0, 0, -1),
                new Vector3i(1, 0, 0),
                new Vector3i(-1, 0, 0)
            );

            for (final Vector3i offset : neighboringChunkOffsets)
            {
                Vector3i position = CoordinateHelper.worldToChunkCoordinates(getGameObject().getTransform().getWorldPosition()).add(offset, new Vector3i());

                if (World.getLocalWorld().getChunk(position) != null)
                    World.getLocalWorld().scheduleRegeneration(position);
            }

            generate();
        }
    }

    private static Vector2f[] addPadding(Vector2f[] localUVs, float pad)
    {
        Vector2f[] padded = new Vector2f[4];

        padded[0] = new Vector2f(
                localUVs[0].x + pad,
                localUVs[0].y + pad
        );

        padded[1] = new Vector2f(
                localUVs[1].x - pad,
                localUVs[1].y + pad
        );

        padded[2] = new Vector2f(
                localUVs[2].x - pad,
                localUVs[2].y - pad
        );

        padded[3] = new Vector2f(
                localUVs[3].x + pad,
                localUVs[3].y - pad
        );

        return padded;
    }

    public short getBlock(@NotNull Vector3i blockPosition)
    {
        if (!isValidPosition(blockPosition))
            return -1;

        return getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
    }

    public short getBlock(int x, int y, int z)
    {
        if (!isValidPosition(new Vector3i(x, y, z)))
            return -1;

        int index = toIndex(x, y, z);

        int neededBits = Math.max(1, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));

        if (neededBits > bitsPerBlock)
        {
            int oldBits = bitsPerBlock;

            bitsPerBlock = neededBits;

            repackBlockData(oldBits);
        }

        int paletteIndex = getPaletteIndex(index, bitsPerBlock);

        return palette.get(paletteIndex);
    }

    public short[][][] getBlocks()
    {
        short[][][] blocksArray = new short[SIZE][SIZE][SIZE];

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                    blocksArray[x][y][z] = getBlock(x, y, z);
            }
        }

        return blocksArray;
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified(boolean modified)
    {
        this.modified = modified;
    }

    private boolean isBlockExposed(int x, int y, int z)
    {
        short blockId = getBlock(x, y, z);

        if (blockId == BlockRegistry.BLOCK_AIR.getId() || !BlockRegistry.get(blockId).isSolid())
            return false;

        if (x == 0 || (getNeighborBlock(x, y, z, new Vector3i(-1, 0, 0)) == BlockRegistry.BLOCK_AIR.getId() && getNeighborBlock(x, y, z, new Vector3i(-1, 0, 0)) != -1 || !BlockRegistry.get(getNeighborBlock(x, y, z, new Vector3i(-1, 0, 0))).isSolid()))
            return true;

        if (x == SIZE - 1 || (getNeighborBlock(x, y, z, new Vector3i(1, 0, 0)) == BlockRegistry.BLOCK_AIR.getId() && getNeighborBlock(x, y, z, new Vector3i(1, 0, 0)) != -1 || !BlockRegistry.get(getNeighborBlock(x, y, z, new Vector3i(1, 0, 0))).isSolid()))
            return true;

        if (y == 0 || (getNeighborBlock(x, y, z, new Vector3i(0, -1, 0)) == BlockRegistry.BLOCK_AIR.getId() && getNeighborBlock(x, y, z, new Vector3i(0, -1, 0)) != -1 || !BlockRegistry.get(getNeighborBlock(x, y, z, new Vector3i(0, -1, 0))).isSolid()))
            return true;

        if (y == SIZE - 1 || (getNeighborBlock(x, y, z, new Vector3i(0, 1, 0)) == BlockRegistry.BLOCK_AIR.getId() && getNeighborBlock(x, y, z, new Vector3i(0, 1, 0)) != -1 || !BlockRegistry.get(getNeighborBlock(x, y, z, new Vector3i(0, 1, 0))).isSolid()))
            return true;

        if (z == 0 || (getNeighborBlock(x, y, z, new Vector3i(0, 0, -1)) == BlockRegistry.BLOCK_AIR.getId() && getNeighborBlock(x, y, z, new Vector3i(0, 0, -1)) == -1 || !BlockRegistry.get(getNeighborBlock(x, y, z, new Vector3i(0, 0, -1))).isSolid()))
            return true;

        if (z == SIZE - 1 || (getNeighborBlock(x, y, z, new Vector3i(0, 0, 1)) == BlockRegistry.BLOCK_AIR.getId() && getNeighborBlock(x, y, z, new Vector3i(0, 0, 1)) == -1 || !BlockRegistry.get(getNeighborBlock(x, y, z, new Vector3i(0, 0, 1))).isSolid()))
            return true;

        return false;
    }

    private short getNeighborBlock(int x, int y, int z, @NotNull Vector3i offset)
    {
        int nx = x + offset.x;
        int ny = y + offset.y;
        int nz = z + offset.z;

        if (nx < 0 || nx >= SIZE || ny < 0 || ny >= SIZE || nz < 0 || nz >= SIZE)
        {
            Vector3i neighborPos = new Vector3i(nx, ny, nz);

            short worldBlock = World.getLocalWorld().getBlock(CoordinateHelper.blockToWorldCoordinates(neighborPos, CoordinateHelper.worldToChunkCoordinates(getGameObject().getTransform().getWorldPosition())));

            if (worldBlock != -1 && worldBlock == BlockRegistry.BLOCK_AIR.getId())
                return BlockRegistry.BLOCK_AIR.getId();

            return worldBlock;
        }

        return getBlock(nx, ny, nz);
    }

    private boolean shouldRenderFace(@NotNull Vector3i position)
    {
        short block = World.getLocalWorld().getBlock(CoordinateHelper.blockToWorldCoordinates(position, CoordinateHelper.worldToChunkCoordinates(getGameObject().getTransform().getWorldPosition())));

        return block != -1 && block == BlockRegistry.BLOCK_AIR.getId();
    }

    private float fract(float value)
    {
        return value - (float)Math.floor(value);
    }

    private Vector2f computeFinalUV(Vector2f base, Vector2f tileSize, float localU, float localV)
    {
        float u = fract(localU);
        float v = fract(localV);

        return new Vector2f(base).add(new Vector2f(u, v).mul(tileSize));
    }

    private boolean isValidPosition(@NotNull Vector3i position)
    {
        return position.x >= 0 && position.x < SIZE &&
                position.y >= 0 && position.y < SIZE &&
                position.z >= 0 && position.z < SIZE;
    }

    private static @NotNull Vector3i fromIndex(int index)
    {
        int x = index & 0xF;
        int y = (index >> 4) & 0xF;
        int z = (index >> 8) & 0xF;

        return new Vector3i(x, y, z);
    }

    private static int toIndex(int x, int y, int z)
    {
        return (x & 0xF) | ((y & 0xF) << 4) | ((z & 0xF) << 8);
    }

    private int getPaletteIndex(int index, int bits)
    {
        int bitIndex = index * bits;
        int arrayIndex = bitIndex / 64;
        int offset = bitIndex % 64;

        if (arrayIndex >= blockData.length)
        {
            System.err.println("getPaletteIndex(old): arrayIndex (" + arrayIndex +
                    ") out of bounds (length " + blockData.length +
                    ") for index " + index + " with bits " + bits);

            return 0;
        }

        long value = blockData[arrayIndex] >>> offset;

        int bitsInCurrentLong = 64 - offset;

        if (bitsInCurrentLong < bits && (arrayIndex + 1) < blockData.length)
            value |= blockData[arrayIndex + 1] << bitsInCurrentLong;

        return (int)(value & ((1 << bits) - 1));
    }

    private void setPaletteIndex(int index, int paletteIndex)
    {
        int bitIndex = index * bitsPerBlock;
        int arrayIndex = bitIndex / 64;
        int offset = bitIndex % 64;

        long mask = ((1L << bitsPerBlock) - 1L) << offset;
        blockData[arrayIndex] = (blockData[arrayIndex] & ~mask) | (((long) paletteIndex << offset) & mask);

        int bitsInCurrentLong = 64 - offset;

        if (bitsInCurrentLong < bitsPerBlock)
        {
            if (arrayIndex + 1 < blockData.length)
            {
                mask = (1L << (bitsPerBlock - bitsInCurrentLong)) - 1L;
                blockData[arrayIndex + 1] = (blockData[arrayIndex + 1] & ~mask) | ((long) paletteIndex >> bitsInCurrentLong);
            }
            else
                System.err.println("setPaletteIndex: index " + index + " requires blockData[" + (arrayIndex+1) + "] but length is " + blockData.length);
        }
    }

    private void repackBlockData(int oldBits)
    {
        int totalBlocks = SIZE * SIZE * SIZE;
        int newLength = (totalBlocks * bitsPerBlock + 63) / 64;

        long[] newBlockData = new long[newLength];

        for (int i = 0; i < totalBlocks; i++)
        {
            int paletteIndex = getPaletteIndex(i, oldBits);

            int bitIndex = i * bitsPerBlock;

            int arrayIndex = bitIndex / 64;
            int offset = bitIndex % 64;

            long mask = ((1L << bitsPerBlock) - 1L) << offset;

            newBlockData[arrayIndex] = (newBlockData[arrayIndex] & ~mask) | (((long) paletteIndex << offset) & mask);

            int bitsInCurrentLong = 64 - offset;

            if (bitsInCurrentLong < bitsPerBlock)
            {
                int bitsLeft = bitsPerBlock - bitsInCurrentLong;

                mask = (1L << bitsLeft) - 1L;

                if (arrayIndex + 1 < newBlockData.length)
                    newBlockData[arrayIndex + 1] = (newBlockData[arrayIndex + 1] & ~mask) | ((long) paletteIndex >> bitsInCurrentLong);
                else
                    System.err.println("repackBlockData: attempted write out-of-bounds for block " + i);
            }
        }

        blockData = newBlockData;
    }

    private static float[] getAmbientOcclusionSubstituteLighting(@NotNull Vector3i normal)
    {
        float[] ao;

        if (normal.y > 0)
            ao = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        else if (normal.z < 0)
            ao = new float[] { 0.65f, 0.65f, 0.65f, 0.65f };
        else if (normal.x < 0)
            ao = new float[] { 0.85f, 0.85f, 0.85f, 0.85f };
        else
            ao = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

        return ao;
    }

    private void addRenderingVoxelPosition(int x, int y, int z)
    {
        Vector3f position = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);

        if (!renderingVoxelPositions.contains(position))
            renderingVoxelPositions.add(position);
    }


    public static @NotNull Chunk create()
    {
        short[][][] blocks = new short[SIZE][SIZE][SIZE];

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                    blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getId();
            }
        }

        return create(blocks);
    }

    public static @NotNull Chunk create(short[][][] blocks)
    {
        Chunk result = new Chunk();

        result.palette = new ArrayList<>();

        short airId = BlockRegistry.BLOCK_AIR.getId();

        result.palette.add(airId);

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockType = blocks[x][y][z];

                    if (!result.palette.contains(blockType))
                        result.palette.add(blockType);
                }
            }
        }

        result.bitsPerBlock = Math.max(1, Integer.SIZE - Integer.numberOfLeadingZeros(result.palette.size() - 1));

        int totalBlocks = SIZE * SIZE * SIZE;
        int arrayLength = (totalBlocks * result.bitsPerBlock + 63) / 64;

        result.blockData = new long[arrayLength];

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    int index = toIndex(x, y, z);
                    short blockType = blocks[x][y][z];
                    int paletteIndex = result.palette.indexOf(blockType);

                    result.setPaletteIndex(index, paletteIndex);
                }
            }
        }

        return result;
    }
}