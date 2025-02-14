package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.math.Transform;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.util.CoordinateHelper;
import com.thatsoulyguy.moonlander.util.SerializableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.*;

@CustomConstructor("create")
public class World extends Component
{
    public static final int WORLD_HEIGHT = 256;
    public static final int VERTICAL_CHUNKS = WORLD_HEIGHT / Chunk.SIZE;

    public long seed = 354576879657L;

    private @EffectivelyNotNull String name;

    public @Nullable Transform chunkLoader;

    private final @NotNull Set<Vector3i> loadedChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull Set<Vector3i> generatingChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull ConcurrentMap<Vector3i, Future<?>> ongoingChunkGenerations = new ConcurrentHashMap<>();

    private final @NotNull List<TerrainGenerator> terrainGenerators = new ArrayList<>();

    private final @NotNull Queue<Vector3i> pendingRegenerationQueue = new ConcurrentLinkedQueue<>();

    private final @NotNull Set<Vector3i> pendingRegenerationSet = ConcurrentHashMap.newKeySet();

    private transient @EffectivelyNotNull ExecutorService chunkGenerationExecutor;

    private final @NotNull SerializableObject chunkLock = new SerializableObject();

    private World() { }

    @Override
    public void initialize()
    {
        generatingChunks.clear();
        chunkGenerationExecutor = Executors.newFixedThreadPool(3);
    }

    @Override
    public void update()
    {
        loadCloseChunks();
        unloadFarChunks();
        processPendingRegeneration();
    }

    public @NotNull Chunk generateChunk(@NotNull Vector3i chunkPosition)
    {
        GameObject object = GameObject.create("default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z, Layer.DEFAULT);

        object.getTransform().setLocalPosition(CoordinateHelper.chunkToWorldCoordinates(chunkPosition));

        object.addComponent(Collider.create(VoxelMeshCollider.class));

        object.addComponent(Objects.requireNonNull(ShaderManager.get("pass.geometry")));
        object.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));

        object.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

        short[][][] blocks = new short[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];

        terrainGenerators.forEach(generator ->
        {
            generator.setSeed(seed);
            generator.setScale(0.006d);
            generator.generateBlocks(blocks, chunkPosition);
        });

        object.addComponent(Chunk.create(blocks));

        Objects.requireNonNull(object.getComponent(Chunk.class)).generate();

