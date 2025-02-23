package com.thatsoulyguy.moonlander.entity;

import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;

import java.util.Objects;

public abstract class LivingEntity extends Entity
{
    private int currentHealth;

    protected LivingEntity()
    {
        currentHealth = getMaximumHealth();
    }

    public abstract float getWalkingSpeed();

    public abstract float getRunningSpeed();

    public abstract int getMaximumHealth();

    public void setCurrentHealth(int currentHealth)
    {
        this.currentHealth = currentHealth;
    }

    public int getCurrentHealth()
    {
        return currentHealth;
    }

    public final void damage(@NotNull Entity damager, int damage)
    {
        currentHealth -= Math.abs(damage);

        GameObject soundObject = GameObject.create("entity.damage" + new Random().nextInt(4096), Layer.DEFAULT);

        soundObject.addComponent(Objects.requireNonNull(AudioManager.get("entity.damage." + new Random().nextInt(2))));

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

    public void onDamaged(@NotNull World world, @NotNull Entity damager, int damageDealt) { }

    public void onHealed(@NotNull World world, @NotNull Entity healer, int damageHealed) { }
}