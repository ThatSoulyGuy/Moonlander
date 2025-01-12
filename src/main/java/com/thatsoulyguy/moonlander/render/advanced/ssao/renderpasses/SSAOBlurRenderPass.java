package com.thatsoulyguy.moonlander.render.advanced.ssao.renderpasses;

import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.render.Camera;
import com.thatsoulyguy.moonlander.render.Shader;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.render.advanced.core.Framebuffer;
import com.thatsoulyguy.moonlander.render.advanced.core.RenderPass;
import com.thatsoulyguy.moonlander.render.advanced.ssao.framebuffers.SSAOBlurFramebuffer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL41;

public class SSAOBlurRenderPass implements RenderPass
{
    private SSAOBlurFramebuffer ssaoBlurBuffer;
    private Shader blurShader;

    private final int ssaoColor;

    public SSAOBlurRenderPass(int ssaoColor)
    {
        this.ssaoColor = ssaoColor;
    }

    @Override
    public void initialize()
    {
        ssaoBlurBuffer = Framebuffer.create(SSAOBlurFramebuffer.class);
        blurShader = ShaderManager.get("ssao.blur");
    }

    @Override
    public void render(@Nullable Camera camera)
    {
        if (camera == null)
            return;

        ssaoBlurBuffer.bind();

        Vector2i dims = Window.getDimensions();
        GL41.glViewport(0, 0, dims.x, dims.y);
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);

        blurShader.bind();

        GL41.glActiveTexture(GL41.GL_TEXTURE0);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, ssaoColor);

        blurShader.setShaderUniform("ssaoInput", 0);

        Framebuffer.renderFullscreenQuadrilateral();

        blurShader.unbind();
        ssaoBlurBuffer.unbind();
    }

    @Override
    public void uninitialize()
    {
        ssaoBlurBuffer.uninitialize();
        blurShader.uninitialize();
    }

    public int getBlurredSSAO()
    {
        return ssaoBlurBuffer.getColorAttachments().get("ssaoBlurColor");
    }
}