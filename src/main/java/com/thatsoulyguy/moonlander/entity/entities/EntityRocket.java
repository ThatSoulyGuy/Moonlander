package com.thatsoulyguy.moonlander.entity.entities;

import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.model.EntityModel;
import com.thatsoulyguy.moonlander.entity.model.models.ModelRocket;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.math.Rigidbody;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.ui.menus.InventoryMenu;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Objects;

public class EntityRocket extends Entity
{
    private float countDown = 5;

    private boolean launch = false;
    private int fuelBucketCount = 20;

    @Override
    public void initialize()
    {
        super.initialize();

        GameObject model = getGameObject().getChild("default." + getClass().getName() + "_" + getId() + ".model");

        model.getTransform().setLocalRotation(new Vector3f(180.0f, 0.0f, 0.0f));
        model.getTransform().setLocalPosition(new Vector3f(0.0f, -2.45f, 0.0f));
    }

    @Override
    public void update()
    {
        if (launch)
        {
            if (countDown < 0)
                EntityPlayer.getLocalPlayer().setWinConditionMenuActive(true);

            getGameObject().getComponentNotNull(Rigidbody.class).addForce(new Vector3f(0.0f, 10 * Time.getDeltaTime(), 0.0f));

            countDown -= 0.01f;
        }
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "entity_rocket";
    }

    @Override
    public @NotNull Vector3f getBoundingBoxSize()
    {
        return new Vector3f(2.0f, 5.0f, 2.0f);
    }

    @Override
    public @Nullable Class<? extends EntityModel> getModelType()
    {
        return ModelRocket.class;
    }

    @Override
    public void onInteractedWith(@NotNull World world, @NotNull Entity interator)
    {
        if (interator instanceof EntityPlayer player)
        {
            if (fuelBucketCount >= 20)
            {
                World.getLocalWorld().killEntity(this, player.getId(), EntityPlayer.class);

                player.getCamera().getGameObject().getParent().removeChild(player.getCamera().getGameObject());
                GameObject camera = getGameObject().addChild(player.getCamera().getGameObject());

                camera.getTransform().setLocalRotation(new Vector3f(0.0f, 0.0f, 0.0f));
                camera.getTransform().setLocalPosition(new Vector3f(0.0f, 0.125f, 0.0f));

                player.getInventoryMenu().setFloatingTitleText("Rocket Launching...");

                launch = true;

                GameObject soundObject = camera.addChild(GameObject.create("entity.rocket_blowoff" + new Random().nextInt(4096), Layer.DEFAULT));

                soundObject.addComponent(Objects.requireNonNull(AudioManager.get("entity.rocket_blowoff")));

                soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

                AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                clip.setLooping(false);
                clip.play(true);
            }

            InventoryMenu.SlotData slot = player.getInventoryMenu().getSlot(new Vector2i(new Vector2i(0, player.getInventoryMenu().currentSlotSelected)));

            assert slot != null;

            if (slot.id() == ItemRegistry.ITEM_FUEL_BUCKET.getId())
            {
                fuelBucketCount += slot.count();

                player.getInventoryMenu().setSlot(new Vector2i(0, player.getInventoryMenu().currentSlotSelected), ItemRegistry.ITEM_EMPTY_BUCKET.getId(), (byte) 1);
                player.getInventoryMenu().setFloatingTitleText("Rocket Fuel: " + fuelBucketCount + "/20");
            }
        }
    }
}