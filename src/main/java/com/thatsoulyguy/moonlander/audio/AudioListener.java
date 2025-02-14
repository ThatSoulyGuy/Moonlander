package com.thatsoulyguy.moonlander.audio;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.math.Rigidbody;
import com.thatsoulyguy.moonlander.system.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

import java.util.concurrent.atomic.AtomicBoolean;

@CustomConstructor("create")
public class AudioListener extends Component
{
    private static @EffectivelyNotNull AudioListener localListener = null;

    private static final AtomicBoolean alreadyExists = new AtomicBoolean(false);

    private AudioListener()
    {
        if (alreadyExists.get())
            throw new IllegalStateException("Attempt to add more than 1 audio listener into the scene!");

        alreadyExists.set(true);
        localListener = this;
    }

    @Override
    public void initialize()
    {
        getGameObject().getTransform().addOnPositionChangedCallback(this, this::onPositionChanged);
        getGameObject().getTransform().addOnRotationChangedCallback(this, this::onRotationChanged);
    }

    @Override
    public void update()
    {
        if (getGameObject().hasComponent(Rigidbody.class))
        {
            Rigidbody rigidbody = getGameObject().getComponent(Rigidbody.class);

            assert rigidbody != null;

            if (rigidbody.getVelocity().x > 0.01f && rigidbody.getVelocity().y > 0.01f && rigidbody.getVelocity().z > 0.01f)
                AL10.alListener3f(AL10.AL_VELOCITY, rigidbody.getVelocity().x, rigidbody.getVelocity().y, rigidbody.getVelocity().z);
        }
    }

    public static @NotNull AudioListener getLocalListener()
    {
        return localListener;
    }

    private void onPositionChanged(@NotNull Vector3f newPosition)
    {
        AL10.alListener3f(AL10.AL_POSITION, newPosition.x, newPosition.y, newPosition.z);
    }

    private void onRotationChanged(@NotNull Vector3f newRotation)
    {
        Quaternionf quaternion = new Quaternionf().rotateXYZ(newRotation.x, newRotation.y, newRotation.z);

        Vector3f forward = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f,  0.0f);

        quaternion.transform(forward);
        quaternion.transform(up);

        AL10.alListenerfv(AL10.AL_ORIENTATION, new float[] { forward.x, forward.y, forward.z, up.x, up.y, up.z });
    }

    @Override
    public void uninitialize()
    {
        getGameObject().getTransform().removeOnPositionChangedCallback(this);
        getGameObject().getTransform().removeOnRotationChangedCallback(this);
    }

    public static @NotNull AudioListener create()
    {
        return new AudioListener();
    }
}