package com.thatsoulyguy.moonlander.render.advanced.core.renderpasses;

import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.render.Camera;
import com.thatsoulyguy.moonlander.render.Shader;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.render.Skybox;
import com.thatsoulyguy.moonlander.render.advanced.core.Framebuffer;
import com.thatsoulyguy.moonlander.render.advanced.core.RenderPass;
import com.thatsoulyguy.moonlander.render.advanced.core.framebuffers.GeometryFrameBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;

public class GeometryRenderPass implements RenderPass
{
    private GeometryFrameBuffer gBuffer;
    private Shader geometryShader;

    @Override
    public void initialize()
    {
        gBuffer = Framebuffer.create(GeometryFrameBuffer.class);
        geometryShader = ShaderManager.get("pass.geometry");
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        gBuffer.bind();

        Vector2i dimensions = Window.getDimensions();
        GL41.glViewport(0, 0, dimensions.x, dimensions.y);

        GL41.glClearColor(0, 0.0f, 0.0f, 1);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);

        geometryShader.bind();
    }

    public void endRender()
    {
        geometryShader.unbind();
    }

    @Override
    public void uninitialize()
    {
        gBuffer.uninitialize();
        geometryShader.uninitialize();
    }

    public @NotNull Shader getGeometryShader()
    {
        return geometryShader;
    }

    public int getPositionTex()
    {
        return gBuffer.getColorAttachments().get("gPosition");
    }

    public int getNormalTex()
    {
        return gBuffer.getColorAttachments().get("gNormal");
    }

    public int getAlbedoTex()
    {
        return gBuffer.getColorAttachments().get("gAlbedo");
    }
}