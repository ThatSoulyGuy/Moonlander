package com.thatsoulyguy.moonlander.entity.model;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.Vertex;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.util.DataAlgorithms;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;

@CustomConstructor("create")
public class ModelPart extends Component
{
    private String name;

    private Vector3f pivotIn = null;
    private Vector3f positionIn = null;
    private Vector3f rotationIn = null;

    private final List<Cube> cubes = new ArrayList<>();

    private ModelPart() { }

    @Override
    public void initialize()
    {
        if (pivotIn != null)
        {
            getGameObject().getTransform().setLocalPivot(pivotIn);
            pivotIn = null;
        }

        if (positionIn != null)
        {
            getGameObject().getTransform().setLocalPosition(positionIn);

            if (pivotIn == null)
                getGameObject().getTransform().setLocalPivot(positionIn);

            positionIn = null;
        }

        if (rotationIn != null)
        {
            getGameObject().getTransform().setLocalRotation(rotationIn);
            rotationIn = null;
        }

        Mesh mesh = getGameObject().getComponentNotNull(Mesh.class);

        Vector2i textureDimensions = getGameObject().getComponentNotNull(Texture.class).getDimensions();

        assert textureDimensions != null;

        List<Vertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (final Cube cube : cubes)
        {
            int baseIndex = vertices.size();

            Map<String, Vector3f[]> vertexPositions = DataAlgorithms.generateCubeVertices(cube.getSize());
            Map<String, Vector2f[]> faceUVs = DataAlgorithms.generateCubeUVs(cube.getSize(), textureDimensions, cube.getUVs());

            LinkedList<String> facesNames = new LinkedList<>(List.of
            (
                "top",
                "bottom",
                "front",
                "back",
                "right",
                "left"
            ));

            LinkedList<Vector3f> normals = new LinkedList<>(List.of
            (
                new Vector3f( 0.0f,  1.0f,  0.0f),
                new Vector3f( 0.0f, -1.0f,  0.0f),
                new Vector3f( 0.0f,  0.0f,  1.0f),
                new Vector3f( 0.0f,  0.0f, -1.0f),
                new Vector3f( 0.0f,  0.0f,  0.0f),
                new Vector3f(-1.0f,  0.0f,  0.0f)
            ));

            for (int f = 0; f < 6; f++)
            {
                String faceName = facesNames.get(f);
                Vector3f[] faceVertices = vertexPositions.get(faceName);
                Vector2f[] faceUV = faceUVs.get(faceName);
                Vector3f normal = normals.get(f);

                vertices.add(Vertex.create(new Vector3f(cube.getPosition()).add(faceVertices[0]), new Vector3f(1.0f, 1.0f, 1.0f), normal, faceUV[0]));
                vertices.add(Vertex.create(new Vector3f(cube.getPosition()).add(faceVertices[1]), new Vector3f(1.0f, 1.0f, 1.0f), normal, faceUV[1]));
                vertices.add(Vertex.create(new Vector3f(cube.getPosition()).add(faceVertices[2]), new Vector3f(1.0f, 1.0f, 1.0f), normal, faceUV[2]));
                vertices.add(Vertex.create(new Vector3f(cube.getPosition()).add(faceVertices[3]), new Vector3f(1.0f, 1.0f, 1.0f), normal, faceUV[3]));
            }

            for (int f = 0; f < 6; f++)
            {
                int offset = baseIndex + f * 4;

                indices.add(offset);
                indices.add(offset + 1);
                indices.add(offset + 2);

                indices.add(offset);
                indices.add(offset + 2);
                indices.add(offset + 3);
            }
        }

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        mesh.generate();
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull ModelPart setPivot(@NotNull Vector3f pivot)
    {
        pivotIn = pivot;

        return this;
    }

    public @NotNull Vector3f getPivot()
    {
        return getGameObject().getTransform().getLocalPivot();
    }

    public @NotNull ModelPart setPosition(@NotNull Vector3f position)
    {
        positionIn = position;

        return this;
    }

    public @NotNull Vector3f getPosition()
    {
        return getGameObject().getTransform().getLocalPosition();
    }

    public @NotNull ModelPart setRotation(@NotNull Vector3f rotation)
    {
        rotationIn = rotation;

        return this;
    }

    public @NotNull Vector3f getRotation()
    {
        return getGameObject().getTransform().getLocalRotation();
    }

    public @NotNull ModelPart addCube(@NotNull Cube cube)
    {
        cubes.add(cube);

        return this;
    }

    public static @NotNull ModelPart create(@NotNull String name)
    {
        ModelPart result = new ModelPart();

        result.name = name;

        return result;
    }

    @CustomConstructor("create")
    public static class Cube
    {
        private Vector3f position;
        private Vector3f size;
        private Vector2i uvs;

        private Cube() { }

        public @NotNull Vector2i getUVs()
        {
            return uvs;
        }

        public @NotNull Vector3f getPosition()
        {
            return position;
        }

        public @NotNull Cube setPosition(@NotNull Vector3f position)
        {
            this.position = position;

            return this;
        }

        public @NotNull Vector3f getSize()
        {
            return size;
        }

        public @NotNull Cube setSize(@NotNull Vector3f size)
        {
            this.size = size;

            return this;
        }

        public @NotNull Cube setUVs(@NotNull Vector2i uvs)
        {
            this.uvs = uvs;

            return this;
        }

        public static @NotNull Cube create()
        {
            Cube result = new Cube();

            result.position = new Vector3f(0.0f);
            result.size = new Vector3f(1.0f);
            result.uvs = new Vector2i(0);

            return result;
        }
    }
}