package com.thatsoulyguy.moonlander.audio;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObjectManager;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.AudioData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

/**
 * A class for playing audio clips.
 * <p>
 * Annotates: [{@code @CustomConstructor(...)}]
 */
@CustomConstructor("create")
public class AudioClip extends Component implements Cloneable
{
    private @EffectivelyNotNull String name;
    private @EffectivelyNotNull AssetPath path;
    private transient boolean destroyGameObjectOnEnd;
    private transient int buffer;
    private transient int source;

    private AudioClip() { }

    @Override
    public void initialize()
    {
        destroyGameObjectOnEnd = false;

        getGameObject().getTransform().addOnPositionChangedCallback(this, this::setPositionInternal);
        getGameObject().getTransform().addOnRotationChangedCallback(this, this::setDirectionInternal);

        generate();
    }

    @Override
    public void onLoad()
    {
        generate();
    }

    @Override
    public void updateMainThread()
    {
        if (!isPlaying() && destroyGameObjectOnEnd)
            GameObjectManager.unregister(getGameObject().getName(), true);
    }

    private void generate()
    {
        AudioData data = AudioData.create(path);

        buffer = AL10.alGenBuffers();

        AL10.alBufferData(buffer, data.getFormat(), data.getData(), data.getSampleRate());

        source = AL10.alGenSources();
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);

        setPosition(new Vector3f(0.0f, 0.0f, 0.0f));
        setVelocity(new Vector3f(0.0f, 0.0f, 0.0f));
        setDirection(new Vector3f(0.0f, 0.0f, 0.0f));
        setVolume(1.0f);
        setPitch(1.0f);
        setLooping(false);
    }

    /**
     * Plays the audio clip.
     */
    public void play(boolean destroyGameObjectOnEnd)
    {
        this.destroyGameObjectOnEnd = destroyGameObjectOnEnd;

        AL10.alSourcePlay(source);
     }

    /**
     * Stops the audio clip.
     */
    public void stop()
    {
        AL10.alSourceStop(source);
    }

    /**
     * A function to get the name of this audio clip
     * @return The name of this audio clip
     */
    public @NotNull String getName()
    {
        return name;
    }

    /**
     * Sets the 3D position of the audio source.
     * @param position The new position.
     */
    public void setPosition(@NotNull Vector3f position)
    {
        getGameObject().getTransform().setLocalPosition(position);
    }

    /**
     * Sets the velocity of the audio source.
     * @param velocity The new velocity.
     */
    public void setVelocity(@NotNull Vector3f velocity)
    {
        AL10.alSource3f(source, AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    /**
     * Sets the direction of the audio source.
     * @param direction The new direction.
     */
    public void setDirection(@NotNull Vector3f direction)
    {
        getGameObject().getTransform().setLocalRotation(direction);
    }

    /**
     * Enables or disables looping.
     * @param looping True to loop, false otherwise.
     */
    public void setLooping(boolean looping)
    {
        AL10.alSourcei(source, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * Sets the volume (gain) of the audio source.
     * @param volume A value typically between 0.0 and 1.0.
     */
    public void setVolume(float volume)
    {
        AL10.alSourcef(source, AL10.AL_GAIN, volume);
    }

    /**
     * Sets the pitch of the audio source.
     * @param pitch The new pitch value (1.0 is default).
     */
    public void setPitch(float pitch)
    {
        AL10.alSourcef(source, AL10.AL_PITCH, pitch);
    }

    /**
     * Gets if the audio clip is currently playing
     * @return A boolean indicating if the audio clip is playing
     */
    public boolean isPlaying()
    {
        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);

        return state == AL10.AL_PLAYING;
    }

    private void setPositionInternal(@NotNull Vector3f position)
    {
        AL10.alSource3f(source, AL10.AL_POSITION, position.x, position.y, position.z);
    }

    private void setDirectionInternal(@NotNull Vector3f direction)
    {
        AL10.alSource3f(source, AL10.AL_DIRECTION, direction.x, direction.y, direction.z);
    }

    /**
     * Releases OpenAL resources.
     */
    @Override
    public void uninitialize()
    {
        getGameObject().getTransform().removeOnPositionChangedCallback(this);
        getGameObject().getTransform().removeOnRotationChangedCallback(this);

        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
    }

    /**
     * Creates a new AudioClip.
     * @param name The name of the clip.
     * @param path The asset path to the audio file.
     * @return A new AudioClip instance.
     */
    public static @NotNull AudioClip create(@NotNull String name, @NotNull AssetPath path)
    {
        AudioClip result = new AudioClip();

        result.name = name;
        result.path = path;

        return result;
    }

    @Override
    public AudioClip clone()
    {
        try
        {
            return (AudioClip) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }
}