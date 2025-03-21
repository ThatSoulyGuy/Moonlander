package com.thatsoulyguy.moonlander.entity.entities;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.colliders.BoxCollider;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.LivingEntity;
import com.thatsoulyguy.moonlander.entity.model.EntityModel;
import com.thatsoulyguy.moonlander.gameplay.OxygenBubble;
import com.thatsoulyguy.moonlander.gameplay.OxygenBubbleManager;
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
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.systems.*;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.CoordinateHelper;
import com.thatsoulyguy.moonlander.util.PlayerDisplay;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityPlayer extends LivingEntity
{
    private @EffectivelyNotNull Camera camera;

    private @EffectivelyNotNull Mesh blockBreakageMesh;

    private transient @EffectivelyNotNull SelectorMesh selectorMesh;

    private @EffectivelyNotNull Vector3i breakingBlockCoordinates;

    private final Inventory inventory = new Inventory();

    private PlayerDisplay display;

    private transient ImageUIElement backgroundShading;

    private float breakingProgress;

    private final float jumpCooldownTimerStart = 0.18f;
    private float jumpCooldownTimer;

    private final float oxygenDepletionCooldownTimerStart = 15.0f;
    private float oxygenDepletionCooldownTimer;

    private final float suffocationCooldownTimerStart = 4.0f;
    private float suffocationCooldownTimer;

    private final float oxygenRefillCooldownTimerStart = 0.25f;
    private float oxygenRefillCooldownTimer;

    private final float blockMiningAudioTimerStart = 0.25f;
    private float blockMiningAudioTimer;

    private final float healTimerStart = 4.0f;
    private float healTimer;

    private long lastHitTime = 0;

    private int oxygen = 20;

    private float footstepTimer = 0f;

    private boolean starterItemsGiven = false;

    private static EntityPlayer instance;

    @Override
    public void initialize()
    {
        super.initialize();

        initializeCamera();
        initializeUI();

        GameObject displayObject = camera.getGameObject().addChild(GameObject.create("default.display", Layer.DEFAULT));

        displayObject.setTransient(true);

        displayObject.addComponent(Objects.requireNonNull(ShaderManager.get("legacy.default")));
        displayObject.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));

        display = displayObject.addComponent(PlayerDisplay.create());

        MainThreadExecutor.submit(() ->
        {
            selectorMesh = SelectorMesh.create();

            GameObject blockBreakageObject = GameObject.create("default.block_breakage", Layer.DEFAULT);

            blockBreakageObject.setTransient(true);

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

            blockBreakageObject.getComponentNotNull(Mesh.class).generate();

            blockBreakageMesh = blockBreakageObject.getComponentNotNull(Mesh.class);
        });

        if (!starterItemsGiven)
        {
            inventory.addItem(ItemRegistry.ITEM_REFINED_ALUMINUM_INGOT.getId(), (byte) 10);
            inventory.addItem(ItemRegistry.ITEM_KNOWLEDGE_BOOK.getId(), (byte) 1);
            inventory.addItem(ItemRegistry.ITEM_SOFT_MOON_ROCK_BLOCK.getId(), (byte) 1);

            starterItemsGiven = true;
        }

        oxygenDepletionCooldownTimer = oxygenDepletionCooldownTimerStart;
        suffocationCooldownTimer = suffocationCooldownTimerStart;
        oxygenRefillCooldownTimer = oxygenRefillCooldownTimerStart;
        blockMiningAudioTimer = blockMiningAudioTimerStart;
        healTimer = healTimerStart;

        instance = this;
    }

    @Override
    public void updateMainThread()
    {
        super.updateMainThread();

        backgroundShading.getGameObject().getTransform().setLocalPosition(new Vector3f((float) Window.getDimensions().x / 2, (float) Window.getDimensions().y / 2, 0.0f));
        backgroundShading.getGameObject().getTransform().setLocalScale(new Vector3f(Window.getDimensions().x, Window.getDimensions().y, 0.0f));

        if (blockBreakageMesh == null)
            return;

        display.currentItem = ItemRegistry.get(inventory.getCurrentlySelectedSlot().id());

        updateControls();
        updateMouselook();
        updateMovement();

        if (oxygenDepletionCooldownTimer < 0 && oxygen > 0)
        {
            oxygen--;
            oxygenDepletionCooldownTimer = oxygenDepletionCooldownTimerStart;
        }

        if (suffocationCooldownTimer < 0 && oxygen <= 0)
        {
            damage(this, 3);
            suffocationCooldownTimer = suffocationCooldownTimerStart;
        }

        if (healTimer < 0 && getCurrentHealth() < getMaximumHealth())
        {
            setCurrentHealth(getCurrentHealth() + 1);

            healTimer = healTimerStart;
        }

        if (getCurrentHealth() <= 0)
        {
            DeathSystem.getInstance().getGameObject().setActive(true);
            CreativeCraftingSystem.getInstance().getGameObject().setActive(false);
            HotbarSystem.getInstance().getGameObject().setActive(false);
            InventorySystem.getInstance().getGameObject().setActive(false);
            PauseSystem.getInstance().getGameObject().setActive(false);
            SurvivalCraftingSystem.getInstance().getGameObject().setActive(false);
            WinConditionSystem.getInstance().getGameObject().setActive(false);

            setBackgroundShadingActive(true);
            pause(true);
        }

        BoxCollider self = getGameObject().getComponentNotNull(BoxCollider.class);

        for (Collider other : OxygenBubbleManager.getAll().stream().filter(OxygenBubble::isOxygenActive).map((oxygenBubble -> oxygenBubble.getGameObject().getComponent(BoxCollider.class))).toList())
        {
            if (other.intersects(self) && oxygenRefillCooldownTimer < 0)
            {
                if (oxygen < 100)
                    oxygen++;

                oxygenRefillCooldownTimer = oxygenRefillCooldownTimerStart;
            }
        }

        jumpCooldownTimer -= Time.getDeltaTime();
        oxygenDepletionCooldownTimer -= Time.getDeltaTime();
        suffocationCooldownTimer -= Time.getDeltaTime();
        oxygenRefillCooldownTimer -= Time.getDeltaTime();
        healTimer -= Time.getDeltaTime();
    }

    @Override
    public void renderDefault(@Nullable Camera camera)
    {
        if (camera != null)
            selectorMesh.render(camera);
    }

    public void pause(boolean pause)
    {
        if (pause)
            InputManager.setMouseMode(MouseMode.FREE);
        else
            InputManager.setMouseMode(MouseMode.LOCKED);
    }

    public void setBackgroundShadingActive(boolean active)
    {
        backgroundShading.getGameObject().setActive(active);
    }

    private void initializeCamera()
    {
        jumpCooldownTimer = jumpCooldownTimerStart;

        getGameObject().addChild(GameObject.create("default.camera", Layer.DEFAULT));

        GameObject cameraObject = getGameObject().getChild("default.camera");

        cameraObject.setTransient(true);

        cameraObject.addComponent(AudioListener.create());

        cameraObject.addComponent(Camera.create(45.0f, 0.1f, 1000.0f));
        cameraObject.getTransform().setLocalPosition(new Vector3f(0.0f, 0.86f, 0.0f));

        camera = cameraObject.getComponent(Camera.class);

        InputManager.setMouseMode(MouseMode.LOCKED);
    }

    private void initializeUI()
    {
        inventory.initialize();

        GameObject hotbarPanelObject = UIPanel.fromJson("ui.hotbar", AssetPath.create("moonlander", "ui/HotbarPanel.json"));

        UIPanel hotbarPanel = hotbarPanelObject.getComponentNotNull(UIPanel.class);

        hotbarPanel.setPanelAlignment(UIPanel.PanelAlignment.LOWER_CENTER);
        hotbarPanel.setOffset(new Vector2i(0, 10));

        HotbarSystem hotbarSystem = hotbarPanelObject.addComponent(HotbarSystem.create());

        hotbarSystem.setInventory(inventory);
        hotbarSystem.generate();


        backgroundShading = UIElement.createGameObject("ui.background_shading", ImageUIElement.class, new Vector2f(0, 0), new Vector2f(0, 0), UIManager.getCanvas()).getComponentNotNull(ImageUIElement.class);

        backgroundShading.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));

        backgroundShading.getGameObject().setActive(false);


        GameObject survivalCraftingPanelObject = UIPanel.fromJson("ui.survival_crafting", AssetPath.create("moonlander", "ui/SurvivalCraftingPanel.json"));

        UIPanel survivalCraftingPanel = survivalCraftingPanelObject.getComponentNotNull(UIPanel.class);

        survivalCraftingPanel.setOffset(new Vector2i(-179, 250));
        survivalCraftingPanel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        survivalCraftingPanelObject.addComponent(SurvivalCraftingSystem.create());

        survivalCraftingPanelObject.setActive(false);


        GameObject creativeCraftingPanelObject = UIPanel.fromJson("ui.creative_crafting", AssetPath.create("moonlander", "ui/CreativeCraftingPanel.json"));

        UIPanel creativeCraftingPanel = creativeCraftingPanelObject.getComponentNotNull(UIPanel.class);

        creativeCraftingPanel.setOffset(new Vector2i(-131, 285));
        creativeCraftingPanel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        creativeCraftingPanelObject.addComponent(CreativeCraftingSystem.create());

        creativeCraftingPanelObject.setActive(false);


        GameObject inventoryPanelObject = UIPanel.fromJson("ui.inventory", AssetPath.create("moonlander", "ui/InventoryPanel.json"));

        UIPanel inventoryPanel = inventoryPanelObject.getComponentNotNull(UIPanel.class);

        inventoryPanel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        InventorySystem inventorySystem = inventoryPanelObject.addComponent(InventorySystem.create());

        inventorySystem.setInventory(inventory);
        inventorySystem.generate();

        inventoryPanelObject.setActive(false);


        GameObject pausePanelObject = UIPanel.fromJson("ui.pause", AssetPath.create("moonlander", "ui/PausePanel.json"));

        UIPanel pausePanel = pausePanelObject.getComponentNotNull(UIPanel.class);

        pausePanel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        pausePanelObject.addComponent(PauseSystem.create());

        pausePanelObject.setActive(false);


        GameObject winConditionPanelObject = UIPanel.fromJson("ui.win_condition", AssetPath.create("moonlander", "ui/WinConditionPanel.json"));

        UIPanel winConditionPanel = winConditionPanelObject.getComponentNotNull(UIPanel.class);

        winConditionPanel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        winConditionPanelObject.addComponent(WinConditionSystem.create());

        winConditionPanelObject.setActive(false);


        GameObject deathPanelObject = UIPanel.fromJson("ui.death", AssetPath.create("moonlander", "ui/DeathPanel.json"));

        UIPanel deathPanel = deathPanelObject.getComponentNotNull(UIPanel.class);

        deathPanel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        deathPanelObject.addComponent(DeathSystem.create());

        deathPanelObject.setActive(false);

        inventory.refreshAll();
    }

    private void updateControls()
    {
        Collider self = getGameObject().getComponent(BoxCollider.class);

        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (!InventorySystem.getInstance().getGameObject().isActive() && InputManager.getKeyState(KeyCode.E, KeyState.PRESSED))
        {
            pause(true);
            setBackgroundShadingActive(true);
            InventorySystem.getInstance().getGameObject().setActive(true);
            SurvivalCraftingSystem.getInstance().getGameObject().setActive(true);
        }

        if ((InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED) || InputManager.getKeyState(KeyCode.E, KeyState.PRESSED)) && SurvivalCraftingSystem.getInstance().getGameObject().isActive())
        {
            pause(false);
            setBackgroundShadingActive(false);
            InventorySystem.getInstance().getGameObject().setActive(false);
            SurvivalCraftingSystem.getInstance().getGameObject().setActive(false);
        }

        if (InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED) && CreativeCraftingSystem.getInstance().getGameObject().isActive())
        {
            pause(false);
            setBackgroundShadingActive(false);
            InventorySystem.getInstance().getGameObject().setActive(false);
            CreativeCraftingSystem.getInstance().getGameObject().setActive(false);
        }

        if (InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED) && !isMenuActive())
        {
            pause(true);
            setBackgroundShadingActive(true);
            PauseSystem.getInstance().getGameObject().setActive(true);
        }

        if (InputManager.getKeyState(KeyCode.ESCAPE, KeyState.PRESSED) && PauseSystem.getInstance().getGameObject().isActive())
        {
            pause(false);
            setBackgroundShadingActive(false);
            PauseSystem.getInstance().getGameObject().setActive(false);
        }

        if (isMenuActive())
            return;

        if (InputManager.getScrollDelta() > 0)
            inventory.currentlySelectedSlotIndex--;

        if (InputManager.getScrollDelta() < 0)
            inventory.currentlySelectedSlotIndex++;

        if (InputManager.getMouseState(MouseCode.LEFT, MouseState.PRESSED))
        {
            long now = System.currentTimeMillis();
            long deltaMs = now - lastHitTime;

            lastHitTime = now;

            float scale = deltaMs / 1000f;

            if (scale > 1.0f)
                scale = 1.0f;

            float baseDamage = 2.0f;
            float damage = baseDamage * scale;

            float minDamage = 1.0f;
            if (damage < minDamage)
                damage = minDamage;

            Raycast.Hit boxHit = Raycast.cast(
                    camera.getGameObject().getTransform().getWorldPosition(),
                    camera.getGameObject().getTransform().getForward(),
                    4,
                    self
            );

            display.swing();

            if (boxHit != null && boxHit.collider() instanceof BoxCollider collider)
            {
                for (Class<? extends LivingEntity> clazz : LivingEntity.getLivingEntityClassTypes())
                {
                    if (collider.getGameObject().hasComponent(clazz))
                    {
                        Inventory.SlotData currentlySelectedSlot = inventory.getCurrentlySelectedSlot();

                        Item item = ItemRegistry.get(currentlySelectedSlot.id());

                        assert item != null;

                        float extraDamage = item.getToolType() == Tool.SWORD ? item.getAccossiatedModifier() : 0;

                        collider.getGameObject().getComponentNotNull(clazz).damage(this, (int) damage + (int) extraDamage);

                        return;
                    }
                }
            }
        }

        if (InputManager.getMouseState(MouseCode.RIGHT, MouseState.PRESSED))
        {
            Raycast.Hit boxHit = Raycast.cast(camera.getGameObject().getTransform().getWorldPosition(), camera.getGameObject().getTransform().getForward(), 4, self);

            if (boxHit != null && boxHit.collider() instanceof BoxCollider collider)
            {
                for (Class<? extends Entity> clazz : Entity.getEntityClassTypes())
                {
                    if (collider.getGameObject().hasComponent(clazz))
                    {
                        collider.getGameObject().getComponentNotNull(clazz).onInteractedWith(Objects.requireNonNull(World.getLocalWorld()), this);

                        return;
                    }
                }
            }
        }

        Raycast.VoxelHit hit = Raycast.castVoxel(camera.getGameObject().getTransform().getWorldPosition(), camera.getGameObject().getTransform().getForward(), 4);

        if (hit != null)
        {
            {
                Vector3f point = hit.center();
                short block = Objects.requireNonNull(World.getLocalWorld()).getBlock(point);

                if (block != BlockRegistry.BLOCK_AIR.getId() && block != -1 && Objects.requireNonNull(BlockRegistry.get(block)).isSolid())
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

            if (InputManager.getMouseState(MouseCode.LEFT, MouseState.HELD))
            {
                Vector3f point = hit.center();
                short blockId = World.getLocalWorld().getBlock(point);
                Block block = BlockRegistry.get(blockId);

                blockBreakageMesh.getGameObject().setActive(true);

                if (blockId != BlockRegistry.BLOCK_AIR.getId() && blockId != -1 && Objects.requireNonNull(block).isSolid())
                {
                    Vector3i blockCoordinates = CoordinateHelper.worldToBlockCoordinates(point);

                    if (breakingBlockCoordinates == null || !breakingBlockCoordinates.equals(blockCoordinates))
                    {
                        breakingBlockCoordinates = blockCoordinates;
                        breakingProgress = 0;
                    }

                    Inventory.SlotData currentlySelectedSlot = inventory.getCurrentlySelectedSlot();

                    float blockHardness;

                    blockHardness = block.getHardness() * Objects.requireNonNull(ItemRegistry.get(currentlySelectedSlot.id())).getAccossiatedModifier();

                    breakingProgress += Time.getDeltaTime();

                    if (blockMiningAudioTimer < 0)
                    {
                        GameObject soundObject = GameObject.create("block.mining" + new Random().nextInt(4096), Layer.DEFAULT);

                        soundObject.addComponent(block.getMiningAudioClips().get(new Random().nextInt(block.getMiningAudioClips().size() - 1)));

                        soundObject.getTransform().setLocalPosition(new Vector3f(blockCoordinates));

                        AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                        clip.setLooping(false);
                        clip.play(true);

                        display.swing();

                        blockMiningAudioTimer = blockMiningAudioTimerStart;
                    }

                    int totalStages = 9;
                    int currentStage = Math.min((int) ((breakingProgress / blockHardness) * totalStages), totalStages - 1);
                    updateDestroyStages(currentStage);

                    if (breakingProgress >= blockHardness)
                    {
                        Tool toolPossessed;

                        currentlySelectedSlot = inventory.getCurrentlySelectedSlot();

                        toolPossessed = Objects.requireNonNull(ItemRegistry.get(currentlySelectedSlot.id())).getToolType();

                        if (block.toolRequired() == Tool.NONE || block.toolRequired() == toolPossessed)
                        {
                            inventory.addItem(Objects.requireNonNull(
                                    BlockRegistry.get(World.getLocalWorld().getBlock(point))
                            ).getAssociatedItem().getId(), (byte) 1);
                        }

                        World.getLocalWorld().setBlock(this, point, BlockRegistry.BLOCK_AIR.getId());

                        GameObject soundObject = GameObject.create("block.break" + new Random().nextInt(4096), Layer.DEFAULT);

                        soundObject.addComponent(block.getBrokenAudioClips().get(new Random().nextInt(block.getBrokenAudioClips().size() - 1)));

                        soundObject.getTransform().setLocalPosition(new Vector3f(blockCoordinates));

                        AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                        clip.setLooping(false);
                        clip.setVolume(30.0f);
                        clip.play(true);

                        breakingProgress = 0;
                        breakingBlockCoordinates = null;

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

            if (InputManager.getMouseState(MouseCode.RIGHT, MouseState.PRESSED))
            {
                Raycast.Hit boxHit = Raycast.cast(camera.getGameObject().getTransform().getWorldPosition(), camera.getGameObject().getTransform().getForward(), 4, self);

                if (boxHit != null && boxHit.collider() instanceof BoxCollider collider)
                {
                    for (Class<? extends Entity> clazz : Entity.getEntityClassTypes())
                    {
                        if (collider.getGameObject().hasComponent(clazz))
                        {
                            collider.getGameObject().getComponentNotNull(clazz).onInteractedWith(World.getLocalWorld(), this);

                            return;
                        }
                    }
                }

                Inventory.SlotData slot = inventory.getCurrentlySelectedSlot();

                Item item = ItemRegistry.get(slot.id());

                if (item == null)
                {
                    System.err.println("Invalid item detected! (This shouldn't happen!)");
                    return;
                }

                Vector3f point = hit.center();
                Vector3f normal = hit.normal();

                short currentBlock = World.getLocalWorld().getBlock(point);

                boolean interactable = false;

                if (currentBlock != -1 && currentBlock != BlockRegistry.BLOCK_AIR.getId())
                {
                    interactable = Objects.requireNonNull(BlockRegistry.get(currentBlock)).isInteractable();

                    if (interactable)
                    {
                        Objects.requireNonNull(BlockRegistry.get(currentBlock)).onInteractedWith(
                                this,
                                World.getLocalWorld(),
                                Objects.requireNonNull(World.getLocalWorld().getChunk(CoordinateHelper.worldToChunkCoordinates(point))),
                                CoordinateHelper.worldToGlobalBlockCoordinates(point)
                        );
                    }
                    else
                        Objects.requireNonNull(ItemRegistry.get(inventory.getCurrentlySelectedSlot().id())).onInteractedWith(this);
                }

                point.add(normal.mul(1f, new Vector3f()));

                currentBlock = World.getLocalWorld().getBlock(point);

                if (currentBlock == -1 || currentBlock == BlockRegistry.BLOCK_AIR.getId() && !interactable)
                {
                    if (slot.count() <= 0 || !item.isBlockItem())
                        return;

                    Block block = BlockRegistry.get(currentBlock);

                    World.getLocalWorld().setBlock(this, point, item.getAssociatedBlock().getId());

                    GameObject soundObject = GameObject.create("block.break" + new Random().nextInt(4096), Layer.DEFAULT);

                    soundObject.addComponent(block.getBrokenAudioClips().get(new Random().nextInt(block.getBrokenAudioClips().size() - 1)));

                    soundObject.getTransform().setLocalPosition(new Vector3f(CoordinateHelper.worldToBlockCoordinates(point)));

                    AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                    clip.setLooping(false);
                    clip.setVolume(30.0f);
                    clip.play(true);

                    if (Objects.requireNonNull(ItemRegistry.get(slot.id())).getToolType() == Tool.BUCKET)
                        inventory.setSlot(new Vector2i(new Vector2i(0, inventory.currentlySelectedSlotIndex)),
                                new Inventory.SlotData(
                                    ItemRegistry.ITEM_EMPTY_BUCKET.getId(),
                                    (byte) 1));
                    else
                        inventory.setSlot(new Vector2i(new Vector2i(0, inventory.currentlySelectedSlotIndex)),
                            new Inventory.SlotData(
                                item.getId(),
                                (byte) (slot.count() - 1)));
                }
            }
        }
        else
        {
            if (InputManager.getMouseState(MouseCode.RIGHT, MouseState.PRESSED))
                Objects.requireNonNull(ItemRegistry.get(inventory.getCurrentlySelectedSlot().id())).onInteractedWith(this);

            breakingBlockCoordinates = null;
            breakingProgress = 0;

            updateDestroyStages(0);

            blockBreakageMesh.getGameObject().setActive(false);
            selectorMesh.active = false;
        }

        blockMiningAudioTimer -= Time.getDeltaTime();
    }

    /**
     * Reset all block-breaking state.
     */
    private void resetBlockBreaking()
    {
        breakingBlockCoordinates = null;
        breakingProgress = 0.0f;
        blockBreakageMesh.getGameObject().setActive(false);
        updateDestroyStages(0);
    }

    private void updateMouselook()
    {
        if (isMenuActive())
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

        Vector3f difference = camera.getGameObject().getTransform().getLocalRotation().sub(rotation, new Vector3f());

        display.cameraMovement = new Vector3f(-difference.y, -difference.x, difference.z).mul(0.1f, new Vector3f());

        camera.getGameObject().getTransform().setLocalRotation(rotation);
    }

    private void updateMovement()
    {
        if (isMenuActive())
            return;

        Rigidbody rigidbody = getGameObject().getComponent(Rigidbody.class);

        if (rigidbody == null)
        {
            System.err.println("No RigidBody component found on player!");
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

        if (rigidbody.isGrounded() && movement.lengthSquared() > 0.001f)
        {
            float speed = rigidbody.getActualMovement().length();

            float frequency = 10.0f;
            float amplitude = Math.min(speed * 0.05f, 0.2f);

            float time = Time.getTime();

            float bobOffsetY = (float) Math.sin(time * frequency) * amplitude;
            float bobOffsetX = (float) Math.cos(time * frequency * 2.0f) * amplitude * 0.5f;

            display.playerMovement = new Vector3f(bobOffsetX, bobOffsetY, 0).mul(60.0f);
        }

        if (rigidbody.isGrounded() && movement.lengthSquared() > 0.001f)
        {
            float currentSpeed = (rigidbody.getActualMovement().length() * 100) * 2;

            if (currentSpeed > 0.2f)
            {
                footstepTimer -= Time.getDeltaTime();

                if (footstepTimer <= 0f)
                {
                    GameObject soundObject = GameObject.create("player.step.stone" + new Random().nextInt(4096), Layer.DEFAULT);

                    soundObject.addComponent(Objects.requireNonNull(AudioManager.get("player.step.stone." + new Random().nextInt(6))));

                    soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

                    AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);
                    clip.setLooping(false);

                    float maxSpeed = 10.0f;
                    float volume = Math.min(currentSpeed / maxSpeed, 1.0f);

                    clip.setVolume(volume);

                    clip.play(true);

                    float minInterval = 0.3f;
                    float maxInterval = 0.7f;

                    float clampedSpeed = Math.min(currentSpeed, maxSpeed);

                    footstepTimer = maxInterval - (clampedSpeed / maxSpeed) * (maxInterval - minInterval);
                }
            }
        }
        else
            footstepTimer = 0f;
    }

    public void updateDestroyStages(int index)
    {
        if (blockBreakageMesh == null)
            return;

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
            )
        );

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
            )
        );

        blockBreakageMesh.generate();
    }

    @Override
    public @NotNull String getRegistryName()
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
    public int getMaximumHealth()
    {
        return 20;
    }

    @Override
    public @NotNull Vector3f getBoundingBoxSize()
    {
        return new Vector3f(0.65f, 1.89f, 0.65f);
    }

    @Override
    public @Nullable Class<? extends EntityModel> getModelType()
    {
        return null;
    }

    public @NotNull Camera getCamera()
    {
        return camera;
    }

    @Override
    public void onDamaged(@NotNull World world, @NotNull Entity damager, int damageDealt)
    {
        camera.getGameObject().getTransform().rotate(new Vector3f(8.0f, 0.0f, 0.0f));
    }

    public void setOxygen(int oxygen)
    {
        this.oxygen = oxygen;
    }

    public int getOxygen()
    {
        return oxygen;
    }

    public void setPaused(boolean paused)
    {
        if (paused)
            InputManager.setMouseMode(MouseMode.FREE);
        else
            InputManager.setMouseMode(MouseMode.LOCKED);
    }

    private boolean isMenuActive()
    {
        return InventorySystem.getInstance().getGameObject().isActive() || PauseSystem.getInstance().getGameObject().isActive() || DeathSystem.getInstance().getGameObject().isActive();
    }

    @Override
    public void uninitialize()
    {
        selectorMesh.uninitialize();
    }

    public static @NotNull EntityPlayer getLocalPlayer()
    {
        return instance;
    }

    @CustomConstructor("create")
    public static class SelectorMesh implements Serializable
    {
        private static final float[] BOX_VERTICES =
        {
            -0.51f, -0.51f, -0.51f,   0.51f, -0.51f, -0.51f,
             0.51f, -0.51f, -0.51f,   0.51f,  0.51f, -0.51f,
             0.51f,  0.51f, -0.51f,  -0.51f,  0.51f, -0.51f,
            -0.51f,  0.51f, -0.51f,  -0.51f, -0.51f, -0.51f,

            -0.51f, -0.51f,  0.51f,   0.51f, -0.51f,  0.51f,
             0.51f, -0.51f,  0.51f,   0.51f,  0.51f,  0.51f,
             0.51f,  0.51f,  0.51f,  -0.51f,  0.51f,  0.51f,
            -0.51f,  0.51f,  0.51f,  -0.51f, -0.51f,  0.51f,

            -0.51f, -0.51f, -0.51f,  -0.51f, -0.51f,  0.51f,
             0.51f, -0.51f, -0.51f,   0.51f, -0.51f,  0.51f,
             0.51f,  0.51f, -0.51f,   0.51f,  0.51f,  0.51f,
            -0.51f,  0.51f, -0.51f,  -0.51f,  0.51f,  0.51f,
        };

        private final List<Vector3f> vertices = new CopyOnWriteArrayList<>();

        private @NotNull Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);

        public boolean active;

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
                System.err.println("OpenGL Error after binding of VAO (SelectorBox::render): " + error);

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
                System.err.println("OpenGL Error after setting uniforms (SelectorBox::render): " + error);

            GL41.glDrawArrays(GL41.GL_LINES, 0, BOX_VERTICES.length / 3);

            error = GL41.glGetError();
            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after glDrawArrays (SelectorBox::render): " + error);

            shader.unbind();

            error = GL41.glGetError();
            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after unbind shader (SelectorBox::render): " + error);

            GL41.glDisableVertexAttribArray(0);
            GL41.glBindVertexArray(0);

            error = GL41.glGetError();
            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error after unbinding VAO (SelectorBox::render): " + error);
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