package com.thatsoulyguy.moonlander.gameplay;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.colliders.BoxCollider;
import com.thatsoulyguy.moonlander.render.*;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@CustomConstructor("create")
public class OxygenBubble extends Component
{
    private final @NotNull List<Vertex> vertices = new ArrayList<>();
    private final @NotNull List<Integer> indices = new ArrayList<>();

    private boolean oxygenActive;

    private transient int vao, vbo, cbo, nbo, uvbo, ibo;

    private OxygenBubble() { }

    @Override
    public void initialize()
    {
        float radius = 6.25f;
        int stacks = 8;
        int slices = 8;

        for (int i = 0; i <= stacks; i++)
        {
            float stackPercent = (float) i / (float) stacks;
            float phi = (float) (Math.PI * stackPercent);

            for (int j = 0; j <= slices; j++)
            {
                float slicePercent = (float) j / (float) slices;
                float theta = (float) (2.0f * Math.PI * slicePercent);

                float x = (float) (radius * Math.sin(phi) * Math.cos(theta));
                float y = (float) (radius * Math.cos(phi));
                float z = (float) (radius * Math.sin(phi) * Math.sin(theta));

                Vector3f position = new Vector3f(x, y, z);
                Vector3f normal = new Vector3f(position).normalize();

                float v = 1.0f - stackPercent;

                Vector2f uv = new Vector2f(slicePercent, v);

                vertices.add(Vertex.create(position, new Vector3f(1.0f, 1.0f, 1.0f), normal, uv));
            }
        }

        for (int i = 0; i < stacks; i++)
        {
            for (int j = 0; j < slices; j++)
            {
                int first = i * (slices + 1) + j;
                int second = (i + 1) * (slices + 1) + j;

                indices.add(first);
                indices.add(second);
                indices.add(first + 1);

                indices.add(second);
                indices.add(second + 1);
                indices.add(first + 1);
            }
        }

        MainThreadExecutor.submit(() ->
        {
            vao = GL41.glGenVertexArrays();
            GL41.glBindVertexArray(vao);

            vbo = GL41.glGenBuffers();
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vbo);
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getPosition), GL41.GL_DYNAMIC_DRAW);
            GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, 0, 0);
            GL41.glEnableVertexAttribArray(0);

            cbo = GL41.glGenBuffers();
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, cbo);
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getColor), GL41.GL_DYNAMIC_DRAW);
            GL41.glVertexAttribPointer(1, 3, GL41.GL_FLOAT, false, 0, 0);
            GL41.glEnableVertexAttribArray(1);

            nbo = GL41.glGenBuffers();
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, nbo);
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getNormal), GL41.GL_DYNAMIC_DRAW);
            GL41.glVertexAttribPointer(2, 3, GL41.GL_FLOAT, false, 0, 0);
            GL41.glEnableVertexAttribArray(2);

            uvbo = GL41.glGenBuffers();
            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, uvbo);
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, toBuffer(vertices, Vertex::getUVs), GL41.GL_DYNAMIC_DRAW);
            GL41.glVertexAttribPointer(3, 2, GL41.GL_FLOAT, false, 0, 0);
            GL41.glEnableVertexAttribArray(3);

            ibo = GL41.glGenBuffers();
            GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, toBuffer(indices), GL41.GL_DYNAMIC_DRAW);

            GL41.glBindVertexArray(0);
        });
    }

    @Override
    public void renderDefault(@Nullable Camera camera)
    {
        if (camera == null || !oxygenActive)
            return;

        GL41.glEnable(GL41.GL_BLEND);
        GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);

        Shader shader = ShaderManager.get("oxygen_bubble");

        GL41.glBindVertexArray(vao);

        GL41.glEnableVertexAttribArray(0);
        GL41.glEnableVertexAttribArray(1);
        GL41.glEnableVertexAttribArray(2);
        GL41.glEnableVertexAttribArray(3);

        shader.bind();

        shader.setUniform("diffuseTexture", 0);
        shader.setUniform("projection", camera.getProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());

        shader.setUniform("model", getGameObject().getTransform().getModelMatrix());

        GL41.glDrawElements(GL41.GL_TRIANGLES, indices.size(), GL41.GL_UNSIGNED_INT, 0);

        shader.unbind();

        GL41.glDisableVertexAttribArray(0);
        GL41.glDisableVertexAttribArray(1);
        GL41.glDisableVertexAttribArray(2);
        GL41.glDisableVertexAttribArray(3);
        GL41.glBindVertexArray(0);

        GL41.glDisable(GL41.GL_BLEND);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (renderDefault): " + error);
    }

    public void setOxygenActive(boolean active)
    {
        this.oxygenActive = active;
    }

    public boolean isOxygenActive()
    {
        return oxygenActive;
    }

    private static <T> FloatBuffer toBuffer(List<Vertex> vertices, Function<Vertex, T> extractor)
    {
        if (vertices.isEmpty())
            throw new IllegalArgumentException("The list of vertices cannot be empty.");

        Object sample = extractor.apply(vertices.getFirst());

        final int dimensions;

        if (sample instanceof Vector3f)
            dimensions = 3;
        else if (sample instanceof Vector2f)
            dimensions = 2;
        else
            throw new IllegalArgumentException("Unsupported vector type: " + sample.getClass().getName());

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * dimensions);

        for (Vertex vertex : vertices)
        {
            T vector = extractor.apply(vertex);
            if (vector instanceof Vector3f vec3)
                buffer.put(vec3.x).put(vec3.y).put(vec3.z);
            else if (vector instanceof Vector2f vec2)
                buffer.put(vec2.x).put(vec2.y);
        }

        buffer.flip();

        return buffer;
    }

    private static IntBuffer toBuffer(List<Integer> indices)
    {
        IntBuffer buffer = BufferUtils.createIntBuffer(indices.size());

        for (int index : indices)
            buffer.put(index);

        buffer.flip();

        return buffer;
    }

    @Override
    public void uninitialize()
    {
        MainThreadExecutor.submit(() ->
        {
            GL41.glDeleteVertexArrays(vao);
            GL41.glDeleteBuffers(vbo);
            GL41.glDeleteBuffers(cbo);
            GL41.glDeleteBuffers(nbo);
            GL41.glDeleteBuffers(uvbo);
            GL41.glDeleteBuffers(ibo);
        });

        OxygenBubbleManager.unregister(this);
    }

    public static @NotNull OxygenBubble create()
    {
        return new OxygenBubble();
    }

    public static @NotNull GameObject createGameObject(@NotNull String name, @NotNull Vector3f position)
    {
        GameObject object = GameObject.create(name, Layer.DEFAULT);

        object.getTransform().setLocalPosition(position);
        object.addComponent(OxygenBubble.create());
        object.addComponent(Collider.create(BoxCollider.class).setSize(new Vector3f(10.0f, 10.0f, 10.0f)));
        object.getComponentNotNull(BoxCollider.class).setCollidable(false);

        OxygenBubbleManager.register(object.getComponentNotNull(OxygenBubble.class));

        return object;
    }
}