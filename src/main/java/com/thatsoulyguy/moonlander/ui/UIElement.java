package com.thatsoulyguy.moonlander.ui;

import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.ShaderManager;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.render.DefaultVertex;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class UIElement extends Component
{
    private static final List<DefaultVertex> DEFAULT_VERTICES = List.of(
            DefaultVertex.create(
                    new Vector3f(-0.5f, -0.5f, 0.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Vector3f(0.0f, 0.0f, -1.0f),
                    new Vector2f(0.0f, 0.0f)
            ),
            DefaultVertex.create(
                    new Vector3f(0.5f, -0.5f, 0.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Vector3f(0.0f, 0.0f, -1.0f),
                    new Vector2f(1.0f, 0.0f)
            ),
            DefaultVertex.create(
                    new Vector3f(0.5f, 0.5f, 0.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Vector3f(0.0f, 0.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f)
            ),
            DefaultVertex.create(
                    new Vector3f(-0.5f, 0.5f, 0.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Vector3f(0.0f, 0.0f, -1.0f),
                    new Vector2f(0.0f, 1.0f)
            )
    );

    private static final List<Integer> DEFAULT_INDICES = List.of(
            0, 1, 2,
            0, 2, 3
    );

    public void generate()
    {
        Mesh mesh = getGameObject().getComponentNotNull(Mesh.class);

        List<DefaultVertex> centeredVertices = centerPolygon(DEFAULT_VERTICES);
        mesh.setVertices(centeredVertices);
        mesh.setIndices(DEFAULT_INDICES);

        onGenerate(mesh);
        mesh.generate();
    }

    public abstract void onGenerate(@NotNull Mesh mesh);

    public static List<DefaultVertex> centerPolygon(List<DefaultVertex> vertices)
    {
        if (vertices.isEmpty())
            return vertices;

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (DefaultVertex vertex : vertices)
        {
            Vector3f pos = vertex.getPosition();

            if (pos.x < minX)
                minX = pos.x;

            if (pos.x > maxX)
                maxX = pos.x;

            if (pos.y < minY)
                minY = pos.y;

            if (pos.y > maxY)
                maxY = pos.y;
        }

        float centerX = (minX + maxX) / 2.0f;
        float centerY = (minY + maxY) / 2.0f;

        List<DefaultVertex> centered = new ArrayList<>();

        for (DefaultVertex vertex : vertices)
        {
            Vector3f pos = vertex.getPosition();
            Vector3f centeredPos = new Vector3f(pos.x - centerX, pos.y - centerY, pos.z);

            centered.add(DefaultVertex.create(centeredPos, vertex.getColor(), vertex.getNormal(), vertex.getUVs()));
        }

        return centered;
    }

    public static <T extends UIElement> @NotNull T create(@NotNull Class<T> clazz)
    {
        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            System.err.println("Missing constructor from UIElement! This shouldn't happen!");
            return clazz.cast(new Object());
        }
    }

    public static <T extends UIElement> @NotNull GameObject createGameObject(@NotNull String name, @NotNull Class<T> clazz, @NotNull Vector2f position, @NotNull Vector2f dimensions, @NotNull GameObject parent)
    {
        GameObject result = parent.addChild(GameObject.create(name, Layer.UI));

        result.getTransform().setLocalPosition(new Vector3f(position.x, position.y, 0.0f));
        result.getTransform().setLocalScale(new Vector3f(dimensions.x, dimensions.y, 0.0f));

        result.addComponent(Objects.requireNonNull(TextureManager.get("error")));
        result.addComponent(Objects.requireNonNull(ShaderManager.get("ui")));
        result.addComponent(Mesh.create(new ArrayList<>(), new ArrayList<>()));
        result.addComponent(create(clazz));

        result.getComponentNotNull(clazz).generate();

        result.setTransient(true);

        return result;
    }
}
