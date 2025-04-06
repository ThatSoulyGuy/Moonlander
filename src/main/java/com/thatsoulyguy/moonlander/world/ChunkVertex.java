package com.thatsoulyguy.moonlander.world;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.render.Vertex;
import com.thatsoulyguy.moonlander.render.VertexAttribute;
import com.thatsoulyguy.moonlander.render.VertexLayout;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL41;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

@CustomConstructor("create")
public class ChunkVertex implements Vertex
{
    private Vector3f position;
    private Vector3f color;
    private Vector3f normal;
    private Vector2f uvs;
    private Vector2f atlasOffset;
    private Vector2f atlasTileSize;
    private static final VertexLayout LAYOUT;

    static
    {
        List<VertexAttribute> attributes = new ArrayList<>();

        attributes.add(new VertexAttribute("position", 3, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("color", 3, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("normal", 3, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("uvs", 2, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("atlasOffset", 2, GL41.GL_FLOAT, 0, false));
        attributes.add(new VertexAttribute("atlasTileSize", 2, GL41.GL_FLOAT, 0, false));

        LAYOUT = new VertexLayout(attributes, (3 + 3 + 3 + 2 + 2 + 2) * Float.BYTES);
    }

    private ChunkVertex() { }

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

    public @NotNull Vector2f getAtlasOffset()
    {
        return atlasOffset;
    }

    public @NotNull Vector2f getAtlasTileSize()
    {
        return atlasTileSize;
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

            case 4:
                buffer.put(atlasOffset.x).put(atlasOffset.y);
                break;

            case 5:
                buffer.put(atlasTileSize.x).put(atlasTileSize.y);
                break;

            default:
                throw new IllegalArgumentException("Invalid attribute index: " + attributeIndex);
        }
    }

    public static @NotNull ChunkVertex create(@NotNull Vector3f position, @NotNull Vector3f color, @NotNull Vector3f normal, @NotNull Vector2f uvs, @NotNull Vector2f atlasOffset, @NotNull Vector2f atlasTileSize)
    {
        ChunkVertex result = new ChunkVertex();

        result.position = position;
        result.color = color;
        result.normal = normal;
        result.uvs = uvs;
        result.atlasOffset = atlasOffset;
        result.atlasTileSize = atlasTileSize;

        return result;
    }
}