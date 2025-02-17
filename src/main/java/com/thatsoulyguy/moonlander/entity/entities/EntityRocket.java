package com.thatsoulyguy.moonlander.entity.entities;

import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.model.EntityModel;
import com.thatsoulyguy.moonlander.entity.model.models.ModelRocket;
import com.thatsoulyguy.moonlander.system.GameObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class EntityRocket extends Entity
{
    @Override
    public void initialize()
    {
        super.initialize();

        GameObject model = getGameObject().getChild("default." + getClass().getName() + "_" + getId() + ".model");

        model.getTransform().setLocalRotation(new Vector3f(180.0f, 0.0f, 0.0f));
        model.getTransform().setLocalPosition(new Vector3f(0.0f, -2.45f, 0.0f));
    }

    @Override
    public @NotNull String getDisplayName()
    {
        return "Rocket";
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
    @SuppressWarnings("unchecked")
    public <T extends EntityModel> @Nullable T getModel()
    {
        return (T) EntityModel.create(ModelRocket.class);
    }
}