        return Objects.requireNonNull(object.getComponent(Chunk.class));
    }

    public void unloadChunk(@NotNull Vector3i chunkPosition)
    {
        if (!loadedChunks.contains(chunkPosition))
        {
            System.err.println("Loaded chunks list does not contain key: " + chunkPosition + "!");
            return;
        }

        GameObjectManager.unregister("default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z, true);

        loadedChunks.remove(chunkPosition);
    }

    public @NotNull List<Vector3i> getLoadedChunks()
    {
        return loadedChunks.stream().toList();
    }

    public @NotNull String getName()
    {
        return name;
    }

    /**
     * Sets the type of a block in the world.
     *
     * @param interactor The entity setting the block
     * @param worldPosition The world position at which a block is set.
     * @param type The type of the block being set
     */
    public boolean setBlock(@NotNull Entity interactor, @NotNull Vector3f worldPosition, short type)
    {
        Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(worldPosition);
        Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(worldPosition);

        if (!loadedChunks.contains(chunkCoordinates))
            return false;
        else
        {
            Objects.requireNonNull(getChunk(chunkCoordinates)).setBlock(interactor, blockCoordinates, type);
            return true;
        }
    }

    /**
     * Gets the type of block in the world.
     * Returns -1 if no block is found
     *
     * @param worldPosition The position of the block in world coordinates
     * @return The type of the block
     */
    public short getBlock(@NotNull Vector3f worldPosition)
    {
        Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(worldPosition);
        Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(worldPosition);

        if (!loadedChunks.contains(chunkCoordinates))
            return -1;
        else
        {
            return Objects.requireNonNull(getChunk(chunkCoordinates)).getBlock(blockCoordinates);
        }
    }

    /**
     * Gets the type of block in the world.
     * Returns -1 if no block is found
     *
     * @param blockPosition The position of the block in global block coordinates
     * @return The type of the block
     */
    public short getBlock(@NotNull Vector3i blockPosition)
    {
        Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(CoordinateHelper.globalBlockToWorldCoordinates(blockPosition));

        if (!loadedChunks.contains(chunkCoordinates))
            return -1;
        else
        {
            return Objects.requireNonNull(getChunk(chunkCoordinates)).getBlock(blockPosition);
        }
    }

    public @Nullable Chunk getChunk(@NotNull Vector3i chunkPosition)
    {
        if (!loadedChunks.contains(chunkPosition))
            return null;

        return Objects.requireNonNull(GameObjectManager.get("default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z)).getComponent(Chunk.class);
    }

    public static @NotNull World getLocalWorld()
    {
        return Objects.requireNonNull(Objects.requireNonNull(GameObjectManager.get("default.world")).getComponent(World.class));
    }

    public void addTerrainGenerator(@NotNull TerrainGenerator generator)
    {
        terrainGenerators.add(generator);
    }

    public void loadCloseChunks()
    {
        if (chunkLoader == null)
            return;

        Vector3f playerWorldPosition = chunkLoader.getWorldPosition();
        Vector3i playerChunkPosition = CoordinateHelper.worldToChunkCoordinates(playerWorldPosition);

        List<Vector3i> chunkPositions = new ArrayList<>();

        for (int cx = playerChunkPosition.x - Settings.RENDER_DISTANCE.getValue(); cx <= playerChunkPosition.x + Settings.RENDER_DISTANCE.getValue(); cx++)
        {
            for (int cz = playerChunkPosition.z - Settings.RENDER_DISTANCE.getValue(); cz <= playerChunkPosition.z + Settings.RENDER_DISTANCE.getValue(); cz++)
            {
                for (int cy = 0; cy < VERTICAL_CHUNKS; cy++)
                    chunkPositions.add(new Vector3i(cx, cy, cz));
            }
        }

        chunkPositions.sort(Comparator.comparingInt(pos -> Math.toIntExact(playerChunkPosition.distanceSquared(pos))));

        for (Vector3i currentChunk : chunkPositions)
        {
            if (loadedChunks.contains(currentChunk) || ongoingChunkGenerations.containsKey(currentChunk))
                continue;

            Future<?> future = chunkGenerationExecutor.submit(() ->
            {
                try
                {
                    generateChunk(currentChunk);

                    synchronized (chunkLock)
                    {
                        if (!loadedChunks.add(currentChunk))
                            System.err.println("Chunk already loaded: " + currentChunk);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Error generating chunk " + currentChunk + ": " + e.getMessage());
                    e.printStackTrace();
                }
                finally
                {
                    ongoingChunkGenerations.remove(currentChunk);

                    List<Vector3i> neighboringChunkOffsets = List.of(
                            new Vector3i(0, 1, 0),
                            new Vector3i(0, -1, 0),
                            new Vector3i(0, 0, 1),
                            new Vector3i(0, 0, -1),
                            new Vector3i(1, 0, 0),
                            new Vector3i(-1, 0, 0)
                    );

                    for (Vector3i offset : neighboringChunkOffsets)
                    {
                        Vector3i neighborPos = new Vector3i(currentChunk.x + offset.x, currentChunk.y + offset.y, currentChunk.z + offset.z);

                        if (getChunk(neighborPos) != null)
                            scheduleRegeneration(neighborPos);
                    }
                }
            });

            ongoingChunkGenerations.put(currentChunk, future);
        }
    }

    public void scheduleRegeneration(Vector3i chunkPosition)
    {
        if (!pendingRegenerationSet.contains(chunkPosition))
        {
            pendingRegenerationSet.add(chunkPosition);
            pendingRegenerationQueue.offer(chunkPosition);
        }
    }

    private void unloadFarChunks()
    {
        synchronized (chunkLock)
        {
            if (chunkLoader == null)
                return;

            Vector3f playerWorldPosition = chunkLoader.getWorldPosition();
            Vector3i playerChunkPosition = CoordinateHelper.worldToChunkCoordinates(playerWorldPosition);

            int unloadDistance = Settings.RENDER_DISTANCE.getValue() + 1;

            loadedChunks.removeIf(chunkPosition ->
            {
                int dx = Math.abs(chunkPosition.x - playerChunkPosition.x);
                int dz = Math.abs(chunkPosition.z - playerChunkPosition.z);

                if (dx > unloadDistance || dz > unloadDistance)
                {
                    unloadChunk(chunkPosition);
                    return true;
                }

                return false;
            });
        }
    }

    private void processPendingRegeneration()
    {
        int tasksToProcess = 5;

        for (int i = 0; i < tasksToProcess; i++)
        {
            Vector3i chunkPos = pendingRegenerationQueue.poll();

            if (chunkPos == null)
                break;

            pendingRegenerationSet.remove(chunkPos);

            Chunk chunk = getChunk(chunkPos);

            if (chunk != null)
            {
                chunkGenerationExecutor.submit(() ->
                {
                    try
                    {
                        chunk.generate();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error regenerating chunk " + chunkPos + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void uninitialize()
    {
        chunkGenerationExecutor.shutdown();

        try
        {
            if (!chunkGenerationExecutor.awaitTermination(20, TimeUnit.SECONDS))
                chunkGenerationExecutor.shutdownNow();
        }
        catch (InterruptedException e)
        {
            chunkGenerationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static @NotNull World create(@NotNull String name)
    {
        World result = new World();

        result.name = name;

        return result;
    }
}