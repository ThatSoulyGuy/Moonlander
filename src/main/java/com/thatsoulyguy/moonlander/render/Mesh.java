package com.thatsoulyguy.moonlander.render;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import com.thatsoulyguy.moonlander.world.TextureAtlas;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL41;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

@CustomConstructor("create")
public class Mesh extends Component
{
    private final @NotNull List<Vertex> vertices = new CopyOnWriteArrayList<>();
    private final @NotNull List<Integer> indices = new CopyOnWriteArrayList<>();
    private @NotNull List<Consumer<Shader>> shaderCalls;

    private transient int vao;
    private transient int[] vboIds;
    private transient int ibo;

    private boolean isTransparent = false;
    private boolean inFront = false;

    private transient CountDownLatch initializationLatch = new CountDownLatch(1);

    private volatile boolean isUninitialized = false;

    private Mesh() { }

    @Override
    public void initialize()
    {
        initializationLatch = new CountDownLatch(1);
        shaderCalls = new ArrayList<>();

        vao = -1;
        vboIds = null;
        ibo = -1;
    }

    @Override
    public void onLoad()
    {
        generate();
    }

    public void generate()
    {
        if (vao == -1)
        {
            MainThreadExecutor.submit(() ->
            {
                try
                {
                    createOrUpdateBuffers();
                }
                finally
                {
                    initializationLatch.countDown();
                }
            });
        }
        else
        {
            MainThreadExecutor.submit(() ->
            {
                try
                {
                    updateBufferData();
                }
                finally
                {
                    initializationLatch.countDown();
                }
            });
        }
    }

    public void addShaderCall(@NotNull Consumer<Shader> function)
    {
        shaderCalls.add(function);
    }

