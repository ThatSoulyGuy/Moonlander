package com.thatsoulyguy.moonlander.ui.elements;

import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.ui.UIElement;
import org.jetbrains.annotations.NotNull;

public class ImageUIElement extends UIElement
{
    public void setTexture(@NotNull Texture texture)
    {
        if (getGameObject().hasComponent(Texture.class) && !TextureManager.has(getGameObject().getComponentNotNull(Texture.class).getName()))
            getGameObject().getComponentNotNull(Texture.class).uninitialize_NoOverride();

        getGameObject().setComponent(texture);
    }

    public @NotNull Texture getTexture()
    {
        return getGameObject().getComponentNotNull(Texture.class);
    }

    @Override
    public void onGenerate(@NotNull Mesh mesh)
    {
        mesh.setTransparent(true);
    }
}