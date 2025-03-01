package com.thatsoulyguy.moonlander.math;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.util.SerializableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CustomConstructor("create")
public class Transform extends Component
{
    private @EffectivelyNotNull Vector3f localPosition;
    private @EffectivelyNotNull Vector3f localRotation;
    private @EffectivelyNotNull Vector3f localScale;
    private @EffectivelyNotNull Vector3f localPivot = new Vector3f(0, 0, 0);

    private @Nullable transient Transform parent;
    private @NotNull Matrix4f cachedModelMatrix = new Matrix4f();

    private final @NotNull Map<Object, SerializableConsumer<Vector3f>> onPositionChangedCallbacks = new ConcurrentHashMap<>();
    private final @NotNull Map<Object, SerializableConsumer<Vector3f>> onRotationChangedCallbacks = new ConcurrentHashMap<>();
    private final @NotNull Map<Object, SerializableConsumer<Vector3f>> onScaleChangedCallbacks = new ConcurrentHashMap<>();
    private final @NotNull Map<Object, SerializableConsumer<Vector3f>> onPivotChangedCallbacks = new ConcurrentHashMap<>();

    private transient @NotNull List<Transform> children = new ArrayList<>();
    private boolean dirty = true;

    private Transform() { }

    @Override
    public void initialize()
    {
        children = new ArrayList<>();
    }

    public <T extends Serializable> void addOnPositionChangedCallback(@NotNull T object, @NotNull SerializableConsumer<Vector3f> consumer)
    {
        onPositionChangedCallbacks.put(object, consumer);
    }

    public <T extends Serializable> void removeOnPositionChangedCallback(@NotNull T object)
    {
        onPositionChangedCallbacks.remove(object);
    }

    public <T extends Serializable> void addOnRotationChangedCallback(@NotNull T object, @NotNull SerializableConsumer<Vector3f> consumer)
    {
        onRotationChangedCallbacks.put(object, consumer);
    }

    public <T extends Serializable> void removeOnRotationChangedCallback(@NotNull T object)
    {
        onRotationChangedCallbacks.remove(object);
    }

    public <T extends Serializable> void addOnScaleChangedCallback(@NotNull T object, @NotNull SerializableConsumer<Vector3f> consumer)
    {
        onScaleChangedCallbacks.put(object, consumer);
    }

    public <T extends Serializable> void removeOnScaleChangedCallback(@NotNull T object)
    {
        onScaleChangedCallbacks.remove(object);
    }

    public <T extends Serializable> void addOnPivotChangedCallback(@NotNull T object, @NotNull SerializableConsumer<Vector3f> consumer)
    {
        onPivotChangedCallbacks.put(object, consumer);
    }

    public <T extends Serializable> void removeOnPivotChangedCallback(@NotNull T object)
    {
        onPivotChangedCallbacks.remove(object);
    }

    public void addChild(@NotNull Transform child)
    {
        child.setParent(this);
        children.add(child);
    }

    public void removeChild(@NotNull Transform child)
    {
        child.setParent(null);
        children.remove(child);
    }

    public void translate(@NotNull Vector3f translation)
    {
        if (!Float.isFinite(translation.x) || !Float.isFinite(translation.y) || !Float.isFinite(translation.z))
            return;

        localPosition.add(translation);
        onPositionChangedCallbacks.values().forEach(consumer -> consumer.accept(localPosition));

        markDirty();
    }

    public void rotate(@NotNull Vector3f rotation)
    {
        localRotation.add(rotation);
        onRotationChangedCallbacks.values().forEach(consumer -> consumer.accept(localRotation));

        markDirty();
    }

    public void scale(@NotNull Vector3f scale)
    {
        localScale.add(scale);
        onScaleChangedCallbacks.values().forEach(consumer -> consumer.accept(localScale));

        markDirty();
    }

    public @NotNull Vector3f getLocalPosition()
    {
        return new Vector3f(localPosition);
    }

    public void setLocalPosition(@NotNull Vector3f localPosition)
    {
        this.localPosition.set(localPosition);

        onPositionChangedCallbacks.values().forEach(consumer -> consumer.accept(this.localPosition));

        markDirty();
    }

    public @NotNull Vector3f getLocalRotation()
    {
        return new Vector3f(localRotation);
    }

