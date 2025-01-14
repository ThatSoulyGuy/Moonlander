package com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses;

import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.render.Camera;
import com.thatsoulyguy.moonlander.render.Shader;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.render.advanced.core.Framebuffer;
import com.thatsoulyguy.moonlander.render.advanced.core.RenderPass;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;

public class SSAOConcludingRenderPass implements RenderPass
{
    private Shader conclusionShader;

    private final int gPosition;
    private final int gNormal;
    private final int gAlbedo;
    private final int ssaoBlur;

    public SSAOConcludingRenderPass(int gPosition, int gNormal, int gAlbedo, int ssaoBlur)
    {
        this.gPosition = gPosition;
        this.gNormal = gNormal;
        this.gAlbedo = gAlbedo;
        this.ssaoBlur = ssaoBlur;
    }

    @Override
    public void initialize()
    {
        conclusionShader = ShaderManager.get("ssao.conclusion");
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);

        Vector2i dimensions = Window.getDimensions();

        GL41.glViewport(0, 0, dimensions.x, dimensions.y);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);

        conclusionShader.bind();

        GL41.glActiveTexture(GL41.GL_TEXTURE0);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, gPosition);
        conclusionShader.setUniform("gPosition", 0);

        GL41.glActiveTexture(GL41.GL_TEXTURE1);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, gNormal);
        conclusionShader.setUniform("gNormal", 1);

        GL41.glActiveTexture(GL41.GL_TEXTURE2);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, gAlbedo);
        conclusionShader.setUniform("gAlbedo", 2);

        GL41.glActiveTexture(GL41.GL_TEXTURE3);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, ssaoBlur);
        conclusionShader.setUniform("ssao", 3);

        Framebuffer.renderFullscreenQuadrilateral();

        conclusionShader.unbind();
    }

    @Override
    public void uninitialize()
    {
        conclusionShader.uninitialize();
    }
}