    @Override
    public void renderDefault(@Nullable Camera camera)
    {
        if (!checkInitialization() || camera == null)
            return;

        if (inFront)
            GL41.glDepthFunc(GL41.GL_ALWAYS);

        if (isTransparent)
        {
            GL41.glEnable(GL41.GL_BLEND);
            GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
        }

        Texture texture = getGameObject().getComponent(Texture.class);

        if (texture == null)
            texture = Objects.requireNonNull(getGameObject().getComponent(TextureAtlas.class)).getOutputTexture();

        Shader shader = getGameObject().getComponentNotNull(Shader.class);

        GL41.glBindVertexArray(vao);

        VertexLayout layout = vertices.isEmpty() ? null : vertices.getFirst().getVertexLayout();

        if (layout != null)
        {
            for (int i = 0; i < layout.attributes().size(); i++)
                GL41.glEnableVertexAttribArray(i);
        }

        assert texture != null;

        texture.bind(0);

        shader.bind();

        shader.setUniform("diffuse", 0);
        shader.setUniform("projection", camera.getProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("model", getGameObject().getTransform().getModelMatrix());

        shaderCalls.forEach(call -> call.accept(shader));
        shaderCalls.clear();

        GL41.glDrawElements(GL41.GL_TRIANGLES, indices.size(), GL41.GL_UNSIGNED_INT, 0);

        shader.unbind();
        texture.unbind();

        if (layout != null)
        {
            for (int i = 0; i < layout.attributes().size(); i++)
                GL41.glDisableVertexAttribArray(i);
        }

        GL41.glBindVertexArray(0);
        GL41.glDisable(GL41.GL_BLEND);
        GL41.glDepthFunc(GL41.GL_LESS);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (renderDefault): " + error);
    }

    @Override
    public void renderUI()
    {
        if (!checkInitialization())
            return;

        GL41.glDisable(GL41.GL_CULL_FACE);

        if (isTransparent)
        {
            GL41.glEnable(GL41.GL_BLEND);
            GL41.glBlendFunc(GL41.GL_SRC_ALPHA, GL41.GL_ONE_MINUS_SRC_ALPHA);
        }

        Texture texture = getGameObject().getComponent(Texture.class);
        if (texture == null)
            texture = Objects.requireNonNull(getGameObject().getComponent(TextureAtlas.class)).getOutputTexture();

        Shader shader = getGameObject().getComponent(Shader.class);

        if (texture == null || shader == null)
        {
            System.err.println("Shader or Texture component(s) missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        Vector2i windowDimensions = Window.getDimensions();

        int windowWidth = windowDimensions.x;
        int windowHeight = windowDimensions.y;

        Matrix4f projectionMatrix = new Matrix4f().ortho2D(0, windowWidth, windowHeight, 0);

        GL41.glBindVertexArray(vao);
        VertexLayout layout = vertices.isEmpty() ? null : vertices.getFirst().getVertexLayout();

        if (layout != null)
        {
            for (int i = 0; i < layout.attributes().size(); i++)
                GL41.glEnableVertexAttribArray(i);
        }

        texture.bind(0);
        shader.bind();
        shader.setUniform("diffuse", 0);
        shader.setUniform("projection", projectionMatrix);
        shader.setUniform("model", getGameObject().getTransform().getModelMatrix());

        GL41.glDrawElements(GL41.GL_TRIANGLES, indices.size(), GL41.GL_UNSIGNED_INT, 0);

        shader.unbind();
        texture.unbind();

        if (layout != null)
        {
            for (int i = 0; i < layout.attributes().size(); i++)
                GL41.glDisableVertexAttribArray(i);
        }

        GL41.glBindVertexArray(0);
        GL41.glEnable(GL41.GL_CULL_FACE);

        if (isTransparent)
            GL41.glDisable(GL41.GL_BLEND);

        int error = GL41.glGetError();

        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (renderUI): " + error);
    }

    @SuppressWarnings("unchecked")
    public <T extends Vertex> @NotNull List<T> getVertices()
    {
        return (List<T>) List.copyOf(vertices);
    }

    public @NotNull List<Integer> getIndices()
    {
        return List.copyOf(indices);
    }

    public <T extends Vertex> void setVertices(@NotNull List<T> vertices)
    {
        synchronized (this.vertices)
        {
            this.vertices.clear();
            this.vertices.addAll(vertices);
        }
    }

    public void setIndices(@NotNull List<Integer> indices)
    {
        synchronized (this.indices)
        {
            this.indices.clear();
            this.indices.addAll(indices);
        }
    }

    public void setTransparent(boolean transparent)
    {
        isTransparent = transparent;
    }

    public boolean isTransparent()
    {
        return isTransparent;
    }

    public boolean isInFront()
    {
        return inFront;
    }

    public void setInFront(boolean inFront)
    {
        this.inFront = inFront;
    }

    public void modify(@NotNull Consumer<List<Vertex>> vertexModifier, @NotNull Consumer<List<Integer>> indexModifier)
    {
        synchronized (this.vertices)
        {
            vertexModifier.accept(vertices);
            indexModifier.accept(indices);

            if (vertices.isEmpty() || indices.isEmpty())
                return;
            if (vao != -1)
                MainThreadExecutor.submit(this::updateBufferData);
            else
                MainThreadExecutor.submit(() ->
                {
                    try
                    {
                        createOrUpdateBuffers();
                    }
                    finally
                    {
                        initializationLatch.countDown();
                    }
                });
        }
    }

    private boolean checkInitialization()
    {
        return initializationLatch.getCount() == 0 && vao != -1 && vboIds != null && ibo != -1;
    }

    private void createOrUpdateBuffers()
    {
        List<Vertex> localVertices;
        List<Integer> localIndices;

        synchronized (vertices)
        {
            localVertices = new ArrayList<>(vertices);
        }

        synchronized (indices)
        {
            localIndices = new ArrayList<>(indices);
        }

        if (localVertices.isEmpty() || localIndices.isEmpty())
            return;

        vao = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vao);

        VertexLayout layout = localVertices.getFirst().getVertexLayout();

        int numAttributes = layout.attributes().size();

        vboIds = new int[numAttributes];

        for (int i = 0; i < numAttributes; i++)
        {
            vboIds[i] = GL41.glGenBuffers();

            GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboIds[i]);
            FloatBuffer buffer = toBufferForAttribute(localVertices, i, layout.attributes().get(i).count());
            GL41.glBufferData(GL41.GL_ARRAY_BUFFER, buffer, GL41.GL_DYNAMIC_DRAW);

            VertexAttribute attr = layout.attributes().get(i);

            GL41.glVertexAttribPointer(i, attr.count(), attr.type(), false, 0, 0);
            GL41.glEnableVertexAttribArray(i);
        }

        ibo = GL41.glGenBuffers();

        GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ibo);
        IntBuffer idxBuffer = toBuffer(localIndices);
        GL41.glBufferData(GL41.GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL41.GL_DYNAMIC_DRAW);

        GL41.glBindVertexArray(0);

        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR)
            System.err.println("OpenGL Error (createOrUpdateBuffers):" + error);
    }

    private void updateBufferData()
    {
        synchronized (this)
        {
            List<Vertex> localVertices;
            List<Integer> localIndices;

            synchronized (vertices)
            {
                localVertices = new ArrayList<>(vertices);
            }

            synchronized (indices)
            {
                localIndices = new ArrayList<>(indices);
            }

            if (localVertices.isEmpty() || localIndices.isEmpty())
                return;

            GL41.glBindVertexArray(vao);

            VertexLayout layout = localVertices.getFirst().getVertexLayout();

            int numAttributes = layout.attributes().size();

            for (int i = 0; i < numAttributes; i++) {
                GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, vboIds[i]);

                FloatBuffer buffer = toBufferForAttribute(localVertices, i, layout.attributes().get(i).count());
                resizeOrSubData(GL41.GL_ARRAY_BUFFER, buffer);
            }

            GL41.glBindBuffer(GL41.GL_ELEMENT_ARRAY_BUFFER, ibo);

            IntBuffer idxBuffer = toBuffer(localIndices);

            resizeOrSubData(GL41.GL_ELEMENT_ARRAY_BUFFER, idxBuffer);

            GL41.glBindVertexArray(0);

            int error = GL41.glGetError();

            if (error != GL41.GL_NO_ERROR)
                System.err.println("OpenGL Error (updateBufferData): " + error);
        }
    }

    private <T1 extends java.nio.Buffer> void resizeOrSubData(int target, @NotNull T1 data)
    {
        int newSize = data.capacity() * (data instanceof FloatBuffer ? Float.BYTES : Integer.BYTES);
        int currentSize = GL41.glGetBufferParameteri(target, GL41.GL_BUFFER_SIZE);

        if (newSize > currentSize)
            GL41.glBufferData(target, newSize, GL41.GL_DYNAMIC_DRAW);

        if (data instanceof FloatBuffer buffer)
            GL41.glBufferSubData(target, 0, buffer);
        else if (data instanceof IntBuffer buffer)
            GL41.glBufferSubData(target, 0, buffer);
    }

    private static <T2 extends Vertex> @NotNull FloatBuffer toBufferForAttribute(@NotNull List<T2> vertices, int attributeIndex, int count)
    {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * count);

        for (T2 vertex : vertices)
            vertex.putAttributeData(attributeIndex, buffer);

        buffer.flip();

        return buffer;
    }

    private static @NotNull IntBuffer toBuffer(@NotNull List<Integer> indices)
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
            synchronized (this)
            {
                if (isUninitialized)
                    return;

                isUninitialized = true;

                if (vao != -1)
                {
                    GL41.glDeleteVertexArrays(vao);
                    vao = -1;
                }

                if (vboIds != null)
                {
                    for (int id : vboIds)
                        GL41.glDeleteBuffers(id);

                    vboIds = null;
                }

                if (ibo != -1)
                {
                    GL41.glDeleteBuffers(ibo);
                    ibo = -1;
                }
            }
        });
    }

    public static @NotNull Mesh create(@NotNull List<Vertex> vertices, @NotNull List<Integer> indices)
    {
        Mesh result = new Mesh();

        result.vertices.clear();
        result.indices.clear();
        result.vertices.addAll(vertices);
        result.indices.addAll(indices);

        return result;
    }
}