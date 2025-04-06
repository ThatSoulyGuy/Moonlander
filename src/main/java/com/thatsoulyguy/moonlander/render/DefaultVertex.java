package com.thatsoulyguy.moonlander.render;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL41;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

@CustomConstructor("create")
public class DefaultVertex implements Vertex
{
    private @EffectivelyNotNull Vector3f position;
    private @EffectivelyNotNull Vector3f color;
    private @EffectivelyNotNull Vector3f normal;
    private @EffectivelyNotNull Vector2f uvs;
    private static final VertexLayout LAYOUT;

    static
    {
        List<VertexAttribute> attributes = new ArrayList<>();

        attributes.add(new VertexAttribute("position", 3, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("color", 3, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("normal", 3, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("uvs", 2, GL41.GL_FLOAT, 0, false));

        LAYOUT = new VertexLayout(attributes, (3 + 3 + 3 + 2) * Float.BYTES);
    }

    private DefaultVertex() { }

    public @NotNull Vector3f getPosition()
    {
        return position;
    }

    public @NotNull Vector3f getColor()
    {
        return color;
    }

    public @NotNull Vector3f getNormal()
    {
        return normal;
    }

    public @NotNull Vector2f getUVs()
    {
        return uvs;
    }

    @Override
    public @NotNull VertexLayout getVertexLayout()
    {
        return LAYOUT;
    }

    @Override
    public void putAttributeData(int attributeIndex, @NotNull FloatBuffer buffer)
    {
        switch (attributeIndex)
        {
            case 0:
                buffer.put(position.x).put(position.y).put(position.z);
                break;

            case 1:
                buffer.put(color.x).put(color.y).put(color.z);
                break;

            case 2:
                buffer.put(normal.x).put(normal.y).put(normal.z);
                break;

            case 3:
                buffer.put(uvs.x).put(uvs.y);
                break;

            default:
                throw new IllegalArgumentException("Invalid attribute index: " + attributeIndex);
        }
    }

    public static @NotNull DefaultVertex create(Vector3f position, Vector3f color, Vector3f normal, Vector2f uvs)
    {
        DefaultVertex result = new DefaultVertex();

        result.position = position;
        result.color = color;
        result.normal = normal;
        result.uvs = uvs;

        return result;
    }
}