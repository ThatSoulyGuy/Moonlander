package com.thatsoulyguy.moonlander.ui.elements;

import com.thatsoulyguy.moonlander.input.InputManager;
import com.thatsoulyguy.moonlander.input.MouseCode;
import com.thatsoulyguy.moonlander.input.MouseState;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.ui.UIElement;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ButtonUIElement extends UIElement
{
    private String onLeftPressedCallback;
    private String onRightPressedCallback;
    private String onHoverBeginCallback;
    private String onHoverEndCallback;
    private String onLeftReleasedCallback;
    private String onRightReleasedCallback;

    private boolean isHovering;

    @Override
    public void update()
    {
        boolean inside = isMouseWithinSelf();

        if (inside && !isHovering)
        {
            isHovering = true;

            if (onHoverBeginCallback != null && !onHoverBeginCallback.isEmpty())
                invokeMethod(onHoverBeginCallback);
        }
        else if (!inside && isHovering)
        {
            isHovering = false;

            if (onHoverEndCallback != null && !onHoverEndCallback.isEmpty())
                invokeMethod(onHoverEndCallback);
        }

        if (inside)
        {
            if (InputManager.getMouseState(MouseCode.LEFT, MouseState.PRESSED))
            {
                if (onLeftPressedCallback != null && !onLeftPressedCallback.isEmpty())
                    invokeMethod(onLeftPressedCallback);
            }

            if (InputManager.getMouseState(MouseCode.LEFT, MouseState.PRESSED))
            {
                if (onLeftReleasedCallback != null && !onLeftReleasedCallback.isEmpty())
                    invokeMethod(onLeftReleasedCallback);
            }

            if (InputManager.getMouseState(MouseCode.RIGHT, MouseState.PRESSED))
            {
                if (onRightPressedCallback != null && !onRightPressedCallback.isEmpty())
                    invokeMethod(onRightPressedCallback);
            }

            if (InputManager.getMouseState(MouseCode.RIGHT, MouseState.PRESSED))
            {
                if (onRightReleasedCallback != null && !onRightReleasedCallback.isEmpty())
                    invokeMethod(onRightReleasedCallback);
            }
        }
    }

    private boolean isMouseWithinSelf()
    {
        Vector2f mousePosition = InputManager.getMousePosition();

        Vector3f position = getGameObject().getTransform().getWorldPosition();
        Vector3f dimensions = getGameObject().getTransform().getWorldScale();

        float halfWidth = dimensions.x / 2f;
        float halfHeight = dimensions.y / 2f;
        float left = position.x - halfWidth;
        float right = position.x + halfWidth;
        float bottom = position.y - halfHeight;
        float top = position.y + halfHeight;

        return (mousePosition.x >= left && mousePosition.x <= right && mousePosition.y >= bottom && mousePosition.y <= top);
    }

    @Override
    public void onGenerate(@NotNull Mesh mesh)
    {
        mesh.setTransparent(true);
    }

    public @NotNull String getOnLeftPressedCallback()
    {
        return onLeftPressedCallback;
    }

    public void setOnLeftPressedCallback(@NotNull String onLeftPressedCallback)
    {
        this.onLeftPressedCallback = onLeftPressedCallback;
    }

    public @NotNull String getOnRightPressedCallback()
    {
        return onRightPressedCallback;
    }

    public void setOnRightPressedCallback(@NotNull String onRightPressedCallback)
    {
        this.onRightPressedCallback = onRightPressedCallback;
    }

    public @NotNull String getOnHoverBeginCallback()
    {
        return onHoverBeginCallback;
    }

    public void setOnHoverBeginCallback(@NotNull String onHoverBeginCallback)
    {
        this.onHoverBeginCallback = onHoverBeginCallback;
    }

    public @NotNull String getOnHoverEndCallback()
    {
        return onHoverEndCallback;
    }

    public void setOnHoverEndCallback(@NotNull String onHoverEndCallback)
    {
        this.onHoverEndCallback = onHoverEndCallback;
    }

    public @NotNull String getOnLeftReleasedCallback()
    {
        return onLeftReleasedCallback;
    }

    public void setOnLeftReleasedCallback(@NotNull String onLeftReleasedCallback)
    {
        this.onLeftReleasedCallback = onLeftReleasedCallback;
    }

    public @NotNull String getOnRightReleasedCallback()
    {
        return onRightReleasedCallback;
    }

    public void setOnRightReleasedCallback(@NotNull String onRightReleasedCallback)
    {
        this.onRightReleasedCallback = onRightReleasedCallback;
    }

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

    private void invokeMethod(@NotNull String reference)
    {
        if (reference.equals("null"))
            return;

        try
        {
            String[] parts = reference.split("::");

            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid reference format. Expected 'ClassName::methodName'");

            String className = parts[0];
            String methodName = parts[1];

            Class<?> clazz = Class.forName(className);

            Method method = clazz.getDeclaredMethod(methodName, ButtonUIElement.class);
            method.setAccessible(true);

            if (Modifier.isStatic(method.getModifiers()))
                method.invoke(null, this);
            else
                throw new IllegalArgumentException("Function is not static!");
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}