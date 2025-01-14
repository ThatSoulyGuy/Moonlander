package com.thatsoulyguy.moonlander.render;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL41;

@CustomConstructor("create")
public class Skybox
{
    public static @Nullable Skybox CURRENT_SKYBOX = null;

    private Texture cubemapTexture;

    private int vaoId;
    private int vboId;

    private Skybox() { }

    public void render(@NotNull Camera camera)
    {
        if (cubemapTexture == null)
        {
            System.err.println("Skybox texture not initialized!");
            return;
        }

        GL41.glDepthMask(false);

        cubemapTexture.bindCubeMap(0);

        Shader skyboxShader = ShaderManager.get("skybox");

        assert skyboxShader != null;

        skyboxShader.bind();

        Matrix4f viewMatrix = new Matrix4f(camera.getViewMatrix()).m30(0).m31(0).m32(0);
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        skyboxShader.setUniform("view", viewMatrix);
        skyboxShader.setUniform("projection", projectionMatrix);

        GL41.glBindVertexArray(vaoId);
        GL41.glDrawArrays(GL41.GL_TRIANGLES, 0, 36);
        GL41.glBindVertexArray(0);

        GL41.glDepthMask(true);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (Skybox::render): " + error);
    }

    public void uninitialize()
    {
        if (vaoId != 0)
        {
            GL41.glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }

        if (vboId != 0)
        {
            GL41.glDeleteBuffers(vboId);
            vboId = 0;
        }
    }

    public static @NotNull Skybox create(@NotNull Texture cubemapTexture)
    {
        Skybox result = new Skybox();

        result.cubemapTexture = cubemapTexture;

        float[] vertices =
                {
                        -1.0f,  1.0f, -1.0f,
                        -1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f,  1.0f, -1.0f,
                        -1.0f,  1.0f, -1.0f,

                        -1.0f, -1.0f,  1.0f,
                        -1.0f, -1.0f, -1.0f,
                        -1.0f,  1.0f, -1.0f,
                        -1.0f,  1.0f, -1.0f,
                        -1.0f,  1.0f,  1.0f,
                        -1.0f, -1.0f,  1.0f,

                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f,  1.0f,
                        1.0f,  1.0f,  1.0f,
                        1.0f,  1.0f,  1.0f,
                        1.0f,  1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,

                        -1.0f, -1.0f,  1.0f,
                        -1.0f,  1.0f,  1.0f,
                        1.0f,  1.0f,  1.0f,
                        1.0f,  1.0f,  1.0f,
                        1.0f, -1.0f,  1.0f,
                        -1.0f, -1.0f,  1.0f,

                        -1.0f,  1.0f, -1.0f,
                        1.0f,  1.0f, -1.0f,
                        1.0f,  1.0f,  1.0f,
                        1.0f,  1.0f,  1.0f,
                        -1.0f,  1.0f,  1.0f,
                        -1.0f,  1.0f, -1.0f,

                        -1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f,  1.0f,
                        1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f,
                        -1.0f, -1.0f,  1.0f,
                        1.0f, -1.0f,  1.0f
                };

        result.vaoId = GL41.glGenVertexArrays();
        result.vboId = GL41.glGenBuffers();

        GL41.glBindVertexArray(result.vaoId);

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, result.vboId);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertices, GL41.GL_STATIC_DRAW);

        GL41.glEnableVertexAttribArray(0);
        GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, 0);
        GL41.glBindVertexArray(0);

        return result;
    }
}