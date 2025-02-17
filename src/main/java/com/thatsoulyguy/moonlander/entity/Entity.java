package com.thatsoulyguy.moonlander.entity;

import com.thatsoulyguy.moonlander.entity.model.EntityModel;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class Entity extends Component
{
    private int id;

    protected Entity() { }

    @Override
    public void initialize()
    {
        if (getModel() != null)
        {
            GameObject model = getGameObject().addChild(GameObject.create("default." + getClass().getName() + "_" + id + ".model", Layer.DEFAULT));

            model.getTransform().setLocalScale(new Vector3f((float) 1 / 16));

            model.addComponent(getModel());
        }
    }

    public final void setId(int id)
    {
        this.id = id;
    }

    public final int getId()
    {
        return id;
    }

    public abstract @NotNull String getDisplayName();

    public abstract @NotNull String getRegistryName();

    public abstract @NotNull Vector3f getBoundingBoxSize();

    public abstract <T extends EntityModel> @Nullable T getModel();

    public void onKilled(@NotNull World world, @NotNull Entity killer) { }

    public void onSpawned(@NotNull World world) { }

    public static <T extends Entity> @NotNull T create(@NotNull Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from Entity! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}