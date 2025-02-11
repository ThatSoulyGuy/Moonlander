package com.thatsoulyguy.moonlander;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.colliders.BoxCollider;
import com.thatsoulyguy.moonlander.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.moonlander.collider.handler.CollisionHandlerManager;
import com.thatsoulyguy.moonlander.collider.handler.CollisionResult;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.crafting.CraftingRecipeRegistry;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.input.InputManager;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.math.Rigidbody;
import com.thatsoulyguy.moonlander.render.*;
import com.thatsoulyguy.moonlander.render.advanced.RenderPassManager;
import com.thatsoulyguy.moonlander.render.advanced.core.renderpasses.GeometryRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.core.renderpasses.LevelRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses.SSAOBlurRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses.SSAOConcludingRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses.SSAORenderPass;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.system.LevelManager;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import com.thatsoulyguy.moonlander.ui.Menu;
import com.thatsoulyguy.moonlander.ui.MenuManager;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.menus.*;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.FileHelper;
import com.thatsoulyguy.moonlander.world.TerrainGenerator;
import com.thatsoulyguy.moonlander.world.TextureAtlas;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import com.thatsoulyguy.moonlander.world.World;
import com.thatsoulyguy.moonlander.world.terraingenerators.CaveTerrainGenerator;
import com.thatsoulyguy.moonlander.world.terraingenerators.GroundTerrainGenerator;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Moonlander
{
    private @EffectivelyNotNull Camera camera;

    public void preInitialize()
    {
        InputManager.initialize();

        if(!GLFW.glfwInit())
            throw new IllegalStateException("Failed to initialize GLFW");

        GLFW.glfwSetErrorCallback((error, description) -> System.err.println("GLFW Error " + error + ": " + GLFWErrorCallback.getDescription(description)));

        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();

        if (primaryMonitor == 0L)
            throw new RuntimeException("Failed to get primary monitor");

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(primaryMonitor);

        if (vidMode == null)
            throw new RuntimeException("Failed to get video mode");

        int windowWidth = vidMode.width();
        int windowHeight = vidMode.height();

        Vector2i windowSize = new Vector2i(windowWidth / 2, windowHeight / 2);

        MainThreadExecutor.initialize();

        Window.initialize("Moonlander* 1.38.10", windowSize);

        DebugRenderer.initialize();

        ShaderManager.register(Shader.create("legacy.default", AssetPath.create("moonlander", "shader/legacy/default")));
        ShaderManager.register(Shader.create("ui", AssetPath.create("moonlander", "shader/ui")));
        ShaderManager.register(Shader.create("pass.passthrough", AssetPath.create("moonlander", "shader/pass/passthrough")));
        ShaderManager.register(Shader.create("pass.geometry", AssetPath.create("moonlander", "shader/pass/geometry")));
        ShaderManager.register(Shader.create("ssao.default", AssetPath.create("moonlander", "shader/ssao/default")));
        ShaderManager.register(Shader.create("ssao.blur", AssetPath.create("moonlander", "shader/ssao/blur")));
        ShaderManager.register(Shader.create("ssao.conclusion", AssetPath.create("moonlander", "shader/ssao/conclusion")));
        ShaderManager.register(Shader.create("skybox", AssetPath.create("moonlander", "shader/skybox")));
        ShaderManager.register(Shader.create("selector_box", AssetPath.create("moonlander", "shader/selectorBox")));
        ShaderManager.register(Shader.create("oxygen_bubble", AssetPath.create("moonlander", "shader/oxygenBubble")));

        TextureManager.register(Texture.create("debug", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/debug.png")));
        TextureManager.register(Texture.create("error", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/error.png")));
        TextureManager.register(Texture.create("ui.hotbar", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/hotbar.png")));
        TextureManager.register(Texture.create("ui.hotbar_selector", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/hotbar_selector.png")));
        TextureManager.register(Texture.create("ui.transparency", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/transparency.png")));
        TextureManager.register(Texture.create("ui.background", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/background.png")));
        TextureManager.register(Texture.create("ui.death_red", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/death_red.png")));
        TextureManager.register(Texture.create("ui.button_default", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/button_default.png")));
        TextureManager.register(Texture.create("ui.button_disabled", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/button_disabled.png")));
        TextureManager.register(Texture.create("ui.button_selected", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/button_selected.png")));
        TextureManager.register(Texture.create("ui.menu.survival_inventory", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/inventory_survival.png")));
        TextureManager.register(Texture.create("ui.menu.compositor", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/compositor.png")));
        TextureManager.register(Texture.create("ui.menu.crafting_table", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/crafting_table.png")));
        TextureManager.register(Texture.create("ui.menu.slot_darken", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/slot_darken.png")));
        TextureManager.register(Texture.create("ui.menu.empty_heart", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/empty_heart.png")));
        TextureManager.register(Texture.create("ui.menu.full_heart", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/full_heart.png")));
        TextureManager.register(Texture.create("ui.menu.half_heart", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/half_heart.png")));
        TextureManager.register(Texture.create("ui.menu.oxygen_dial", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/oxygen_dial.png")));
        TextureManager.register(Texture.create("ui.menu.book", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/book.png")));
        TextureManager.register(Texture.create("ui.menu.next_page", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/next_page.png")));
        TextureManager.register(Texture.create("ui.menu.previous_page", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/previous_page.png")));
        TextureManager.register(Texture.create("ui.menu.next_page_deactivated", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/next_page_deactivated.png")));
        TextureManager.register(Texture.create("ui.menu.previous_page_deactivated", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/previous_page_deactivated.png")));
        TextureManager.register(Texture.create("ui.menu.oxygen_dial_ball", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/oxygen_dial_ball.png")));
        TextureManager.register(Texture.create("ui.menu.oxygen_pointer", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/oxygen_pointer.png")));
        TextureManager.register(Texture.create("ui.block_lookover", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/block_lookover.png")));

        TextureAtlasManager.register(TextureAtlas.create("blocks", AssetPath.create("moonlander", "texture/block/")));
        TextureAtlasManager.register(TextureAtlas.create("items", AssetPath.create("moonlander", "texture/item/")));

        MenuManager.register(Menu.create(BookMenu.class));
        MenuManager.register(Menu.create(CompositorMenu.class));
        MenuManager.register(Menu.create(CraftingTableMenu.class));
        MenuManager.register(Menu.create(InventoryMenu.class));
        MenuManager.register(Menu.create(PauseMenu.class));
        MenuManager.register(Menu.create(DeathMenu.class));

        Skybox.CURRENT_SKYBOX = Skybox.create(Texture.createCubeMap("skybox", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, List.of
        (
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/earth-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png")
        )));


        LevelRenderPass levelRenderPass = new LevelRenderPass();

        RenderPassManager.register(levelRenderPass);

        GeometryRenderPass geometryPass = new GeometryRenderPass();

        RenderPassManager.register(geometryPass);


        SSAORenderPass ssaoPass = new SSAORenderPass(
                geometryPass.getPositionTex(),
                geometryPass.getNormalTex()
        );

        RenderPassManager.register(ssaoPass);


        SSAOBlurRenderPass ssaoBlurPass = new SSAOBlurRenderPass(ssaoPass.getSSAOColor());

        RenderPassManager.register(ssaoBlurPass);


        SSAOConcludingRenderPass concludingPass = new SSAOConcludingRenderPass(
                geometryPass.getPositionTex(),
                geometryPass.getNormalTex(),
                geometryPass.getAlbedoTex(),
                ssaoBlurPass.getBlurredSSAO()
        );

        RenderPassManager.register(concludingPass);


        Settings.initialize();

        BlockRegistry.initialize();
        ItemRegistry.initialize();
        CraftingRecipeRegistry.initialize();

        registerCollisionHandlers();

        InputManager.update();

        UIManager.initialize();

        Time.reset();
    }

    public void initialize()
    {
        //LevelManager.loadLevel(FileHelper.getPersistentDataPath("Invasion2") + "/overworld", true);

        //*
        LevelManager.createLevel("overworld", true);

        GameObject player = GameObject.create("default.player", Layer.DEFAULT);

        player.getTransform().setLocalPosition(new Vector3f(0.0f, 180.0f, 0.0f));

        player.addComponent(Collider.create(BoxCollider.class).setSize(new Vector3f(0.65f, 1.89f, 0.65f)));
        player.addComponent(Rigidbody.create());
        player.addComponent(Entity.create(EntityPlayer.class));

        GameObject overworld = GameObject.create("default.world", Layer.DEFAULT);

        overworld.addComponent(World.create("overworld"));

        World world = overworld.getComponentNotNull(World.class);

        world.addTerrainGenerator(TerrainGenerator.create(GroundTerrainGenerator.class));
        world.addTerrainGenerator(TerrainGenerator.create(CaveTerrainGenerator.class));
        //*/

        camera = Objects.requireNonNull(GameObjectManager.get("default.player")).getComponentNotNull(EntityPlayer.class).getCamera();
    }

    public void update()
    {
        Time.update();

        World.getLocalWorld().chunkLoader = Objects.requireNonNull(GameObjectManager.get("default.player")).getTransform();

        GameObjectManager.updateMainThread();
        GameObjectManager.update();
        UIManager.update();

        MainThreadExecutor.execute();

        InputManager.update();
    }

    public void render()
    {
        Window.preRender();

        GameObjectManager.renderDefault(camera);

        GameObjectManager.renderUI();

        Window.postRender();
    }

    private void registerCollisionHandlers()
    {
        CollisionHandlerManager.register(BoxCollider.class, BoxCollider.class, (a, b, selfIsMovable) ->
        {
            BoxCollider boxA = (BoxCollider) a;
            BoxCollider boxB = (BoxCollider) b;

            Vector3f posA = boxA.getPosition();
            Vector3f sizeA = boxA.getSize();
            Vector3f posB = boxB.getPosition();
            Vector3f sizeB = boxB.getSize();

            Vector3f minA = new Vector3f(posA).sub(new Vector3f(sizeA).mul(0.5f));
            Vector3f maxA = new Vector3f(posA).add(new Vector3f(sizeA).mul(0.5f));

            Vector3f minB = new Vector3f(posB).sub(new Vector3f(sizeB).mul(0.5f));
            Vector3f maxB = new Vector3f(posB).add(new Vector3f(sizeB).mul(0.5f));

            boolean intersects = Collider.intersectsGeneric(minA, maxA, minB, maxB);

            Vector3f resolution = new Vector3f();

            if (intersects)
            {
                float overlapX = Math.min(maxA.x - minB.x, maxB.x - minA.x);
                float overlapY = Math.min(maxA.y - minB.y, maxB.y - minA.y);
                float overlapZ = Math.min(maxA.z - minB.z, maxB.z - minA.z);

                float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));

                if (minOverlap == overlapX)
                    resolution.x = (posA.x < posB.x) ? -overlapX : overlapX;
                else if (minOverlap == overlapY)
                    resolution.y = (posA.y < posB.y) ? -overlapY : overlapY;
                else
                    resolution.z = (posA.z < posB.z) ? -overlapZ : overlapZ;

                if (!selfIsMovable)
                    resolution.negate();
            }

            return new CollisionResult(intersects, resolution);
        });

        CollisionHandlerManager.register(BoxCollider.class, VoxelMeshCollider.class, (a, b, selfIsMovable) ->
        {
            BoxCollider box = (BoxCollider) a;
            VoxelMeshCollider voxelMesh = (VoxelMeshCollider) b;

            Vector3f totalResolution = new Vector3f();

            final int MAX_ITERATIONS = 10;

            for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++)
            {
                Vector3f resolution = Collider.resolveAllCollisions(box, voxelMesh);

                if (resolution.length() < 0.00001f)
                    break;

                totalResolution.add(resolution);
            }

            boolean collided = totalResolution.length() > 0.00001f;

            return new CollisionResult(collided, totalResolution);
        });

        CollisionHandlerManager.register(VoxelMeshCollider.class, VoxelMeshCollider.class, (a, b, selfIsMovable) -> //TODO: Performance-intensive; Optimize this
        {
            VoxelMeshCollider voxelMeshA = (VoxelMeshCollider) a;
            VoxelMeshCollider voxelMeshB = (VoxelMeshCollider) b;

            Vector3f posA = voxelMeshA.getPosition();
            Vector3f posB = voxelMeshB.getPosition();

            Vector3f totalResolution = new Vector3f();
            boolean intersects = false;

            for (Vector3f voxelA : voxelMeshA.getVoxels())
            {
                Vector3f voxelWorldPosA = new Vector3f(posA).add(voxelA);
                Vector3f voxelMinA = new Vector3f(voxelWorldPosA).sub(0.5f, 0.5f, 0.5f);
                Vector3f voxelMaxA = new Vector3f(voxelWorldPosA).add(0.5f, 0.5f, 0.5f);

                for (Vector3f voxelB : voxelMeshB.getVoxels())
                {
                    Vector3f voxelWorldPosB = new Vector3f(posB).add(voxelB);
                    Vector3f voxelMinB = new Vector3f(voxelWorldPosB).sub(0.5f, 0.5f, 0.5f);
                    Vector3f voxelMaxB = new Vector3f(voxelWorldPosB).add(0.5f, 0.5f, 0.5f);

                    boolean voxelIntersects = Collider.intersectsGeneric(voxelMinA, voxelMaxA, voxelMinB, voxelMaxB);

                    if (voxelIntersects)
                    {
                        intersects = true;

                        float overlapX = Math.min(voxelMaxA.x - voxelMinB.x, voxelMaxB.x - voxelMinA.x);
                        float overlapY = Math.min(voxelMaxA.y - voxelMinB.y, voxelMaxB.y - voxelMinA.y);
                        float overlapZ = Math.min(voxelMaxA.z - voxelMinB.z, voxelMaxB.z - voxelMinA.z);

                        float minOverlap = Math.min(overlapX, Math.min(overlapY, overlapZ));

                        Vector3f resolution = new Vector3f();

                        if (minOverlap == overlapX)
                            resolution.x = (posA.x < posB.x) ? -overlapX : overlapX;
                        else if (minOverlap == overlapY)
                            resolution.y = (posA.y < posB.y) ? -overlapY : overlapY;
                        else
                            resolution.z = (posA.z < posB.z) ? -overlapZ : overlapZ;

                        totalResolution.add(resolution);
                    }
                }
            }

            if (intersects)
            {
                int intersectCount = voxelMeshA.getVoxels().size() * voxelMeshB.getVoxels().size();
                totalResolution.mul(1.0f / intersectCount);

                if (!selfIsMovable)
                    totalResolution.negate();
            }

            return new CollisionResult(intersects, totalResolution);
        });
    }

    public void uninitialize()
    {
        LevelManager.saveLevel("overworld", FileHelper.getPersistentDataPath("Invasion2"));

        if (Skybox.CURRENT_SKYBOX != null)
            Skybox.CURRENT_SKYBOX.uninitialize();

        GameObjectManager.uninitialize();

        RenderPassManager.uninitialize();

        InputManager.uninitialize();

        ShaderManager.uninitialize();
        TextureManager.uninitialize();

        DebugRenderer.uninitialize();

        Window.uninitialize();
    }

    public static void main(String[] args)
    {
        try
        {
            Moonlander instantiation = new Moonlander();

            instantiation.preInitialize();
            instantiation.initialize();

            while (!Window.shouldClose())
            {
                instantiation.update();
                instantiation.render();

                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException exception)
                {
                    throw new RuntimeException(exception);
                }
            }

            instantiation.uninitialize();
        }
        catch (Exception exception)
        {
            String stackTrace = Arrays.stream(exception.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));

            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage() + "\n\n" + stackTrace,
                    "Exception!",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(-1);
        }
    }
}