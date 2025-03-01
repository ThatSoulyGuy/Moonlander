package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.colliders.BoxCollider;
import com.thatsoulyguy.moonlander.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.math.Rigidbody;
import com.thatsoulyguy.moonlander.math.Transform;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.util.CoordinateHelper;
import com.thatsoulyguy.moonlander.util.Pair;
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

    public long seed = -1;

    private @EffectivelyNotNull String name;

    public @Nullable Transform chunkLoader;

    private final @NotNull Set<Vector3i> loadedChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull Set<Vector3i> generatingChunks = ConcurrentHashMap.newKeySet();

    private final @NotNull ConcurrentMap<Vector3i, Future<?>> ongoingChunkGenerations = new ConcurrentHashMap<>();

    private final @NotNull List<TerrainGenerator> terrainGenerators = new ArrayList<>();
    private final @NotNull List<RegionalSpawner> regionalSpawners = new ArrayList<>();

    private final @NotNull Queue<Vector3i> pendingRegenerationQueue = new ConcurrentLinkedQueue<>();

    private final @NotNull Set<Vector3i> pendingRegenerationSet = ConcurrentHashMap.newKeySet();

    private transient @EffectivelyNotNull ExecutorService chunkGenerationExecutor;

    private final @NotNull SerializableObject chunkLock = new SerializableObject();

    private final Map<Vector3i, ChunkSaveData> chunkSaveMap = new ConcurrentHashMap<>();

    private final @NotNull Map<Pair<Integer, Class<? extends Entity>>, Entity> entities = new ConcurrentHashMap<>();

    private final float spawnCycleTimerStart = 10.0f;
    private float spawnCycleTimer;

    private World() { }

    @Override
    public void initialize()
    {
        generatingChunks.clear();
        chunkGenerationExecutor = Executors.newFixedThreadPool(3);

        if (seed == -1)
            seed = new Random().nextLong(9999999);

        spawnCycleTimer = spawnCycleTimerStart;
    }

    @Override
    public void onUnload()
    {
        chunkSaveMap.clear();
    }

    @Override
    public void update()
    {
        loadCloseChunks();
        unloadFarChunks();
        processPendingRegeneration();

        if (spawnCycleTimer < 0 && entities.size() < 20 && chunkLoader != null)
        {
            for (int r = 0; r < 20 - entities.size(); r++)
            {
                Random random = new Random();

                Vector3f basePosition = new Vector3f(random.nextFloat() * 50, 0, random.nextFloat() * 50);

                basePosition.add(chunkLoader.getWorldPosition());

                Vector3f spawnPosition = findSpawnPosition(basePosition);

                spawnPosition.y += 2;

                regionalSpawners.forEach(spawner -> spawner.onSpawnCycle(this, spawnPosition));
            }

            spawnCycleTimer = spawnCycleTimerStart;
        }

        spawnCycleTimer -= Time.getDeltaTime();
    }

    public @NotNull Chunk loadOrGenerateChunk(@NotNull Vector3i chunkPosition)
    {
        if (chunkSaveMap.containsKey(chunkPosition))
        {
            ChunkSaveData saved = chunkSaveMap.get(chunkPosition);

            String chunkName = "default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z;
            GameObject object = GameObject.create(chunkName, Layer.DEFAULT);

            object.getTransform().setLocalPosition(CoordinateHelper.chunkToWorldCoordinates(chunkPosition));

            object.addComponent(Collider.create(VoxelMeshCollider.class));
            object.addComponent(Objects.requireNonNull(ShaderManager.get("pass.geometry")));
            object.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));
            object.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

            Chunk chunk = Chunk.create(saved.blocks());
            object.addComponent(chunk);

            chunk.generate();
            chunk.setModified(false);

            for (EntitySaveData eData : saved.entities())
            {
                Entity entity = Entity.create(eData.type());
                entity.setId(eData.id());

                String entityName = "default." + eData.type().getName() + "_" + eData.id();
                GameObject entityObject = GameObject.create(entityName, Layer.DEFAULT);

                entityObject.getTransform().setLocalPosition(eData.position());
                entityObject.setActive(true);

                entityObject.addComponent(entity);
                entity.onSpawned(this);

                entities.put(new Pair<>(eData.id(), eData.type()), entity);
            }

            return chunk;
        }
        else
            return generateChunk(chunkPosition);
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

        Chunk chunk = getChunk(chunkPosition);

        if (chunk == null)
        {
            loadedChunks.remove(chunkPosition);

            return;
        }

        List<Entity> entitiesInChunk = new ArrayList<>();

        for (Entity entity : entities.values())
        {
            Vector3i entityChunkPosition = CoordinateHelper.worldToChunkCoordinates(entity.getGameObject().getTransform().getWorldPosition());

            if (entityChunkPosition.equals(chunkPosition))
                entitiesInChunk.add(entity);
        }

        if (chunk.isModified() || !entitiesInChunk.isEmpty())
        {
            short[][][] blocks = chunk.getBlocks();
            List<EntitySaveData> entityDataList = new ArrayList<>();

            for (Entity entity : entitiesInChunk)
            {
                entity.getGameObject().getTransform().translate(new Vector3f(0, 0.1f, 0));
                entity.getGameObject().setActive(false);

                entityDataList.add(new EntitySaveData(
                        entity.getId(),
                        entity.getClass(),
                        entity.getGameObject().getTransform().getWorldPosition()
                ));

                GameObjectManager.unregister(entity.getGameObject().getName(), true);

                entities.remove(new Pair<Integer, Class<? extends Entity>>(entity.getId(), entity.getClass()));
            }

            chunkSaveMap.put(chunkPosition, new ChunkSaveData(blocks, entityDataList));

            chunk.setModified(false);
        }

        String chunkName = "default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z;
        GameObjectManager.unregister(chunkName, true);

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
            Chunk chunk = getChunk(chunkCoordinates);

            if (chunk == null)
                return -1;
            else
                return chunk.getBlock(blockCoordinates);
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
            Chunk chunk = getChunk(chunkCoordinates);

            if (chunk == null)
                return -1;
            else
                return chunk.getBlock(blockPosition);
        }
    }

    public @Nullable Chunk getChunk(@NotNull Vector3i chunkPosition)
    {
        String chunkName = "default.chunk_" + chunkPosition.x + "_" + chunkPosition.y + "_" + chunkPosition.z;

        if (GameObjectManager.has(chunkName) && loadedChunks.contains(chunkPosition))
            return Objects.requireNonNull(GameObjectManager.get(chunkName)).getComponent(Chunk.class);
        else
            return null;
    }

    public <T extends Entity> int spawnEntity(@NotNull Vector3f position, @NotNull Class<T> type)
    {
        Entity entity = Entity.create(type);

        int randomNumber = new Random().nextInt(4096);

        entity.setId(randomNumber);

        GameObject object = GameObject.create("default." + type.getName() + "_" + randomNumber, Layer.DEFAULT);

        object.getTransform().setLocalPosition(position);
        object.addComponent(Collider.create(BoxCollider.class).setSize(entity.getBoundingBoxSize()));
        object.addComponent(Rigidbody.create());
        object.addComponent(entity);

        entity.onSpawned(this);

        entities.putIfAbsent(new Pair<>(randomNumber, type), entity);

        return randomNumber;
    }

    public <T extends Entity> @Nullable Entity getEntity(int id, @NotNull Class<T> type)
    {
        if (!entities.containsKey(new Pair<Integer, Class<? extends Entity>>(id, type)))
        {
            System.err.println("Couldn't find entity: '" + id + "' with type: '" + type + "'!");
            return null;
        }

        return entities.get(new Pair<Integer, Class<? extends Entity>>(id, type));
    }

    public <T extends Entity> void killEntity(@NotNull Entity killer, int id, @NotNull Class<T> type)
    {
        if (!entities.containsKey(new Pair<Integer, Class<? extends Entity>>(id, type)))
        {
            System.err.println("Couldn't find entity: '" + id + "' with type: '" + type + "'!");
            return;
        }

        Entity entity = entities.get(new Pair<Integer, Class<? extends Entity>>(id, type));

        entity.onKilled(this, killer);

        entities.remove(new Pair<Integer, Class<? extends Entity>>(id, type));

        GameObjectManager.unregister(entity.getGameObject().getName(), true);
    }


    public <T extends Entity> void reregisterEntity(int id, @NotNull Class<? extends Entity> type, @NotNull T entity, @NotNull GameObject gameObject)
    {
        entities.putIfAbsent(new Pair<>(id, type), entity);

        entities.get(new Pair<Integer, Class<? extends Entity>>(id, type)).setGameObject(gameObject);
    }

    public static @Nullable World getLocalWorld()
    {
        if (!GameObjectManager.has("default.world"))
            return null;

        return Objects.requireNonNull(GameObjectManager.get("default.world")).getComponent(World.class);
    }

    public void addTerrainGenerator(@NotNull TerrainGenerator generator)
    {
        terrainGenerators.add(generator);
    }

    public void addRegionalSpawner(@NotNull RegionalSpawner spawner)
    {
        regionalSpawners.add(spawner);
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
                    loadOrGenerateChunk(currentChunk);

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

    private @NotNull Vector3f findSpawnPosition(@NotNull Vector3f basePosition)
    {
        float worldX = basePosition.x;
        float worldZ = basePosition.z;

        int chunkX = (int)Math.floor(worldX / Chunk.SIZE);
        int chunkZ = (int)Math.floor(worldZ / Chunk.SIZE);

        Vector3f spawnPos = null;

        for (int cy = VERTICAL_CHUNKS - 1; cy >= 0 && spawnPos == null; cy--)
        {
            Vector3i candidateChunkPos = new Vector3i(chunkX, cy, chunkZ);
            Chunk candidateChunk = getChunk(candidateChunkPos);

            if (candidateChunk == null)
                continue;

            short[][][] blocks = candidateChunk.getBlocks();

            int localX = (int)(worldX - chunkX * Chunk.SIZE);
            int localZ = (int)(worldZ - chunkZ * Chunk.SIZE);

            for (int ly = Chunk.SIZE - 1; ly >= 0; ly--)
            {
                if (blocks[localX][ly][localZ] != 0)
                {
                    float spawnY = cy * Chunk.SIZE + ly + 1;
                    spawnPos = new Vector3f(worldX, spawnY, worldZ);

                    break;
                }
            }
        }

        if (spawnPos == null)
            spawnPos = new Vector3f(worldX, 100, worldZ);

        return spawnPos;
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

    public record ChunkSaveData(short[][][] blocks, List<EntitySaveData> entities) { }

    public record EntitySaveData(int id, Class<? extends Entity> type, Vector3f position) { }
}