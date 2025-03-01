package com.thatsoulyguy.moonlander.entity.model;

import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EntityModel extends Component
{
    private final Map<String, ModelPart> partsList = new ConcurrentHashMap<>();

    public abstract void initialize();

    public abstract @NotNull Texture getTexture();

    public void addPart(@NotNull ModelPart part)
    {
        GameObject gameObject = getGameObject().addChild(GameObject.create("default." + part.getName(), Layer.DEFAULT));

        gameObject.addComponent(Objects.requireNonNull(ShaderManager.get("pass.geometry")));
        gameObject.addComponent(getTexture());
        gameObject.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));
        gameObject.addComponent(part);

        partsList.put("default." + part.getName(), part);
    }

    public @Nullable ModelPart getPart(@NotNull String name)
    {
        return partsList.getOrDefault("default." + name, null);
    }

    public void removePart(@NotNull String name)
    {
        if (!partsList.containsKey(name))
        {
            System.err.println("Failed to find key: '" + name + "' in parts list for model: '" + getGameObject().getName() + "'!");
            return;
        }

        GameObjectManager.unregister("default." + name, true);

        partsList.remove(name);
    }

    public void setTexture(@NotNull Texture texture)
    {
        partsList.values().forEach(modelPart -> modelPart.getGameObject().setComponent(texture));
    }

    public static <T extends EntityModel> @NotNull T create(Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from EntityModel! This shouldn't happen!");

            return clazz.cast(new Object());
        }
    }
}