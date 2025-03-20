package com.thatsoulyguy.moonlander.input;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public enum MouseCode
{
    LEFT(GLFW.GLFW_MOUSE_BUTTON_1),
    RIGHT(GLFW.GLFW_MOUSE_BUTTON_2),
    MIDDLE(GLFW.GLFW_MOUSE_BUTTON_3),
    UNKNOWN(-1);

    private final int value;

    MouseCode(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static @NotNull MouseCode fromGLFWKey(int glfwKey)
    {
        for (MouseCode mouseCode : MouseCode.values())
        {
            if (mouseCode.getValue() == glfwKey)
                return mouseCode;
        }

        return MouseCode.UNKNOWN;
    }
}