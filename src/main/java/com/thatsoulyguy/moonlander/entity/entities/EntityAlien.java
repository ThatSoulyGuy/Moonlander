package com.thatsoulyguy.moonlander.entity.entities;

import com.thatsoulyguy.moonlander.core.Time;
import com.thatsoulyguy.moonlander.entity.Entity;
import com.thatsoulyguy.moonlander.entity.IntelligenceCommon;
import com.thatsoulyguy.moonlander.entity.LivingEntity;
import com.thatsoulyguy.moonlander.entity.model.EntityModel;
import com.thatsoulyguy.moonlander.entity.model.ModelPart;
import com.thatsoulyguy.moonlander.entity.model.models.ModelAlien;
import com.thatsoulyguy.moonlander.math.Rigidbody;
import com.thatsoulyguy.moonlander.math.Transform;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Random;

public class EntityAlien extends LivingEntity
{
    private final float attackCooldownTimerStart = 2.0f;
    private float attackCooldownTimer;
    private final float damageOverlayTimerStart = 0.2f;
    private float damageOverlayTimer;

    private float randomMoveTimer = 0f;
    private final Vector3f randomDirection = new Vector3f();
    private final Random random = new Random();
    private float targetHeadYaw = 0.0f;

    @Override
    public void initialize()
    {
        super.initialize();

        GameObject model = getGameObject().getChild("default." + getClass().getName() + "_" + getId() + ".model");

        model.getTransform().setLocalRotation(new Vector3f(180.0f, 0.0f, 0.0f));
        model.getTransform().setLocalPosition(new Vector3f(0.0f, 0.45f, 0.0f));

        attackCooldownTimer = attackCooldownTimerStart;
        damageOverlayTimer = damageOverlayTimerStart;

        randomMoveTimer = 0f;
    }

    @Override
    public void update()
    {
        EntityPlayer player = EntityPlayer.getLocalPlayer();
        float distanceFromPlayer = getGameObject().getTransform().getWorldPosition()
                .distance(player.getGameObject().getTransform().getWorldPosition());

        if (distanceFromPlayer < 15)
        {
            assert getModelReference() != null;

            ModelPart head = getModelReference().getPart("head");
            ModelPart body = getModelReference().getPart("body");

            assert head != null;
            assert body != null;

            Vector3f desiredHeadRot = IntelligenceCommon.lookAtEuler(
                    head.getGameObject().getTransform().getWorldPosition(),
                    player.getCamera().getGameObject().getTransform().getWorldPosition()
            );

            targetHeadYaw = desiredHeadRot.y;

            head.getGameObject().getTransform().getLocalRotationReference().x = desiredHeadRot.x;
            head.getGameObject().getTransform().getLocalRotationReference().z = desiredHeadRot.z;

            getGameObject().getTransform().translate(
                    head.getGameObject().getTransform().getForward().negate().normalize().mul(0.01f)
            );

            if (getGameObject().getComponentNotNull(Rigidbody.class).isGrounded() && distanceFromPlayer > 3)
                getGameObject().getComponentNotNull(Rigidbody.class).addForce(new Vector3f(0.0f, 6.0f, 0.0f));
        }
        else
        {
            if (randomMoveTimer <= 0f)
            {
                float angleDeg = random.nextFloat() * 360f;
                float angleRad = (float)Math.toRadians(angleDeg);

                randomDirection.set(
                        (float)Math.sin(angleRad),
                        0.0f,
                        (float)Math.cos(angleRad)
                ).normalize();

                targetHeadYaw = computeYawFromDirection(randomDirection);

                randomMoveTimer = 3.0f + random.nextFloat() * 2f;
            }
            else
            {
                float wanderSpeed = 0.01f;

                assert getModelReference() != null;

                ModelPart head = getModelReference().getPart("head");

                assert head != null;

                getGameObject().getTransform().translate(randomDirection.mul(wanderSpeed, new Vector3f()));
            }

            randomMoveTimer -= Time.getDeltaTime();
        }

        {
            assert getModelReference() != null;

            ModelPart headPart = getModelReference().getPart("head");
            ModelPart bodyPart = getModelReference().getPart("body");

            assert headPart != null;
            assert bodyPart != null;

            Transform headTransform = headPart.getGameObject().getTransform();

            float currentHeadYaw = headTransform.getLocalRotation().y;

            headTransform.getLocalRotationReference().y = IntelligenceCommon.interpolateAngle(currentHeadYaw, targetHeadYaw, Time.getDeltaTime() * 5);

            float headYaw = headPart.getGameObject().getTransform().getLocalRotation().y;
            float currentBodyYaw = bodyPart.getGameObject().getTransform().getLocalRotation().y;
            float newBodyYaw = IntelligenceCommon.moveBodyWithHead(headYaw, currentBodyYaw, 45.0f);

            bodyPart.getGameObject().getTransform().setLocalRotation(new Vector3f(0.0f, newBodyYaw, 0.0f));
        }

        if (attackCooldownTimer < 0 && distanceFromPlayer < 3f)
        {
            player.damage(this, 5);
            attackCooldownTimer = attackCooldownTimerStart;
        }

        if (getCurrentHealth() <= 0)
            Objects.requireNonNull(World.getLocalWorld()).killEntity(this, getId(), getClass());

        assert getModelReference() != null;

        if (damageOverlayTimer < 0)
            getModelReference().setTexture(Objects.requireNonNull(TextureManager.get("entity.alien")));

        attackCooldownTimer -= Time.getDeltaTime();
        damageOverlayTimer -= Time.getDeltaTime();
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "entity_player";
    }

    @Override
    public float getWalkingSpeed()
    {
        return 20.0f;
    }

    @Override
    public float getRunningSpeed()
    {
        return 25.0f;
    }

    @Override
    public int getMaximumHealth()
    {
        return 30;
    }

    @Override
    public @NotNull Vector3f getBoundingBoxSize()
    {
        return new Vector3f(0.65f, 1.89f, 0.65f);
    }

    @Override
    public @Nullable Class<? extends EntityModel> getModelType()
    {
        return ModelAlien.class;
    }

    @Override
    public void onDamaged(@NotNull World world, @NotNull Entity damage, int damageDealt)
    {
        assert getModelReference() != null;

        getModelReference().setTexture(Objects.requireNonNull(TextureManager.get("entity.damage")));

        damageOverlayTimer = damageOverlayTimerStart;
    }

    private static float computeYawFromDirection(@NotNull Vector3f direction)
    {
        return (float) Math.toDegrees(Math.atan2(direction.x, -direction.z));
    }
}