    public @NotNull Vector3f getLocalRotationReference()
    {
        return localRotation;
    }

    public void setLocalRotation(@NotNull Vector3f localRotation)
    {
        this.localRotation.set(localRotation);

        onRotationChangedCallbacks.values().forEach(consumer -> consumer.accept(this.localRotation));

        markDirty();
    }

    public @NotNull Vector3f getLocalScale()
    {
        return new Vector3f(localScale);
    }
    public void setLocalScale(@NotNull Vector3f localScale)
    {
        this.localScale.set(localScale);

        onScaleChangedCallbacks.values().forEach(consumer -> consumer.accept(this.localScale));

        markDirty();
    }

    public @NotNull Vector3f getLocalPivot()
    {
        return new Vector3f(localPivot);
    }

    public void setLocalPivot(@NotNull Vector3f pivot)
    {
        this.localPivot.set(pivot);

        onPivotChangedCallbacks.values().forEach(consumer -> consumer.accept(this.localPivot));

        markDirty();
    }

    public @Nullable Transform getParent()
    {
        return parent;
    }

    public void setParent(@Nullable Transform parent)
    {
        this.parent = parent;

        markDirty();
    }

    public @NotNull Vector3f getForward()
    {
        return getModelMatrix().getColumn(2, new Vector3f()).negate().normalize();
    }

    public @NotNull Vector3f getRight()
    {
        return getModelMatrix().getColumn(0, new Vector3f()).normalize();
    }

    public @NotNull Vector3f getUp()
    {
        return getModelMatrix().getColumn(1, new Vector3f()).normalize();
    }

    public @NotNull Vector3f getWorldPosition()
    {
        return getModelMatrix().getTranslation(new Vector3f());
    }

    public @NotNull Vector3f getWorldRotation()
    {
        Quaternionf normalizedRotation = getModelMatrix().getNormalizedRotation(new Quaternionf());

        Vector3f eulerAngles = new Vector3f();
        normalizedRotation.getEulerAnglesXYZ(eulerAngles);

        return eulerAngles.mul((float)Math.toDegrees(1.0));
    }

    public @NotNull Vector3f getWorldScale()
    {
        return getModelMatrix().getScale(new Vector3f());
    }

    public @NotNull Matrix4f getModelMatrix()
    {
        if (dirty)
        {
            float rx = (float) Math.toRadians(localRotation.x);
            float ry = (float) Math.toRadians(localRotation.y);
            float rz = (float) Math.toRadians(localRotation.z);

            Matrix4f localMatrix = new Matrix4f()
                    .identity()
                    .translate(localPosition)
                    .translate(localPivot)
                    .rotateY(ry)
                    .rotateX(rx)
                    .rotateZ(rz)
                    .scale(localScale)
                    .translate(new Vector3f(localPivot).negate());

            cachedModelMatrix = localMatrix;
            dirty = false;

            if (parent != null)
                return parent.getModelMatrix().mul(localMatrix, new Matrix4f());
            else
                return new Matrix4f(localMatrix);
        }
        else
        {
            if (parent != null)
                return parent.getModelMatrix().mul(cachedModelMatrix, new Matrix4f());
            else
                return new Matrix4f(cachedModelMatrix);
        }
    }

    private void markDirty()
    {
        dirty = true;
        children.forEach(Transform::markDirty);
    }

    @Override
    public String toString()
    {
        return "\nPosition: [" + localPosition.x + ", " + localPosition.y + ", " + localPosition.z + "]\n" +
                "Rotation: [" + localRotation.x + ", " + localRotation.y + ", " + localRotation.z + "]\n" +
                "Scale: ["    + localScale.x    + ", " + localScale.y    + ", " + localScale.z    + "]\n" +
                "Pivot: ["    + localPivot.x    + ", " + localPivot.y    + ", " + localPivot.z    + "]\n";
    }

    public static @NotNull Transform create(@NotNull Vector3f position, @NotNull Vector3f rotation, @NotNull Vector3f scale)
    {
        Transform result = new Transform();

        result.localPosition = new Vector3f(position);
        result.localRotation = new Vector3f(rotation);
        result.localScale = new Vector3f(scale);
        result.localPivot = new Vector3f(0, 0, 0);

        return result;
    }
}