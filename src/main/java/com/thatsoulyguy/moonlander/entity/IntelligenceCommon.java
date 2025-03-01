package com.thatsoulyguy.moonlander.entity;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.entity.model.ModelPart;
import com.thatsoulyguy.moonlander.math.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

@Static
public class IntelligenceCommon
{
    private IntelligenceCommon() { }

    /**
     * Returns Euler angles {pitch, yaw, 0} so that an object at 'from' faces toward 'to'
     * in a typical 3D coordinate system where +Y is up and the object looks along -Z by default.
     *
     * @param from The starting position (where the object is)
     * @param to   The target position to look at
     * @return A Vector3f containing {pitch, yaw, 0}, in degrees
     */
    public static @NotNull Vector3f lookAtEuler(@NotNull Vector3f from, @NotNull Vector3f to)
    {
        Vector3f dir = new Vector3f(new Vector3f(to).sub(from)).negate();

        return getCommonDirection(dir);
    }

    public static @NotNull Vector3f getCommonDirection(@NotNull Vector3f direction)
    {
        float yaw = (float)Math.atan2(-direction.x, direction.z);
        float xzLen = (float)Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float pitch = -(float)Math.atan2(direction.y, xzLen);

        return new Vector3f((float)Math.toDegrees(pitch),
                (float)Math.toDegrees(yaw),
                0.0f);
    }

//    float deltaTime = Time.getDeltaTime();
//    float rotationSpeed = 5.0f;
//
//            assert getModelReference() != null;
//
//    Transform headTransform = Objects.requireNonNull(getModelReference().getPart("head")).getGameObject().getTransform();
//
//    float currentHeadYaw = headTransform.getLocalRotation().y;
//
//            headTransform.getLocalRotationReference().y = interpolateAngle(currentHeadYaw, targetHeadYaw, rotationSpeed * deltaTime);
//
//    ModelPart headPart = getModelReference().getPart("head");
//    ModelPart bodyPart = getModelReference().getPart("body");
//
//            assert headPart != null;
//            assert bodyPart != null;
//
//    float headYaw = headPart.getGameObject().getTransform().getLocalRotation().y;
//
//    Transform bodyTransform = bodyPart.getGameObject().getTransform();
//
//    float currentBodyYaw = bodyTransform.getLocalRotation().y;
//
//    float yawDiff = ((headYaw - currentBodyYaw + 180) % 360) - 180;
//
//            if (Math.abs(yawDiff) > 10.0f)
//    {
//        float desiredBodyYaw = headYaw - (yawDiff > 0 ? 10.0f : -10.0f);
//        float newBodyYaw = interpolateAngle(currentBodyYaw, desiredBodyYaw, rotationSpeed * deltaTime);
//
//        bodyTransform.setLocalRotation(new Vector3f(0.0f, newBodyYaw, 0.0f));
//    }

    public static float moveBodyWithHead(float currentHeadYaw, float currentBodyYaw, float maxAngle)
    {
        currentHeadYaw = normalizeAngle(currentHeadYaw);
        currentBodyYaw = normalizeAngle(currentBodyYaw);

        if (Math.abs(currentHeadYaw) < maxAngle)
            return currentBodyYaw;

        float desiredBodyYaw = (Math.abs(currentHeadYaw) - 10.0f) * Math.signum(currentHeadYaw);

        float rotationSpeed = 5.0f;
        float deltaTime = Time.getDeltaTime();

        return interpolateAngle(currentBodyYaw, desiredBodyYaw, rotationSpeed * deltaTime);
    }

    public static float interpolateAngle(float current, float target, float factor)
    {
        float diff = ((target - current + 540) % 360) - 180;
        return current + diff * factor;
    }

    private static float normalizeAngle(float angle)
    {
        angle %= 360;

        if (angle < -180)
            angle += 360;

        if (angle > 180)
            angle -= 360;

        return angle;
    }
}