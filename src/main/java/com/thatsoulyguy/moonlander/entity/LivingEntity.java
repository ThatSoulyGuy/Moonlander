package com.thatsoulyguy.moonlander.entity;

import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;

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

        onDamaged(World.getLocalWorld(), damager, Math.abs(damage));
    }

    public final void heal(@NotNull Entity damager, int damage)
    {
        currentHealth += Math.abs(damage);

        onHealed(World.getLocalWorld(), damager, Math.abs(damage));
    }

    public void onDamaged(@NotNull World world, @NotNull Entity damager, int damageDealt) { }

    public void onHealed(@NotNull World world, @NotNull Entity healer, int damageHealed) { }
}