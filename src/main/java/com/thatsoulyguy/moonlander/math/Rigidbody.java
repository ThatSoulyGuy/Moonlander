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

    private final @NotNull Vector3f desiredVelocity = new Vector3f(0, 0, 0);

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

        groundedCheck = groundedCheckObject.addComponent(
                Collider.create(BoxCollider.class)
                        .setSize(new Vector3f(self.getSize().x - 0.001f, self.getSize().y, self.getSize().z - 0.001f))
        );

        groundedCheckObject.getTransform().translate(new Vector3f(0.0f, -0.01f, 0.0f));

        groundedCheck.setCollidable(false);
    }

    @Override
    public void update()
    {
        float dt = Time.getDeltaTime();

        if (!isGrounded)
            desiredVelocity.y += GRAVITY * dt;

        desiredVelocity.x *= (float)Math.pow(DRAG, dt);
        desiredVelocity.z *= (float)Math.pow(DRAG, dt);

        BoxCollider self = getGameObject().getComponent(BoxCollider.class);
        List<Collider> colliders = ColliderManager.getAll().stream()
                .filter(c -> c != self)
                .filter(c -> c.getPosition()
                        .distance(getGameObject().getTransform().getWorldPosition()) < 32)
                .toList();

        previousPosition.set(getGameObject().getTransform().getWorldPosition());

        float dx = desiredVelocity.x * dt;
        float dz = desiredVelocity.z * dt;
        float dy = desiredVelocity.y * dt;

        resolveAxis(dx, 0, self, colliders);
        resolveAxis(dz, 2, self, colliders);

        isGrounded = resolveAxis(dy, 1, self, colliders);

        if (isGrounded)
            desiredVelocity.y = 0f;

        currentPosition.set(getGameObject().getTransform().getWorldPosition());
    }

    private boolean resolveAxis(float displacement, int axis, BoxCollider self, List<Collider> colliders)
    {
        final float EPS = 1e-5f;

        Vector3f move = new Vector3f();

        if (axis == 0)
            move.x = displacement;
        else if (axis == 1)
            move.y = displacement;
        else
            move.z = displacement;

        getGameObject().getTransform().translate(move);

        Vector3f totalCorr = new Vector3f();

        for (Collider other : colliders)
        {
            Vector3f corr;

            if (other instanceof VoxelMeshCollider vm)
                corr = Collider.resolveAllCollisions(self, vm);
            else
            {
                if (!self.intersects(other)) continue;
                corr = self.resolve(other, true);
            }

            if (corr.length() < EPS)
                continue;

            float c = (axis == 0 ? corr.x : axis == 1 ? corr.y : corr.z);

            Vector3f axisCorr = new Vector3f();

            if (axis == 0)
                axisCorr.x = c;
            else if (axis == 1)
                axisCorr.y = c;
            else
                axisCorr.z = c;

            totalCorr.add(axisCorr);
        }

        if (totalCorr.length() < EPS)
            return false;

        getGameObject().getTransform().translate(totalCorr);

        if (axis == 1 && totalCorr.y > EPS)
            return true;

        Vector3f normal = new Vector3f(totalCorr).normalize();
        float velAlong = desiredVelocity.dot(normal);

        if (velAlong < 0f)
        {
            Vector3f proj = new Vector3f(normal).mul(velAlong);
            desiredVelocity.sub(proj);
        }

        return false;
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
     * Adds force to the Rigidbody.
     *
     * @param force The force to add.
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