package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.Vertex;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import com.thatsoulyguy.moonlander.util.CoordinateHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

@CustomConstructor("create")
public class Chunk extends Component
{
    public static final byte SIZE = 16;

    private transient List<Vertex> vertices = new ArrayList<>();
    private transient List<Integer> indices = new ArrayList<>();

    private Map<Integer, Short> blocks = new HashMap<>();

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

        List<Vector3f> renderingVoxelPositions = new ArrayList<>();

        needsToUpdate = false;

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockId = getBlock(x, y, z);

                    if (blockId == BlockRegistry.BLOCK_AIR.getId())
                        continue;

                    TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);

                    if (textureAtlas == null)
                    {
                        System.err.println("Texture atlas was not found on chunk object!");
                        return;
                    }

                    Block block = Objects.requireNonNull(BlockRegistry.get(blockId));

                    if (block.updates())
                        needsToUpdate = true;

                    renderFaceIfNeeded(x, y, z, textureAtlas, block, renderingVoxelPositions);
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
            mesh.modify((verticesIn ->
            {
                verticesIn.clear();
                verticesIn.addAll(vertices);
            }),
            (indicesIn) ->
            {
                indicesIn.clear();
                indicesIn.addAll(indices);
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

        blocks.entrySet().parallelStream().forEach(entry ->
        {
            int index = entry.getKey();
            short blockId = entry.getValue();

            Vector3i localPos = fromIndex(index);

            Vector3i globalBlockPos = new Vector3i(
                    (int)(chunkWorldPos.x) + localPos.x,
                    (int)(chunkWorldPos.y) + localPos.y,
                    (int)(chunkWorldPos.z) + localPos.z
            );

            BlockRegistry.get(blockId).onTick(World.getLocalWorld(), this, globalBlockPos);
        });
    }

    private void renderFaceIfNeeded(int x, int y, int z, TextureAtlas textureAtlas, Block block, List<Vector3f> renderingVoxelPositions)
    {
        Vector3f basePosition = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);

        if (!renderingVoxelPositions.contains(basePosition) && block.isSolid())
            renderingVoxelPositions.add(basePosition);

        Vector3i[] directions =
        {
            new Vector3i( 0,  0,  1),
            new Vector3i( 0,  0, -1),
            new Vector3i( 0,  1,  0),
            new Vector3i( 0, -1,  0),
            new Vector3i( 1,  0,  0),
            new Vector3i(-1,  0,  0)
        };

        int[] textureRotations = { 180, 180, 0, 0, -90, 90 };
        int[] colorIndices = { 2, 3, 0, 1, 4, 5 };

        for (int i = 0; i < directions.length; i++)
        {
            Vector3i direction = directions[i];
            int colorIndex = colorIndices[i];
            int rotation = textureRotations[i];
            Vector3i neighborPosition = new Vector3i(x + direction.x, y + direction.y, z + direction.z);

            Block neighborBlock = BlockRegistry.get(getBlock(neighborPosition));

            if (shouldRenderFace(neighborPosition) || (neighborBlock != null && !neighborBlock.isSolid()))
            {
                addFace(
                        new Vector3i(x, y, z),
                        direction,
                        block.getColors()[colorIndex],
                        textureAtlas.getSubTextureCoordinates(block.getTextures()[colorIndex], rotation)
                );
            }
        }
    }

    private void addRenderingVoxelPosition(@NotNull List<Vector3f> voxelPositions, int x, int y, int z)
    {
        Vector3f position = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);

        if (!voxelPositions.contains(position))
            voxelPositions.add(position);
    }

    /**
     * Sets the block at the given position to 'type' and updates the chunk mesh.
     * If you "break" a block (set it to air), this will remove its faces.
     * If you place a new block, it'll add its faces.
     *
     * @param interactor The entity setting the block
     * @param blockPosition The (x, y, z) position in chunk space
     * @param type The block ID to place
     */
    public void setBlock(@NotNull Entity interactor, @NotNull Vector3i blockPosition, short type)
    {
        if (!isValidPosition(blockPosition))
            return;

        modified = true;

        int index = toIndex(blockPosition.x, blockPosition.y, blockPosition.z);

        Vector3i globalBlockCoordinates = CoordinateHelper.worldToGlobalBlockCoordinates(CoordinateHelper.blockToWorldCoordinates(blockPosition, CoordinateHelper.worldToChunkCoordinates(getGameObject().getTransform().getWorldPosition())));

        if (type == BlockRegistry.BLOCK_AIR.getId())
        {
            blocks.remove(index);
            BlockRegistry.get(type).onBroken(interactor, World.getLocalWorld(), this, globalBlockCoordinates);
        }
        else
        {
            blocks.put(index, type);
            BlockRegistry.get(type).onPlaced(interactor, World.getLocalWorld(), this, globalBlockCoordinates);
        }

        List<Vector3i> neighboringChunkOffsets = List.of(
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

        rebuildMeshAndCollider();
    }

    /**
     * Gets the type of the block at the specified position.
     * Returns -1 if the block is outside the bounds of the chunk
     *
     * @param blockPosition The position in block coordinates
     * @return The type of block retrieved
     */
    public short getBlock(@NotNull Vector3i blockPosition)
    {
        if (!isValidPosition(blockPosition))
            return -1;

        return getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
    }

    public short[][][] getBlocks()
    {
        short[][][] blocks = new short[16][16][16];

        for (Map.Entry<Integer, Short> entry : this.blocks.entrySet())
        {
            Vector3i position = fromIndex(entry.getKey());

            blocks[position.x][position.y][position.z] = entry.getValue();
        }

        return blocks;
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified(boolean modified)
    {
        this.modified = modified;
    }

    private short getBlock(int x, int y, int z)
    {
        int index = toIndex(x, y, z);
        return blocks.getOrDefault(index, BlockRegistry.BLOCK_AIR.getId());
    }

    private boolean shouldRenderFace(@NotNull Vector3i position)
    {
        short block = World.getLocalWorld().getBlock(CoordinateHelper.blockToWorldCoordinates(position, CoordinateHelper.worldToChunkCoordinates(getGameObject().getTransform().getWorldPosition())));

        if (block == -1)
            return false;
        else
            return block == BlockRegistry.BLOCK_AIR.getId();
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

    private void rebuildMeshAndCollider()
    {
        vertices.clear();
        indices.clear();

        List<Vector3f> renderingVoxelPositions = new ArrayList<>();

        TextureAtlas textureAtlas = getGameObject().getComponent(TextureAtlas.class);
        if (textureAtlas == null)
        {
            System.err.println("Texture atlas was not found on chunk object!");
            return;
        }

        needsToUpdate = false;

        for (int x = 0; x < SIZE; x++)
        {
            for (int y = 0; y < SIZE; y++)
            {
                for (int z = 0; z < SIZE; z++)
                {
                    short blockID = getBlock(x, y, z);

                    if (blockID == BlockRegistry.BLOCK_AIR.getId())
                        continue;

                    Block block = Objects.requireNonNull(BlockRegistry.get(blockID));

                    if (block.updates())
                        needsToUpdate = true;

                    renderFaceIfNeeded(x, y, z, textureAtlas, block, renderingVoxelPositions);
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

        if (!vertices.isEmpty() && !indices.isEmpty())
        {
            mesh.setTransient(true);
            collider.setTransient(true);

            MainThreadExecutor.submit(() -> mesh.modify(
                    vertList ->
                    {
                        vertList.clear();
                        vertList.addAll(vertices);
                    },
                    idxList ->
                    {
                        idxList.clear();
                        idxList.addAll(indices);
                    }
            ));

            collider.setVoxels(renderingVoxelPositions);
        }
        else
        {
            MainThreadExecutor.submit(() -> mesh.modify
            (
                    List::clear,
                    List::clear
            ));

            collider.setVoxels(Collections.emptyList());
        }
    }

    private void addFace(@NotNull Vector3i position, @NotNull Vector3i normal, @NotNull Vector3f baseColor, @Nullable Vector2f[] uvs)
    {
        synchronized (this)
        {
            if (uvs == null)
                return;

            Vector3i[] faceVertices = getFaceVerticesForNormal(position, normal);

            for (int i = 0; i < 4; i++)
                vertices.add(Vertex.create(new Vector3f(faceVertices[i]), baseColor, new Vector3f(normal.x, normal.y, normal.z), uvs[i]));

            int startIndex = this.vertices.size() - 4;

            boolean isTop = (normal.x == 0 && normal.y == 1 && normal.z == 0);
            boolean isBottom = (normal.x == 0 && normal.y == -1 && normal.z == 0);

            if (isTop || isBottom) {
                indices.add(startIndex);
                indices.add(startIndex + 1);
                indices.add(startIndex + 2);
                indices.add(startIndex + 2);
                indices.add(startIndex + 3);
                indices.add(startIndex);
            } else {
                indices.add(startIndex);
                indices.add(startIndex + 2);
                indices.add(startIndex + 1);
                indices.add(startIndex + 2);
                indices.add(startIndex);
                indices.add(startIndex + 3);
            }
        }
    }

    private static float[] getAmbientOcclusionSubstituteLighting(@NotNull Vector3i normal)
    {
        float[] ao;

        if (normal.y > 0)
            ao = new float[]
            {
                1.0f,
                1.0f,
                1.0f,
                1.0f
            };
        else if (normal.z < 0)
            ao = new float[]
            {
                0.65f,
                0.65f,
                0.65f,
                0.65f
            };
        else if (normal.x < 0)
            ao = new float[]
            {
                0.85f,
                0.85f,
                0.85f,
                0.85f
            };
        else
            ao = new float[]
            {
                1.0f,
                1.0f,
                1.0f,
                1.0f
            };

        return ao;
    }

    private @NotNull Vector3i[] getFaceVerticesForNormal(@NotNull Vector3i position, @NotNull Vector3i normal)
    {
        int x = position.x;
        int y = position.y;
        int z = position.z;

        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
        {
            return new Vector3i[]
            {
                new Vector3i(x, y, z + 1),
                new Vector3i(x, y + 1, z + 1),
                new Vector3i(x + 1, y + 1, z + 1),
                new Vector3i(x + 1, y, z + 1)
            };
        }
        else if (normal.x == 0 && normal.y == 0 && normal.z == -1)
        {
            return new Vector3i[]
            {
                new Vector3i(x + 1, y, z),
                new Vector3i(x + 1, y + 1, z),
                new Vector3i(x, y + 1, z),
                new Vector3i(x, y, z)
            };
        }
        else if (normal.x == 0 && normal.y == 1 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x, y + 1, z),
                new Vector3i(x, y + 1, z + 1),
                new Vector3i(x + 1, y + 1, z + 1),
                new Vector3i(x + 1, y + 1, z)
            };
        }
        else if (normal.x == 0 && normal.y == -1 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x + 1, y, z),
                new Vector3i(x + 1, y, z + 1),
                new Vector3i(x, y, z + 1),
                new Vector3i(x, y, z)
            };
        }
        else if (normal.x == 1 && normal.y == 0 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x + 1, y, z),
                new Vector3i(x + 1, y, z + 1),
                new Vector3i(x + 1, y + 1, z + 1),
                new Vector3i(x + 1, y + 1, z)
            };
        }
        else if (normal.x == -1 && normal.y == 0 && normal.z == 0)
        {
            return new Vector3i[]
            {
                new Vector3i(x, y + 1, z),
                new Vector3i(x, y + 1, z + 1),
                new Vector3i(x, y, z + 1),
                new Vector3i(x, y, z)
            };
        }

        return new Vector3i[]
        {
            new Vector3i(x, y, z),
            new Vector3i(x, y, z),
            new Vector3i(x, y, z),
            new Vector3i(x, y, z)
        };
    }

    public static @NotNull Chunk create()
    {
        return create(new short[SIZE][SIZE][SIZE]);
    }

    public static @NotNull Chunk create(short[][][] blocks)
    {
        Chunk result = new Chunk();

        Map<Integer, Short> blockMap = new HashMap<>();

        for (int x = 0; x < blocks.length; x++)
        {
            for (int y = 0; y < blocks[x].length; y++)
            {
                for (int z = 0; z < blocks[x][y].length; z++)
                {
                    short blockType = blocks[x][y][z];

                    if (blockType != BlockRegistry.BLOCK_AIR.getId())
                    {
                        int index = toIndex(x, y, z);

                        blockMap.put(index, blockType);
                    }
                }
            }
        }

        result.blocks = blockMap;

        return result;
    }
}