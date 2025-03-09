package com.thatsoulyguy.moonlander.entity;

import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class LivingEntity extends Entity
{
    private static final List<Class<? extends LivingEntity>> LIVING_ENTITY_CLASS_TYPES = new ArrayList<>();

    private int currentHealth;

    protected LivingEntity()
    {
        currentHealth = getMaximumHealth();
    }

    @Override
    public void initialize()
    {
        super.initialize();

        LIVING_ENTITY_CLASS_TYPES.add(getClass());
    }

    public abstract float getWalkingSpeed();

    public abstract float getRunningSpeed();

    public abstract int getMaximumHealth();

    public String[] getHurtAudioClips()
    {
        return new String[]
        {
            "entity.damage.0",
            "entity.damage.1",
            "entity.damage.2"
        };
    }

    public final void setCurrentHealth(int currentHealth)
    {
        this.currentHealth = currentHealth;
    }

    public final int getCurrentHealth()
    {
        return currentHealth;
    }

    public final void damage(@NotNull Entity damager, int damage)
    {
        currentHealth -= Math.abs(damage);

        GameObject soundObject = GameObject.create("entity.damage" + new Random().nextInt(4096), Layer.DEFAULT);

        soundObject.addComponent(Objects.requireNonNull(AudioManager.get(getHurtAudioClips()[new Random().nextInt(getHurtAudioClips().length)])));

        soundObject.getTransform().setLocalPosition(getGameObject().getTransform().getLocalPosition());

        AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

        clip.setVolume(20.0f);
        clip.setLooping(false);
        clip.play(true);

        onDamaged(Objects.requireNonNull(World.getLocalWorld()), damager, Math.abs(damage));
    }

    public final void heal(@NotNull Entity damager, int damage)
    {
        currentHealth += Math.abs(damage);

        onHealed(Objects.requireNonNull(World.getLocalWorld()), damager, Math.abs(damage));
    }

    public static @NotNull List<Class<? extends LivingEntity>> getLivingEntityClassTypes()
    {
        return List.copyOf(LIVING_ENTITY_CLASS_TYPES);
    }

    public void onDamaged(@NotNull World world, @NotNull Entity damager, int damageDealt) { }

    public void onHealed(@NotNull World world, @NotNull Entity healer, int damageHealed) { }
}