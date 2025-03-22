package com.thatsoulyguy.moonlander;

import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioManager;
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
import com.thatsoulyguy.moonlander.input.InputManager;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.render.*;
import com.thatsoulyguy.moonlander.render.advanced.RenderPassManager;
import com.thatsoulyguy.moonlander.render.advanced.core.renderpasses.GeometryRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.core.renderpasses.LevelRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses.SSAOBlurRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses.SSAOConcludingRenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses.SSAORenderPass;
import com.thatsoulyguy.moonlander.system.*;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.FileHelper;
import com.thatsoulyguy.moonlander.util.AudioData;
import com.thatsoulyguy.moonlander.world.TextureAtlas;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import com.thatsoulyguy.moonlander.world.World;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Moonlander
{
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

        Window.initialize("Moonlander* 1.62.18", windowSize);

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
        TextureManager.register(Texture.create("entity.rocket", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/entity/rocket.png")));
        TextureManager.register(Texture.create("entity.damage", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/entity/damage.png")));
        TextureManager.register(Texture.create("entity.alien", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/entity/alien.png")));
        TextureManager.register(Texture.create("entity.alien_damage", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/entity/alien_damage.png")));
        TextureManager.register(Texture.create("entity.player_arm", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/entity/player_arm.png")));
        TextureManager.register(Texture.create("ui.hotbar", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/hotbar.png")));
        TextureManager.register(Texture.create("ui.hotbar_selector", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/hotbar_selector.png")));
        TextureManager.register(Texture.create("ui.transparency", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/transparency.png")));
        TextureManager.register(Texture.create("ui.crosshair", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/crosshair.png")));
        TextureManager.register(Texture.create("ui.background", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/background.png")));
        TextureManager.register(Texture.create("ui.death_red", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/death_red.png")));
        TextureManager.register(Texture.create("ui.win_green", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/win_green.png")));
        TextureManager.register(Texture.create("ui.button_default", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/button_default.png")));
        TextureManager.register(Texture.create("ui.button_disabled", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/button_disabled.png")));
        TextureManager.register(Texture.create("ui.button_selected", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, AssetPath.create("moonlander", "texture/ui/button_selected.png")));
        TextureManager.register(Texture.create("ui.menu.survival_inventory", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/inventory_survival.png")));
        TextureManager.register(Texture.create("ui.menu.survival_crafting", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/survival_crafting.png")));
        TextureManager.register(Texture.create("ui.menu.creative_crafting", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/creative_crafting.png")));
        TextureManager.register(Texture.create("ui.menu.compositor", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/compositor.png")));
        TextureManager.register(Texture.create("ui.menu.furnace", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/furnace.png")));
        TextureManager.register(Texture.create("ui.menu.crafting_table", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/crafting_table.png")));
        TextureManager.register(Texture.create("ui.menu.slot_darken", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/slot_darken.png")));
        TextureManager.register(Texture.create("ui.menu.empty_heart", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/empty_heart.png")));
        TextureManager.register(Texture.create("ui.menu.full_heart", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/full_heart.png")));
        TextureManager.register(Texture.create("ui.menu.half_heart", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/half_heart.png")));
        TextureManager.register(Texture.create("ui.menu.bubble", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/bubble.png")));
        TextureManager.register(Texture.create("ui.menu.bubble_half", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/bubble_half.png")));
        TextureManager.register(Texture.create("ui.menu.oxygen_dial", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/oxygen_dial.png")));
        TextureManager.register(Texture.create("ui.menu.book", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/book.png")));
        TextureManager.register(Texture.create("ui.menu.next_page", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/next_page.png")));
        TextureManager.register(Texture.create("ui.menu.previous_page", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/previous_page.png")));
        TextureManager.register(Texture.create("ui.menu.next_page_selected", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/next_page_selected.png")));
        TextureManager.register(Texture.create("ui.menu.previous_page_selected", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/previous_page_selected.png")));
        TextureManager.register(Texture.create("ui.menu.next_page_deactivated", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/next_page_deactivated.png")));
        TextureManager.register(Texture.create("ui.menu.previous_page_deactivated", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/previous_page_deactivated.png")));
        TextureManager.register(Texture.create("ui.menu.oxygen_dial_ball", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/oxygen_dial_ball.png")));
        TextureManager.register(Texture.create("ui.menu.oxygen_pointer", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/oxygen_pointer.png")));
        TextureManager.register(Texture.create("ui.block_lookover", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/block_lookover.png")));
        TextureManager.register(Texture.create("ui.title", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/title.png")));
        TextureManager.register(Texture.create("ui.menu.main_menu_background", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/menu/main_menu_background.png")));
        TextureManager.register(Texture.create("ui.usage.fist", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/usage_fist.png")));
        TextureManager.register(Texture.create("ui.usage.pickaxe", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/usage_pickaxe.png")));
        TextureManager.register(Texture.create("ui.usage.sword", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, false, AssetPath.create("moonlander", "texture/ui/usage_sword.png")));

        TextureAtlasManager.register(TextureAtlas.create("blocks", AssetPath.create("moonlander", "texture/block/")));
        TextureAtlasManager.register(TextureAtlas.create("items", AssetPath.create("moonlander", "texture/item/")));

        Skybox.CURRENT_SKYBOX = Skybox.create(Texture.createCubeMap("skybox", Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, List.of
        (
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/earth-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png"),
            AssetPath.create("moonlander", "texture/skybox/stars-panel.png")
        )));

        AudioManager.initialize();

        AudioManager.register(AudioClip.create("debug", AssetPath.create("moonlander", "audio/explosion.ogg")));

        AudioManager.register(AudioClip.create("player.step.stone.0", AssetPath.create("moonlander", "audio/player/step/stone1.ogg")));
        AudioManager.register(AudioClip.create("player.step.stone.1", AssetPath.create("moonlander", "audio/player/step/stone2.ogg")));
        AudioManager.register(AudioClip.create("player.step.stone.2", AssetPath.create("moonlander", "audio/player/step/stone3.ogg")));
        AudioManager.register(AudioClip.create("player.step.stone.3", AssetPath.create("moonlander", "audio/player/step/stone4.ogg")));
        AudioManager.register(AudioClip.create("player.step.stone.4", AssetPath.create("moonlander", "audio/player/step/stone5.ogg")));
        AudioManager.register(AudioClip.create("player.step.stone.5", AssetPath.create("moonlander", "audio/player/step/stone6.ogg")));
        AudioManager.register(AudioClip.create("block.break.stone.0", AssetPath.create("moonlander", "audio/block/break/stone1.ogg")));
        AudioManager.register(AudioClip.create("block.break.stone.1", AssetPath.create("moonlander", "audio/block/break/stone2.ogg")));
        AudioManager.register(AudioClip.create("block.break.stone.2", AssetPath.create("moonlander", "audio/block/break/stone3.ogg")));
        AudioManager.register(AudioClip.create("block.break.stone.3", AssetPath.create("moonlander", "audio/block/break/stone4.ogg")));
        AudioManager.register(AudioClip.create("block.mining.stone.0", AssetPath.create("moonlander", "audio/block/mining/stone1.ogg")));
        AudioManager.register(AudioClip.create("block.mining.stone.1", AssetPath.create("moonlander", "audio/block/mining/stone2.ogg")));
        AudioManager.register(AudioClip.create("block.mining.stone.2", AssetPath.create("moonlander", "audio/block/mining/stone3.ogg")));
        AudioManager.register(AudioClip.create("entity.damage.0", AssetPath.create("moonlander", "audio/entity/damage1.ogg")));
        AudioManager.register(AudioClip.create("entity.damage.1", AssetPath.create("moonlander", "audio/entity/damage2.ogg")));
        AudioManager.register(AudioClip.create("entity.damage.2", AssetPath.create("moonlander", "audio/entity/damage3.ogg")));
        AudioManager.register(AudioClip.create("entity.rocket_blowoff", AssetPath.create("moonlander", "audio/entity/rocket_blowoff.ogg")));
        AudioManager.register(AudioClip.create("entity.zombie.damage.0", AssetPath.create("moonlander", "audio/entity/zombie/damage1.ogg")));
        AudioManager.register(AudioClip.create("entity.zombie.damage.1", AssetPath.create("moonlander", "audio/entity/zombie/damage2.ogg")));
        AudioManager.register(AudioClip.create("entity.zombie.death", AssetPath.create("moonlander", "audio/entity/zombie/death.ogg")));
        AudioManager.register(AudioClip.create("ui.click", AssetPath.create("moonlander", "audio/ui/click.ogg")));

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

        Time.reset();
    }

    public void initialize()
    {
        Levels.createMainMenu();
    }

    public void update()
    {
        Time.update();

        if (World.getLocalWorld() != null && Camera.getLocalCamera() != null)
            World.getLocalWorld().chunkLoader = Camera.getLocalCamera().getGameObject().getTransform();

        Levels.update();

        GameObjectManager.updateMainThread();
        GameObjectManager.update();

        MainThreadExecutor.execute();

        InputManager.update();
    }

    public void render()
    {
        Window.preRender();

        GameObjectManager.renderDefault(Camera.getLocalCamera());

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
        if (LevelManager.hasLevel("overworld"))
            LevelManager.saveLevel("overworld", FileHelper.getPersistentDataPath("Moonlander"));

        if (Skybox.CURRENT_SKYBOX != null)
            Skybox.CURRENT_SKYBOX.uninitialize();

        GameObjectManager.uninitialize();

        AudioManager.uninitialize();
        AudioData.uninitialize();

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

            System.err.println(stackTrace);

            System.exit(-1);
        }
    }
}