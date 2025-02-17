package com.thatsoulyguy.moonlander.ui.uielements;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.ui.UIElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ImageUIElement extends UIElement
{
    private @EffectivelyNotNull GameObject object;

    @Override
    public void generate(@NotNull GameObject object)
    {
        object.addComponent(Objects.requireNonNull(ShaderManager.get("ui")));
        object.addComponent(Objects.requireNonNull(TextureManager.get("error")));

        object.addComponent(Mesh.create(DEFAULT_VERTICES, DEFAULT_INDICES));

        object.getComponentNotNull(Mesh.class).generate();

        this.object = object;
    }
}