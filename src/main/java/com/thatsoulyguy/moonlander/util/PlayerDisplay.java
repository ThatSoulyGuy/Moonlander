package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.block.Block;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.item.Item;
import com.thatsoulyguy.moonlander.item.ItemRegistry;
import com.thatsoulyguy.moonlander.math.Transform;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.render.DefaultVertex;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.thread.MainThreadExecutor;
import com.thatsoulyguy.moonlander.world.TextureAtlasManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

@CustomConstructor("create")
public class PlayerDisplay extends Component
{
    public @Nullable Item currentItem;

    private @Nullable Item lastItem = null;

    public Vector3f cameraMovement = new Vector3f();
    public Vector3f playerMovement = new Vector3f();

    private Vector3f originalPosition = new Vector3f();
    private Vector3f originalRotation = new Vector3f();

    private boolean isSwinging = false;
    private float swingElapsed = 0.0f;

    private final Vector3f swingTargetPos = new Vector3f();
    private final Vector3f swingTargetRot = new Vector3f();

    private PlayerDisplay() { }

    @Override
    public void update()
    {
        if (isSwinging)
        {
            float delta = Time.getDeltaTime();
            swingElapsed += delta;

            Transform transform = getGameObject().getTransform();

            float swingDuration = 0.18f;
            float swingHalf = swingDuration / 2.0f;

            if (swingElapsed < swingHalf)
            {
                float factor = swingElapsed / swingHalf;

                transform.setLocalPosition(lerp(originalPosition, swingTargetPos, factor));
                transform.setLocalRotation(lerp(originalRotation, swingTargetRot, factor));
            }
            else if (swingElapsed < swingDuration)
            {
                float factor = (swingElapsed - swingHalf) / swingHalf;

                transform.setLocalPosition(lerp(swingTargetPos, originalPosition, factor));
                transform.setLocalRotation(lerp(swingTargetRot, originalRotation, factor));
            }
            else
            {
                transform.setLocalPosition(new Vector3f(originalPosition));
                transform.setLocalRotation(new Vector3f(originalRotation));

                isSwinging = false;
            }
        }
        else
        {
            Transform transform = getGameObject().getTransform();

            Vector3f combinedOffset = new Vector3f(cameraMovement).add(playerMovement);

            Vector3f targetPos = new Vector3f(originalPosition).add(combinedOffset);

            float smoothingFactor = 5.0f * Time.getDeltaTime();

            Vector3f currentPos = transform.getLocalPosition();
            Vector3f newPos = lerp(currentPos, targetPos, smoothingFactor);

            transform.setLocalPosition(newPos);
        }

        if (currentItem == null || (lastItem != null && lastItem == currentItem))
            return;

        lastItem = currentItem;

        MainThreadExecutor.submit(() ->
        {
            List<DefaultVertex> vertices = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();

            Mesh mesh = getGameObject().getComponentNotNull(Mesh.class);

            mesh.setInFront(true);

            if (currentItem == ItemRegistry.ITEM_AIR)
            {
                originalPosition = new Vector3f(1.7f, -2.0f, -2.6f);
                originalRotation = new Vector3f(0.0f, 100.0f, 40.0f);

                getGameObject().getTransform().setLocalPosition(originalPosition);
                getGameObject().getTransform().setLocalRotation(originalRotation);

                Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.0f, 0.0f), color, new Vector3f(0, 0, -1), new Vector2f(0, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.0f, 0.0f), color, new Vector3f(0, 0, -1), new Vector2f(1, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.5f, 0.0f), color, new Vector3f(0, 0, -1), new Vector2f(1, 1)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.5f, 0.0f), color, new Vector3f(0, 0, -1), new Vector2f(0, 1)));

                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.0f, 0.5f), color, new Vector3f(0, 0, 1), new Vector2f(0, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.0f, 0.5f), color, new Vector3f(0, 0, 1), new Vector2f(1, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.5f, 0.5f), color, new Vector3f(0, 0, 1), new Vector2f(1, 1)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.5f, 0.5f), color, new Vector3f(0, 0, 1), new Vector2f(0, 1)));

                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.0f, 0.5f), color, new Vector3f(-1, 0, 0), new Vector2f(0, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.0f, 0.0f), color, new Vector3f(-1, 0, 0), new Vector2f(1, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.5f, 0.0f), color, new Vector3f(-1, 0, 0), new Vector2f(1, 1)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.5f, 0.5f), color, new Vector3f(-1, 0, 0), new Vector2f(0, 1)));

                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.0f, 0.0f), color, new Vector3f(1, 0, 0), new Vector2f(0, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.0f, 0.5f), color, new Vector3f(1, 0, 0), new Vector2f(1, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.5f, 0.5f), color, new Vector3f(1, 0, 0), new Vector2f(1, 1)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.5f, 0.0f), color, new Vector3f(1, 0, 0), new Vector2f(0, 1)));

                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.0f, 0.5f), color, new Vector3f(0, -1, 0), new Vector2f(0, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.0f, 0.5f), color, new Vector3f(0, -1, 0), new Vector2f(1, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.0f, 0.0f), color, new Vector3f(0, -1, 0), new Vector2f(1, 1)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.0f, 0.0f), color, new Vector3f(0, -1, 0), new Vector2f(0, 1)));

                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.5f, 0.0f), color, new Vector3f(0, 1, 0), new Vector2f(0, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.5f, 0.0f), color, new Vector3f(0, 1, 0), new Vector2f(1, 0)));
                vertices.add(DefaultVertex.create(new Vector3f(2.0f, 0.5f, 0.5f), color, new Vector3f(0, 1, 0), new Vector2f(1, 1)));
                vertices.add(DefaultVertex.create(new Vector3f(0.0f, 0.5f, 0.5f), color, new Vector3f(0, 1, 0), new Vector2f(0, 1)));

                indices.addAll(getCommonIndices());

                if (getGameObject().hasComponent(Texture.class) && !getGameObject().getComponentNotNull(Texture.class).getName().equals("entity.player_arm"))
                    getGameObject().getComponentNotNull(Texture.class).uninitialize_NoOverride();

                getGameObject().setComponent(Objects.requireNonNull(TextureManager.get("entity.player_arm")));
            }
            else if (currentItem.isBlockItem())
            {
                originalPosition = new Vector3f(1.0f, -1.4f, -3.5f);
                originalRotation = new Vector3f(15.0f, 45.0f, 15.0f);

                getGameObject().getTransform().setLocalPosition(originalPosition);
                getGameObject().getTransform().setLocalRotation(originalRotation);

                Block block = currentItem.getAssociatedBlock();

                List<Vector2f[]> uvs = Arrays.stream(block.getTextures())
                        .map(name -> Objects.requireNonNull(TextureAtlasManager.get("blocks")).getSubTextureCoordinates(name, 0))
                        .toList();

                Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

                vertices.add(DefaultVertex.create(new Vector3f(0, 0, 0), color, new Vector3f(0, 0, -1), uvs.getFirst()[0]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 0, 0), color, new Vector3f(0, 0, -1), uvs.getFirst()[1]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 1, 0), color, new Vector3f(0, 0, -1), uvs.getFirst()[2]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 1, 0), color, new Vector3f(0, 0, -1), uvs.getFirst()[3]));

                vertices.add(DefaultVertex.create(new Vector3f(1, 0, 1), color, new Vector3f(0, 0, 1), uvs.get(1)[0]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 0, 1), color, new Vector3f(0, 0, 1), uvs.get(1)[1]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 1, 1), color, new Vector3f(0, 0, 1), uvs.get(1)[2]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 1, 1), color, new Vector3f(0, 0, 1), uvs.get(1)[3]));

                vertices.add(DefaultVertex.create(new Vector3f(0, 0, 1), color, new Vector3f(-1, 0, 0), uvs.get(2)[0]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 0, 0), color, new Vector3f(-1, 0, 0), uvs.get(2)[1]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 1, 0), color, new Vector3f(-1, 0, 0), uvs.get(2)[2]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 1, 1), color, new Vector3f(-1, 0, 0), uvs.get(2)[3]));

                vertices.add(DefaultVertex.create(new Vector3f(1, 0, 0), color, new Vector3f(1, 0, 0), uvs.get(3)[0]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 0, 1), color, new Vector3f(1, 0, 0), uvs.get(3)[1]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 1, 1), color, new Vector3f(1, 0, 0), uvs.get(3)[2]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 1, 0), color, new Vector3f(1, 0, 0), uvs.get(3)[3]));

                vertices.add(DefaultVertex.create(new Vector3f(0, 0, 1), color, new Vector3f(0, -1, 0), uvs.get(4)[0]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 0, 1), color, new Vector3f(0, -1, 0), uvs.get(4)[1]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 0, 0), color, new Vector3f(0, -1, 0), uvs.get(4)[2]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 0, 0), color, new Vector3f(0, -1, 0), uvs.get(4)[3]));

                vertices.add(DefaultVertex.create(new Vector3f(0, 1, 0), color, new Vector3f(0, 1, 0), uvs.get(5)[0]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 1, 0), color, new Vector3f(0, 1, 0), uvs.get(5)[1]));
                vertices.add(DefaultVertex.create(new Vector3f(1, 1, 1), color, new Vector3f(0, 1, 0), uvs.get(5)[2]));
                vertices.add(DefaultVertex.create(new Vector3f(0, 1, 1), color, new Vector3f(0, 1, 0), uvs.get(5)[3]));

                indices.addAll(getCommonIndices());

                if (getGameObject().hasComponent(Texture.class) && !getGameObject().getComponentNotNull(Texture.class).getName().equals("entity.player_arm"))
                {
                    getGameObject().getComponentNotNull(Texture.class).uninitialize_NoOverride();
                    getGameObject().removeComponent(Texture.class);
                }
                else if (getGameObject().hasComponent(Texture.class) && getGameObject().getComponentNotNull(Texture.class).getName().equals("entity.player_arm"))
                    getGameObject().removeComponent(Texture.class);

                getGameObject().addComponent(Objects.requireNonNull(TextureAtlasManager.get("blocks")));
            }
            else if (!currentItem.isBlockItem())
            {
                originalPosition = new Vector3f(0.7f, -1.0f, -2.0f);
                originalRotation = new Vector3f(0.0f, 33.0f, 0.0f);

                getGameObject().getTransform().setLocalPosition(originalPosition);
                getGameObject().getTransform().setLocalRotation(originalRotation);

                Texture texture = Objects.requireNonNull(TextureAtlasManager.get("items")).createSubTexture(currentItem.getTexture(), true);

                Pair<List<DefaultVertex>, List<Integer>> data = DataAlgorithms.extrudeTextureIntoMeshData(texture, 0.1f);

                vertices.addAll(data.t());
                indices.addAll(data.a());

                if (getGameObject().hasComponent(Texture.class) && !getGameObject().getComponentNotNull(Texture.class).getName().equals("entity.player_arm"))
                    getGameObject().getComponentNotNull(Texture.class).uninitialize_NoOverride();

                getGameObject().setComponent(texture);
            }

            mesh.setVertices(vertices);
            mesh.setIndices(indices);

            mesh.generate();
        });
    }

    public void swing()
    {
        swingTargetPos.set(originalPosition).add(0.0f, -0.2f, -1.2f);
        swingTargetRot.set(originalRotation).add(20.0f, 0.0f, 0.0f);

        swingElapsed = 0.0f;
        isSwinging = true;
    }

    private @NotNull Vector3f lerp(@NotNull Vector3f start, @NotNull Vector3f end, float factor)
    {
        return new Vector3f
        (
            start.x + factor * (end.x - start.x),
            start.y + factor * (end.y - start.y),
            start.z + factor * (end.z - start.z)
        );
    }

    private @NotNull List<Integer> getCommonIndices()
    {
        List<Integer> result = new ArrayList<>();

        result.add(2); result.add(1); result.add(0);
        result.add(3); result.add(2); result.add(0);

        result.add(6); result.add(5); result.add(4);
        result.add(7); result.add(6); result.add(4);

        result.add(10); result.add(9); result.add(8);
        result.add(11); result.add(10); result.add(8);

        result.add(14); result.add(13); result.add(12);
        result.add(15); result.add(14); result.add(12);

        result.add(18); result.add(17); result.add(16);
        result.add(19); result.add(18); result.add(16);

        result.add(22); result.add(21); result.add(20);
        result.add(23); result.add(22); result.add(20);

        return result;
    }

    public static @NotNull PlayerDisplay create()
    {
        return new PlayerDisplay();
    }
}