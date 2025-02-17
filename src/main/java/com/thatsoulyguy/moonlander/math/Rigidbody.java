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

    private final @NotNull Vector3f desiredVelocity = new Vector3f(0,0,0);

    private boolean isGrounded = false;
    private @EffectivelyNotNull BoxCollider groundedCheck;

    private final Vector3f previousPosition = new Vector3f();
    private final Vector3f currentPosition = new Vector3f();

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

        previousPosition.set(getGameObject().getTransform().getWorldPosition());

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
            desiredVelocity.y += GRAVITY * Time.getDeltaTime();

        desiredVelocity.x *= (float) Math.pow(DRAG, Time.getDeltaTime());
        desiredVelocity.z *= (float) Math.pow(DRAG, Time.getDeltaTime());

        Transform transform = getGameObject().getTransform();
        Vector3f currentPosition = transform.getWorldPosition();

        previousPosition.set(currentPosition);

        Vector3f newPosition = new Vector3f(
                currentPosition.x + desiredVelocity.x * Time.getDeltaTime(),
                currentPosition.y + desiredVelocity.y * Time.getDeltaTime(),
                currentPosition.z + desiredVelocity.z * Time.getDeltaTime()
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
                        desiredVelocity.set(0.0f, 0.0f, 0.0f);

                        if (resolution.y > 0)
                        {
                            collidedFromBelow = true;
                            desiredVelocity.y = 0.0f;
                        }
                        else if (resolution.y < 0)
                            desiredVelocity.y = 0.0f;
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
            desiredVelocity.y = 0.0f;

        this.currentPosition.set(transform.getWorldPosition());
    }

    /**
     * Calculates and returns the actual movement vector that was applied during the current update.
     * This is computed as the difference between the object's world position at the end of the update
     * and its position at the beginning.
     *
     * @return The actual displacement vector.
     */
    public Vector3f getActualMovement()
    {
        return new Vector3f(currentPosition).sub(previousPosition, new Vector3f());
    }

    /**
     * Adds force to the Rigidbody
     *
     * @param force The force to add
     */
    public void addForce(@NotNull Vector3f force)
    {
        desiredVelocity.x += force.x;
        desiredVelocity.y += force.y;
        desiredVelocity.z += force.z;
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
     * @param desiredVelocity The new velocity vector.
     */
    public void setDesiredVelocity(Vector3f desiredVelocity)
    {
        this.desiredVelocity.set(desiredVelocity);
    }

    /**
     * Gets the current velocity of the Rigidbody.
     *
     * @return The velocity vector.
     */
    public Vector3f getDesiredVelocity()
    {
        return new Vector3f(desiredVelocity);
    }

    public static @NotNull Rigidbody create()
    {
        return new Rigidbody();
    }
}