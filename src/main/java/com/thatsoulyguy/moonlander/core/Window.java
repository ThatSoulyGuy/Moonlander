package com.thatsoulyguy.moonlander.core;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.input.InputManager;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Static
public class Window
{
    private static final @NotNull List<Runnable> onResizeCompleted = new ArrayList<>();

    private static long handle;

    private Window() { }

    public static void initialize(@NotNull String title, @NotNull Vector2i dimensions)
    {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        handle = GLFW.glfwCreateWindow(dimensions.x, dimensions.y, title, 0, 0);

        if(handle == 0)
            throw new IllegalStateException("Failed to create window");

        GLFW.glfwMakeContextCurrent(handle);

        GLFW.glfwSetFramebufferSizeCallback(handle, (_, width, height) ->
        {
            GL41.glViewport(0, 0, width, height);
            onResizeCompleted.forEach(Runnable::run);
        });

        GLFW.glfwSetKeyCallback(handle, InputManager.getKeyCallback());
        GLFW.glfwSetMouseButtonCallback(handle, InputManager.getMouseButtonCallback());
        GLFW.glfwSetCursorPosCallback(handle, InputManager.getMousePositionCallback());
        GLFW.glfwSetScrollCallback(handle, InputManager.getScrollCallback());

        GLFW.glfwShowWindow(handle);

        GLFW.glfwSwapInterval(1);

        GL.createCapabilities();

        GL41.glEnable(GL41.GL_DEPTH_TEST);

        GL41.glEnable(GL41.GL_CULL_FACE);
        GL41.glCullFace(GL41.GL_BACK);
    }

    public static void preRender()
    {
        GL41.glClearColor(0.0f, 0.45f, 0.75f, 1.0f);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);
    }

    public static void postRender()
    {
        GLFW.glfwPollEvents();
        GLFW.glfwSwapBuffers(handle);
    }

    public static boolean shouldClose()
    {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public static void addOnResizeCompletedCallback(@NotNull Runnable runnable)
    {
        onResizeCompleted.add(runnable);
    }

    public static void setTitle(@NotNull String title)
    {
        GLFW.glfwSetWindowTitle(handle, title);
    }

    public static @NotNull String getTitle()
    {
        return Objects.requireNonNull(GLFW.glfwGetWindowTitle(handle));
    }

    public static void setDimensions(@NotNull Vector2i dimensions)
    {
        GLFW.glfwSetWindowSize(handle, dimensions.x, dimensions.y);
    }

    public static @NotNull Vector2i getDimensions()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer framebufferWidth = memoryStack.mallocInt(1);
            IntBuffer framebufferHeight = memoryStack.mallocInt(1);

            GLFW.glfwGetFramebufferSize(handle, framebufferWidth, framebufferHeight);

            return new Vector2i(framebufferWidth.get(0), framebufferHeight.get(0));
        }
    }

    public static long getHandle()
    {
        return handle;
    }

    public static void setPosition(@NotNull Vector2i position)
    {
        GLFW.glfwSetWindowPos(handle, position.x, position.y);
    }

    public static @NotNull Vector2i getPosition()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);

            GLFW.glfwGetWindowPos(handle, x, y);

            return new Vector2i(x.get(), y.get());
        }
    }

    public static void exit()
    {
        GLFW.glfwSetWindowShouldClose(handle, true);
    }

    public static void uninitialize()
    {
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
    }
}