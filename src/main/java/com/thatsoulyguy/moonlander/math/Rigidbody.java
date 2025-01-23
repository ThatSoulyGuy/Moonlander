package com.thatsoulyguy.moonlander.math;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.collider.Collider;
import com.thatsoulyguy.moonlander.collider.ColliderManager;
import com.thatsoulyguy.moonlander.collider.colliders.BoxCollider;
import com.thatsoulyguy.moonlander.collider.colliders.VoxelMeshCollider;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

@CustomConstructor("create")
public class Rigidbody extends Component
{
    public static final float GRAVITY = -6.8f;
    public static final float DRAG = 0.01f;

    private static final float FIXED_DELTA = 1.0f / 60.0f;

    private final @NotNull Vector3f velocity = new Vector3f(0,0,0);

    private boolean isGrounded = false;
    private @EffectivelyNotNull BoxCollider groundedCheck;

    private Rigidbody() { }

    @Override
    public void initialize()
    {
        Collider self = getGameObject().getComponent(BoxCollider.class);

        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        GameObject groundedCheckObject = getGameObject().addChild(GameObject.create("default.grounded_check", Layer.DEFAULT));

        groundedCheck = groundedCheckObject.addComponent(Collider.create(BoxCollider.class).setSize(new Vector3f(self.getSize().x - 0.001f, self.getSize().y, self.getSize().z - 0.001f)));

        groundedCheckObject.getTransform().translate(new Vector3f(0.0f, -0.01f, 0.0f));

        groundedCheck.setCollidable(false);
    }

    @Override
    public void update()
    {
        Collider self = getGameObject().getComponent(BoxCollider.class);
        if (self == null)
        {
            System.err.println("Collider component missing from GameObject: '" + getGameObject().getName() + "'!");
            return;
        }

        if (!isGrounded)
            velocity.y += GRAVITY * FIXED_DELTA;

        velocity.x *= (float) Math.pow(DRAG, FIXED_DELTA);
        velocity.z *= (float) Math.pow(DRAG, FIXED_DELTA);

        Transform transform = getGameObject().getTransform();
        Vector3f currentPosition = transform.getWorldPosition();

        Vector3f newPosition = new Vector3f(
                currentPosition.x + velocity.x * FIXED_DELTA,
                currentPosition.y + velocity.y * FIXED_DELTA,
                currentPosition.z + velocity.z * FIXED_DELTA
        );

        transform.setLocalPosition(newPosition);

        List<Collider> colliders = ColliderManager.getAll().stream()
                .filter(c -> c != self)
                .filter(c -> c.getPosition().distance(transform.getWorldPosition()) < 32)
                .toList();

        boolean groundCheckHit = false;
        boolean collidedFromBelow = false;

        Vector3f totalResolution = new Vector3f();

        for (int iteration = 0; iteration < 10; iteration++)
        {
            Vector3f currentResolution = new Vector3f();
            boolean resolvedAny = false;

            for (Collider collider : colliders)
            {
                if (self.intersects(collider))
                {
                    Vector3f resolution = self.resolve(collider, true);

                    if (resolution.length() > 0.00001f)
                    {
                        currentResolution.add(resolution);
                        resolvedAny = true;

                        if (resolution.length() > 1.0f)
                            resolution.normalize().mul(1.0f);

                        transform.translate(resolution);
                        velocity.set(0.0f, 0.0f, 0.0f);

                        if (resolution.y > 0)
                        {
                            collidedFromBelow = true;
                            velocity.y = 0.0f;
                        }
                        else if (resolution.y < 0)
                            velocity.y = 0.0f;
                    }
                }
            }

            if (!resolvedAny)
                break;

            totalResolution.add(currentResolution);

            if (totalResolution.length() > 10.0f)
            {
                System.err.println("Warning: Excessive collision resolution detected! Total Resolution: " + totalResolution);
                break;
            }
        }

        for (Collider collider : colliders)
        {
            Vector3f boxPosition = groundedCheck.getPosition();
            Vector3f boxSize = groundedCheck.getSize();

            Vector3f boxMin = new Vector3f(boxPosition).sub(new Vector3f(boxSize).mul(0.5f));
            Vector3f boxMax = new Vector3f(boxPosition).add(new Vector3f(boxSize).mul(0.5f));

            if (collider instanceof VoxelMeshCollider meshCollider)
            {
                for (Vector3f voxel : meshCollider.getVoxels())
                {
                    Vector3f voxelWorldPosition = new Vector3f(meshCollider.getPosition()).add(voxel);

                    Vector3f voxelMin = new Vector3f(voxelWorldPosition).sub(0.5f, 0.5f, 0.5f);
                    Vector3f voxelMax = new Vector3f(voxelWorldPosition).add(0.5f, 0.5f, 0.5f);

                    if (Collider.intersectsGeneric(boxMin, boxMax, voxelMin, voxelMax))
                    {
                        groundCheckHit = true;
                        break;
                    }
                }

                if (groundCheckHit) break;
            }
        }

        isGrounded = collidedFromBelow || groundCheckHit;

        if (isGrounded)
            velocity.y = 0.0f;
    }


    /**
     * Adds force to the Rigidbody
     *
     * @param force The force to add
     */
    public void addForce(@NotNull Vector3f force)
    {
        velocity.x += force.x;
        velocity.y += force.y;
        velocity.z += force.z;
    }

    /**
     * Checks if the Rigidbody is currently grounded.
     *
     * @return True if grounded, false otherwise.
     */
    public boolean isGrounded()
    {
        return isGrounded;
    }

    /**
     * Sets the velocity of the Rigidbody.
     * This can be used to apply external forces or impulses.
     *
     * @param velocity The new velocity vector.
     */
    public void setVelocity(Vector3f velocity)
    {
        this.velocity.set(velocity);
    }

    /**
     * Gets the current velocity of the Rigidbody.
     *
     * @return The velocity vector.
     */
    public Vector3f getVelocity()
    {
        return new Vector3f(velocity);
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}