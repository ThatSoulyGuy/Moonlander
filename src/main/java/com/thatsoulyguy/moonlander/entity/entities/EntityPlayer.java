package com.thatsoulyguy.moonlander.entity.entities;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.colliders.BoxCollider;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.input.*;
import com.thatsoulyguy.moonlander.item.Inventory;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.item.Tool;
import com.thatsoulyguy.moonlander.math.Raycast;
import com.thatsoulyguy.moonlander.math.Rigidbody;
import com.thatsoulyguy.moonlander.render.*;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import com.thatsoulyguy.moonlander.ui.MenuManager;
import com.thatsoulyguy.moonlander.ui.menus.CraftingTableMenu;
import com.thatsoulyguy.moonlander.ui.menus.InventoryMenu;
import com.thatsoulyguy.moonlander.ui.menus.PauseMenu;
import com.thatsoulyguy.moonlander.util.CoordinateHelper;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.io.Serializable;
import java.lang.Math;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityPlayer extends Entity
{
    private @EffectivelyNotNull Camera camera;

    private @EffectivelyNotNull Mesh blockBreakageMesh;

    private @EffectivelyNotNull SelectorMesh selectorMesh;

    private @EffectivelyNotNull Vector3i breakingBlockCoordinates;

    private @EffectivelyNotNull PauseMenu pauseMenu;
    private @EffectivelyNotNull InventoryMenu inventoryMenu;
    private @EffectivelyNotNull CraftingTableMenu craftingTableMenu;

    private @NotNull Inventory inventory = new Inventory();

    private float breakingProgress;

    private final float blockBreakCooldownTimerStart = 0.083f;
    private float blockBreakCooldownTimer;

    private final float jumpCooldownTimerStart = 0.18f;
    private float jumpCooldownTimer;

    @Override
    public void initialize()
    {
        super.initialize();

        initializeCamera();
        initializeUI();

        selectorMesh = SelectorMesh.create();

        GameObject blockBreakageObject = GameObject.create("block_breakage", Layer.DEFAULT);

        blockBreakageObject.getTransform().setLocalScale(new Vector3f(1.01f, 1.01f, 1.01f));

        blockBreakageObject.addComponent(Objects.requireNonNull(ShaderManager.get("pass.geometry")));
        blockBreakageObject.addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));

        Vector2f[] uvs = Objects.requireNonNull(TextureAtlasManager.get("blocks")).getSubTextureCoordinates("destroy_stage_0");

        blockBreakageObject.addComponent(Mesh.create(
            List.of
            (
                Vertex.create(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[0]),
                Vertex.create(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[1]),
                Vertex.create(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[2]),
                Vertex.create(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[3]),

                Vertex.create(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[0]),
                Vertex.create(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[1]),
                Vertex.create(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[2]),
                Vertex.create(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[3]),

                Vertex.create(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[0]),
                Vertex.create(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[1]),
                Vertex.create(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[2]),
                Vertex.create(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[3]),

                Vertex.create(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[0]),
                Vertex.create(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[1]),
                Vertex.create(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[2]),
                Vertex.create(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[3]),

                Vertex.create(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[0]),
                Vertex.create(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[1]),
                Vertex.create(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[2]),
                Vertex.create(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[3]),

                Vertex.create(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[0]),
                Vertex.create(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[1]),
                Vertex.create(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[2]),
                Vertex.create(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[3])
            ),
            List.of
            (
                0, 1, 2,
                0, 2, 3,

                4, 5, 6,
                4, 6, 7,

                8, 9, 10,
                8, 10, 11,

                12, 13, 14,
                12, 14, 15,

                16, 17, 18,
                16, 18, 19,

                20, 21, 22,
                20, 22, 23
            )
        ));

        blockBreakageObject.getComponentNotNull(Mesh.class).setTransparent(true);

        blockBreakageObject.getComponentNotNull(Mesh.class).onLoad();

        blockBreakageMesh = blockBreakageObject.getComponentNotNull(Mesh.class);

        inventoryMenu.addItem(ItemRegistry.ITEM_REFINED_ALUMINUM_INGOT.getId(), (byte) 10);
    }

    @Override
    public void updateMainThread()
    {
        super.updateMainThread();

        updateControls();
        updateMouselook();
        updateMovement();

        inventoryMenu.update();
        craftingTableMenu.update();

        jumpCooldownTimer -= Time.getDeltaTime();
        blockBreakCooldownTimer -= Time.getDeltaTime();
    }

    @Override
    public void renderDefault(@Nullable Camera camera)
    {
        if (camera != null)
            selectorMesh.render(camera);
    }

    private void initializeCamera()
    {
        jumpCooldownTimer = jumpCooldownTimerStart;
        blockBreakCooldownTimer = blockBreakCooldownTimerStart;

        getGameObject().addChild(GameObject.create("default.camera", Layer.DEFAULT));

        GameObject cameraObject = getGameObject().getChild("default.camera");

        cameraObject.addComponent(Camera.create(45.0f, 0.01f, 1000.0f));
        cameraObject.getTransform().setLocalPosition(new Vector3f(0.0f, 0.86f, 0.0f));

        camera = cameraObject.getComponent(Camera.class);

        InputManager.setMouseMode(MouseMode.LOCKED);
    }

    private void initializeUI()
    {
        inventoryMenu = (InventoryMenu) MenuManager.get("menu_inventory");

        assert inventoryMenu != null;

        inventoryMenu.setInventory(inventory);


        pauseMenu = (PauseMenu) MenuManager.get("menu_pause");

        assert pauseMenu != null;

        pauseMenu.setHost(this);

        pauseMenu.setActive(false);


        craftingTableMenu = (CraftingTableMenu) MenuManager.get("menu_crafting_table");

        assert craftingTableMenu != null;

        craftingTableMenu.setInventory(inventory);
        craftingTableMenu.setInventoryMenu(inventoryMenu);

        craftingTableMenu.setActive(false);
    }

    private void updateControls()
    {
        Collider self = getGameObject().getComponent(BoxCollider.class);

        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (InputManager.getKeyState(KeyCode.K, KeyState.PRESSED))
            setCraftingTableMenuActive(true);

        if (InputManager.getKeyState(KeyCode.LEFT, KeyState.PRESSED))
            inventoryMenu.currentSlotSelected--;

        if (InputManager.getKeyState(KeyCode.RIGHT, KeyState.PRESSED))
            inventoryMenu.currentSlotSelected++;

        if (InputManager.getKeyState(KeyCode.E, KeyState.PRESSED) && !pauseMenu.getActive() && !craftingTableMenu.isActive())
        {
            if (!inventoryMenu.getSurvivalMenuActive())
                InputManager.setMouseMode(MouseMode.FREE);
            else
                InputManager.setMouseMode(MouseMode.LOCKED);

            inventoryMenu.setSurvivalMenuActive(!inventoryMenu.getSurvivalMenuActive());
        }

        if (InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED))
        {
            if (craftingTableMenu.isActive())
            {
                craftingTableMenu.setActive(false);
                InputManager.setMouseMode(MouseMode.LOCKED);

                return;
            }

            if (inventoryMenu.getSurvivalMenuActive())
            {
                inventoryMenu.setSurvivalMenuActive(false);
                InputManager.setMouseMode(MouseMode.LOCKED);

                return;
            }

            if (pauseMenu.getActive())
            {
                pauseMenu.setActive(false);
                InputManager.setMouseMode(MouseMode.LOCKED);
            }
            else
            {
                pauseMenu.setActive(true);
                InputManager.setMouseMode(MouseMode.FREE);
            }
        }

        if (inventoryMenu.getSurvivalMenuActive() || inventoryMenu.getCreativeMenuActive() || pauseMenu.getActive() || craftingTableMenu.isActive())
            return;

        Raycast.VoxelHit hit = Raycast.castVoxel(camera.getGameObject().getTransform().getWorldPosition(), camera.getGameObject().getTransform().getForward(), 4);

        if (hit != null)
        {
            {
                Vector3f point = hit.center();

                short block = World.getLocalWorld().getBlock(point);

                if (block != BlockRegistry.BLOCK_AIR.getId() && block != -1)
                {
                    selectorMesh.active = true;

                    Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(point);
                    Vector3i chunkCoordinates = CoordinateHelper.worldToChunkCoordinates(point);

                    Vector3f selectorPosition = CoordinateHelper.blockToWorldCoordinates(blockCoordinates, chunkCoordinates).add(new Vector3f(0.5f));

                    blockBreakageMesh.getGameObject().getTransform().setLocalPosition(selectorPosition);

                    selectorMesh.position = selectorPosition;
                }
                else
                {
                    blockBreakageMesh.getGameObject().setActive(false);
                    selectorMesh.active = false;
                }
            }

            if (InputManager.getMouseState(MouseCode.MOUSE_LEFT, MouseState.HELD))
            {
                Vector3f point = hit.center();
                short blockID = World.getLocalWorld().getBlock(point);

                blockBreakageMesh.getGameObject().setActive(true);

                if (blockID != BlockRegistry.BLOCK_AIR.getId() && blockID != -1)
                {
                    Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(point);

                    if (breakingBlockCoordinates == null || !breakingBlockCoordinates.equals(blockCoordinates))
                    {
                        breakingBlockCoordinates = blockCoordinates;
                        breakingProgress = 0;
                    }

                    float blockHardness = Objects.requireNonNull(BlockRegistry.get(blockID)).getHardness();
                    breakingProgress += Time.getDeltaTime();

                    int totalStages = 9;
                    int currentStage = Math.min((int) ((breakingProgress / blockHardness) * totalStages), totalStages - 1);
                    updateDestroyStages(currentStage);

                    if (breakingProgress >= blockHardness)
                    {
                        if (Objects.requireNonNull(BlockRegistry.get(blockID)).toolRequired() == Tool.NONE)
                            inventoryMenu.addItem(Objects.requireNonNull(BlockRegistry.get(World.getLocalWorld().getBlock(point))).getAssociatedItem().getId(), (byte) 1);

                        World.getLocalWorld().setBlock(point, BlockRegistry.BLOCK_AIR.getId());

                        breakingProgress = 0;
                        breakingBlockCoordinates = null;
                        blockBreakCooldownTimer = blockBreakCooldownTimerStart;

                        updateDestroyStages(0);
                    }
                }
                else
                {
                    breakingBlockCoordinates = null;
                    breakingProgress = 0;
                    updateDestroyStages(0);
                }
            }
            else
            {
                breakingProgress = 0;
                updateDestroyStages(0);
                blockBreakageMesh.getGameObject().setActive(false);
            }

            if (InputManager.getMouseState(MouseCode.MOUSE_RIGHT, MouseState.PRESSED))
            {
                InventoryMenu.SlotData slot = inventoryMenu.getSlot(new Vector2i(0, inventoryMenu.currentSlotSelected));

                if (slot == null)
                    return;

                Item item = ItemRegistry.get(slot.id());

                if (item == null)
                {
                    System.err.println("Invalid item detected! (This shouldn't happen!)");
                    return;
                }

                Vector3f point = hit.center();
                Vector3f normal = hit.normal();

                short currentBlock = World.getLocalWorld().getBlock(point);

                if (currentBlock != -1 && currentBlock != BlockRegistry.BLOCK_AIR.getId())
                {
                    if (Objects.requireNonNull(BlockRegistry.get(currentBlock)).isInteractable())
                        Objects.requireNonNull(BlockRegistry.get(currentBlock)).onInteractedWith(this, World.getLocalWorld(), Objects.requireNonNull(World.getLocalWorld().getChunk(CoordinateHelper.worldToChunkCoordinates(point))), CoordinateHelper.worldToGlobalBlockCoordinates(point));
                }

                point.add(normal.mul(1f, new Vector3f()));

                currentBlock = World.getLocalWorld().getBlock(point);

                if (currentBlock == -1 || currentBlock == BlockRegistry.BLOCK_AIR.getId())
                {
                    if (slot.count() <= 0 || !item.isBlockItem())
                        return;

                    World.getLocalWorld().setBlock(point, item.getAssociatedBlock().getId());

                    inventoryMenu.setSlot(new Vector2i(0, inventoryMenu.currentSlotSelected), item.getAssociatedBlock().getId(), (byte) (slot.count() - 1));
                }
            }
        }
        else
        {
            breakingBlockCoordinates = null;
            breakingProgress = 0;
            updateDestroyStages(0);
            blockBreakageMesh.getGameObject().setActive(false);
            selectorMesh.active = false;
        }

        if (InputManager.getScrollDelta() > 0)
            inventoryMenu.currentSlotSelected--;

        if (InputManager.getScrollDelta() < 0)
            inventoryMenu.currentSlotSelected++;
    }

    private void updateMouselook()
    {
        if (inventoryMenu.getSurvivalMenuActive() || inventoryMenu.getCreativeMenuActive() || pauseMenu.getActive() || craftingTableMenu.isActive())
            return;

        Vector2f mouseDelta = InputManager.getMouseDelta();

        Vector3f rotation = new Vector3f(camera.getGameObject().getTransform().getLocalRotation());

        float mouseSensitivity = 0.1f;

        rotation.y += -mouseDelta.x * mouseSensitivity;
        rotation.x += -mouseDelta.y * mouseSensitivity;

        float maxPitch = 89.9f;

        if (rotation.x > maxPitch)
            rotation.x = maxPitch;
        else if (rotation.x < -maxPitch)
            rotation.x = -maxPitch;

        camera.getGameObject().getTransform().setLocalRotation(rotation);
    }

    private void updateMovement()
    {
        if (inventoryMenu.getSurvivalMenuActive() || inventoryMenu.getCreativeMenuActive() || pauseMenu.getActive() || craftingTableMenu.isActive())
            return;

        Rigidbody rigidbody = getGameObject().getComponent(Rigidbody.class);

        if (rigidbody == null)
        {
            System.err.println("No RigidBody component found on game object: '" + getGameObject().getName() + "'!");
            return;
        }

        float movementSpeed = getWalkingSpeed() * Time.getDeltaTime();

        if (InputManager.getKeyState(KeyCode.LEFT_SHIFT, KeyState.HELD))
            movementSpeed = getRunningSpeed() * Time.getDeltaTime();

        Vector3f movement = new Vector3f();

        Vector3f forward = new Vector3f(camera.getGameObject().getTransform().getForward());
        Vector3f right = new Vector3f(camera.getGameObject().getTransform().getRight());

        forward.y = 0;
        right.y = 0;

        forward.normalize();
        right.normalize();

        if (InputManager.getKeyState(KeyCode.W, KeyState.HELD))
            movement.add(forward.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.S, KeyState.HELD))
            movement.add(forward.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.A, KeyState.HELD))
            movement.add(right.mul(-movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.D, KeyState.HELD))
            movement.add(right.mul(movementSpeed, new Vector3f()));

        if (InputManager.getKeyState(KeyCode.SPACE, KeyState.HELD) && rigidbody.isGrounded() && jumpCooldownTimer <= 0)
        {
            rigidbody.addForce(new Vector3f(0.0f, 5.5f, 0.0f));
            jumpCooldownTimer = jumpCooldownTimerStart;
        }

        rigidbody.addForce(movement);
    }

    public void updateDestroyStages(int index)
    {
        Vector2f[] uvs = Objects.requireNonNull(TextureAtlasManager.get("blocks")).getSubTextureCoordinates("destroy_stage_" + index);

        blockBreakageMesh.setVertices(
        List.of
        (
            Vertex.create(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[0]),
            Vertex.create(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[1]),
            Vertex.create(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[2]),
            Vertex.create(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.5f), uvs[3]),

            Vertex.create(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[0]),
            Vertex.create(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[1]),
            Vertex.create(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[2]),
            Vertex.create(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, -0.5f), uvs[3]),

            Vertex.create(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[0]),
            Vertex.create(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[1]),
            Vertex.create(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[2]),
            Vertex.create(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(-0.5f, 0.0f, 0.0f), uvs[3]),

            Vertex.create(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[0]),
            Vertex.create(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[1]),
            Vertex.create(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[2]),
            Vertex.create(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.5f, 0.0f, 0.0f), uvs[3]),

            Vertex.create(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[0]),
            Vertex.create(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[1]),
            Vertex.create(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[2]),
            Vertex.create(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.5f, 0.0f), uvs[3]),

            Vertex.create(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[0]),
            Vertex.create(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[1]),
            Vertex.create(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[2]),
            Vertex.create(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, -0.5f, 0.0f), uvs[3])
        ));

        blockBreakageMesh.setIndices(
        List.of
        (
            0, 1, 2,
            0, 2, 3,

            4, 5, 6,
            4, 6, 7,

            8, 9, 10,
            8, 10, 11,

            12, 13, 14,
            12, 14, 15,

            16, 17, 18,
            16, 18, 19,

            20, 21, 22,
            20, 22, 23
        ));

        blockBreakageMesh.onLoad();
    }

    private void resetBreakingVisuals()
    {
        breakingBlockCoordinates = null;
        breakingProgress = 0;
        updateDestroyStages(0);
    }

    @Override
    public String getDisplayName()
    {
        return "Player**";
    }

    @Override
    public String getRegistryName()
    {
        return "entity_player";
    }

    @Override
    public float getWalkingSpeed()
    {
        return 30.0f;
    }

    @Override
    public float getRunningSpeed()
    {
        return 35.0f;
    }

    @Override
    public float getMaximumHealth()
    {
        return 100;
    }

    public @NotNull Camera getCamera()
    {
        return camera;
    }

    public void setPaused(boolean paused)
    {
        if (paused)
        {
            pauseMenu.setActive(true);
            InputManager.setMouseMode(MouseMode.FREE);
        }
        else
        {
            pauseMenu.setActive(false);
            InputManager.setMouseMode(MouseMode.LOCKED);
        }
    }

    public void setCraftingTableMenuActive(boolean active)
    {
        if (active)
        {
            craftingTableMenu.build();

            InputManager.setMouseMode(MouseMode.FREE);
        }
        else
            InputManager.setMouseMode(MouseMode.LOCKED);

        craftingTableMenu.setActive(active);
    }

    public boolean isCraftingTableMenuActive()
    {
        return craftingTableMenu.isActive();
    }

    @Override
    public void uninitialize()
    {
        selectorMesh.uninitialize();
    }

    @CustomConstructor("create")
    public static class SelectorMesh implements Serializable
    {
        private static final float[] BOX_VERTICES =
        {
            -0.5f, -0.5f, -0.5f,   0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,   0.5f,  0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,  -0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,  -0.5f, -0.5f, -0.5f,

            -0.5f, -0.5f,  0.5f,   0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,   0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,  -0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,  -0.5f, -0.5f,  0.5f,

            -0.5f, -0.5f, -0.5f,  -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,   0.5f, -0.5f,  0.5f,
            0.5f,  0.5f, -0.5f,   0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,  -0.5f,  0.5f,  0.5f,
        };

        private final List<Vector3f> vertices = new CopyOnWriteArrayList<>();

        private @NotNull Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);

        private boolean active;

        private transient int vao, vbo, ibo;

        private SelectorMesh() { }

        private void generate()
        {
            for (int i = 0; i < BOX_VERTICES.length; i += 3)
                vertices.add(new Vector3f(BOX_VERTICES[i], BOX_VERTICES[i + 1], BOX_VERTICES[i + 2]));

            vao = GL41.glGenVertexArrays();
            GL41.glBindVertexArray(vao);

            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size() * 3);

            for (Vector3f vertex : vertices)
                vertexBuffer.put(vertex.x).put(vertex.y).put(vertex.z);

            vertexBuffer.flip();

            vbo = GL41.glGenBuffers();
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertexBuffer, GL41.GL_DYNAMIC_DRAW);
            GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, 0, 0);
            GL41.glEnableVertexAttribArray(0);

            GL41.glBindVertexArray(0);
        }

        public void render(@Nullable Camera camera)
        {
            if (camera == null || !active)
                return;

            Shader shader = Objects.requireNonNull(ShaderManager.get("selector_box"));

            GL41.glBindVertexArray(vao);
            GL41.glEnableVertexAttribArray(0);

            int error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after binding of VAO + enabling of vertex attribute arrays (SelectorBox::render): " + error);

            shader.bind();

            error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after binding shader (SelectorBox::render): " + error);

            Matrix4f localMatrix = new Matrix4f()
                    .identity()
                    .translate(position)
                    .rotateY(0.0f)
                    .rotateX(0.0f)
                    .rotateZ(0.0f)
                    .scale(1.0f);

            shader.setUniform("projection", camera.getProjectionMatrix());
            shader.setUniform("view", camera.getViewMatrix());
            shader.setUniform("model", localMatrix);

            error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after setting shader uniforms (SelectorBox::render): " + error);

            GL41.glDrawArrays(GL41.GL_LINES, 0, BOX_VERTICES.length / 3);

            error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after rendering objects (SelectorBox::render): " + error);

            shader.unbind();

            error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after unbinding shader (SelectorBox::render): " + error);

            GL41.glDisableVertexAttribArray(0);
            GL41.glBindVertexArray(0);

            error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after unbinding of VAO + disabling of vertex attribute arrays (SelectorBox::render): " + error);
        }

        public void uninitialize()
        {
            MainThreadExecutor.submit(() ->
            {
                GL41.glDeleteVertexArrays(vao);
                GL41.glDeleteBuffers(vbo);
                GL41.glDeleteBuffers(ibo);
            });
        }

        public static @NotNull SelectorMesh create()
        {
            SelectorMesh result = new SelectorMesh();

            result.vao = -1;
            result.vbo = -1;
            result.ibo = -1;

            result.generate();

            return result;
        }
    }
}