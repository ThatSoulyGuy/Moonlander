package com.thatsoulyguy.moonlander.system;

import com.thatsoulyguy.moonlander.annotation.Manager;
import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.gameplay.OxygenBubble;
import com.thatsoulyguy.moonlander.render.*;
import com.thatsoulyguy.moonlander.render.advanced.RenderPassManager;
import com.thatsoulyguy.moonlander.render.advanced.core.RenderPass;
import com.thatsoulyguy.moonlander.render.advanced.core.renderpasses.GeometryRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.core.renderpasses.LevelRenderPass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL41;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Static
@Manager(GameObject.class)
public class GameObjectManager
{
    private static final @NotNull ConcurrentMap<String, GameObject> gameObjectMap = new ConcurrentHashMap<>();
    private static final @NotNull ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final @NotNull BlockingQueue<GameObject> uninitializeQueue = new LinkedBlockingQueue<>();
    private static final @NotNull AtomicBoolean isUpdating = new AtomicBoolean(false);

    private GameObjectManager() { }

    public static void register(@NotNull GameObject object)
    {
        gameObjectMap.putIfAbsent(object.getName(), object);

        if (LevelManager.getCurrentLevel() != null)
            LevelManager.getCurrentLevel().addGameObject(object.getName());
    }

    public static void unregister(@NotNull String name)
    {
        unregister(name, false);
    }

    public static void unregister(@NotNull String name, boolean uninitialize)
    {
        GameObject gameObject = gameObjectMap.remove(name);

        if (gameObject == null)
            return;

        if (LevelManager.getCurrentLevel() != null)
            LevelManager.getCurrentLevel().removeGameObject(name);

        if (uninitialize)
            uninitializeQueue.add(gameObject);
    }

    public static @Nullable GameObject get(@NotNull String name)
    {
        return gameObjectMap.getOrDefault(name, null);
    }

    public static @NotNull List<GameObject> getAll()
    {
        return List.copyOf(gameObjectMap.values());
    }

    public static void loadAll()
    {
        try
        {
            gameObjectMap.values().forEach(GameObject::onLoad);
        }
        catch (Exception e)
        {
            System.err.println("Failed to load all game objects' components due to exception: " + e);
        }
    }

    public static void stop()
    {
        gameObjectMap.values().forEach(GameObject::uninitialize);
        gameObjectMap.clear();
    }

    public static void updateMainThread()
    {
        isUpdating.set(true);

        gameObjectMap.values().forEach(gameObject ->
        {
            if (gameObject.isActive())
                gameObject.updateMainThread();
        });

        isUpdating.set(false);

        processUninitializeQueue();
    }

    public static void update()
    {
        isUpdating.set(true);

        CountDownLatch latch = new CountDownLatch(gameObjectMap.size());

        for (GameObject gameObject : gameObjectMap.values())
        {
            executor.submit(() ->
            {
                try
                {
                    if (gameObject.isActive())
                        gameObject.update();
                }
                catch (Exception e)
                {
                    System.err.println("Error in GameObject update task: " + e.getMessage());
                }
                finally
                {
                    latch.countDown();
                }
            });
        }

        try
        {
            boolean completed = latch.await(1, TimeUnit.SECONDS);

            if (!completed)
                System.err.println("Some update tasks timed out.");

        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            System.err.println("Update tasks interrupted: " + e.getMessage());
        }

        isUpdating.set(false);

        processUninitializeQueue();
    }

    public static void renderDefault(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        if (Settings.USE_ADVANCED_RENDERING_FEATURES.getValue())
        {
            GeometryRenderPass geometryRenderPass = null;

            if (RenderPassManager.has(GeometryRenderPass.class))
                geometryRenderPass = (GeometryRenderPass) Objects.requireNonNull(RenderPassManager.get(GeometryRenderPass.class));

            if (geometryRenderPass != null)
                geometryRenderPass.render(camera);

            if (Skybox.CURRENT_SKYBOX != null)
                Skybox.CURRENT_SKYBOX.render(camera);

            List<GameObject> transparentGameObjects = new ArrayList<>();
            List<GameObject> inFrontGameObjects = new ArrayList<>();

            for (GameObject gameObject : gameObjectMap.values())
            {
                Mesh mesh = gameObject.getComponentRecursive(Mesh.class);

                boolean hasMesh = (mesh != null);
                boolean isInFront = hasMesh && mesh.isInFront();
                boolean isTransparent = hasMesh && mesh.isTransparent();
                boolean hasOxygenBubble = gameObject.hasComponentRecursive(OxygenBubble.class);

                if (isInFront)
                    inFrontGameObjects.add(gameObject);
                else if (isTransparent || hasOxygenBubble)
                    transparentGameObjects.add(gameObject);
                else
                    gameObject.renderDefault(camera);
            }

            transparentGameObjects.sort((aObject, bObject) ->
            {
                float dist1 = aObject.getTransform().getWorldPosition().distanceSquared(camera.getGameObject().getTransform().getWorldPosition());
                float dist2 = bObject.getTransform().getWorldPosition().distanceSquared(camera.getGameObject().getTransform().getWorldPosition());

                return Float.compare(dist1, dist2);
            });

            for (GameObject gameObject : transparentGameObjects)
                gameObject.renderDefault(camera);

            if (geometryRenderPass != null)
                geometryRenderPass.endRender();

            List<RenderPass> passList = RenderPassManager.getAll().stream()
                    .filter(pass -> !(pass instanceof LevelRenderPass))
                    .filter(pass -> !(pass instanceof GeometryRenderPass))
                    .toList();

            passList.forEach(pass -> pass.render(camera));

            for (GameObject gameObject : inFrontGameObjects)
                gameObject.renderDefault(camera);

            int error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error (GameObjectManager::renderDefault): " + error);
        }
        else
        {
            if (Skybox.CURRENT_SKYBOX != null)
                Skybox.CURRENT_SKYBOX.render(camera);

            gameObjectMap.values().forEach(gameObject -> gameObject.renderDefault(camera));

            DebugRenderer.render(camera);

            int error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error (GameObjectManager::renderDefault): " + error);
        }
    }

    public static void renderUI()
    {
        GL41.glDisable(GL41.GL_DEPTH_TEST);

        for (GameObject gameObject : gameObjectMap.values())
            gameObject.renderUI();

        GL41.glEnable(GL41.GL_DEPTH_TEST);
    }

    public static boolean has(@NotNull String name)
    {
        return gameObjectMap.containsKey(name);
    }

    private static void processUninitializeQueue()
    {
        while (!uninitializeQueue.isEmpty())
        {
            GameObject gameObject = uninitializeQueue.poll();

            if (gameObject != null)
                gameObject.uninitialize();
        }
    }

    public static void uninitialize()
    {
        executor.shutdown();

        try
        {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES))
            {
                executor.shutdownNow();

                if (!executor.awaitTermination(1, TimeUnit.MINUTES))
                    System.err.println("Executor did not terminate.");
            }
        }
        catch (InterruptedException e)
        {
            executor.shutdownNow();

            Thread.currentThread().interrupt();
            System.err.println("GameObject update tasks were interrupted.");
        }

        for (GameObject gameObject : gameObjectMap.values())
            gameObject.uninitialize();

        gameObjectMap.clear();
    }
}