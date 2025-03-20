package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.*;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.util.FileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

@CustomConstructor("create")
public class PauseSystem extends Component
{
    private static PauseSystem instance = null;

    private PauseSystem() { }

    @Override
    public void initialize()
    {
        instance = this;
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        if (element.getGameObject().getName().equals("ui.back_to_game"))
        {
            EntityPlayer.getLocalPlayer().setPaused(false);
            EntityPlayer.getLocalPlayer().setBackgroundShadingActive(false);
            getInstance().getGameObject().setActive(false);

            GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

            soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

            soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

            AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

            clip.setLooping(false);
            clip.play(true);
        }
        else
        {
            EntityPlayer.getLocalPlayer().setPaused(true);
            EntityPlayer.getLocalPlayer().setBackgroundShadingActive(false);
            getInstance().getGameObject().setActive(false);

            GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

            soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

            soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

            AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

            clip.setLooping(false);
            clip.play(true);

            LevelManager.saveLevel("overworld", FileHelper.getPersistentDataPath("Moonlander"));

            LevelManager.unloadCurrentLevel();

            Levels.createMainMenu();
        }
    }

    public static void onHoverBegin(@NotNull ButtonUIElement element)
    {
        if (!element.getTexture().getName().equals("ui.button_disabled"))
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected")));
    }

    public static void onHoverEnd(@NotNull ButtonUIElement element)
    {
        if (!element.getTexture().getName().equals("ui.button_disabled"))
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default")));
    }

    public static @NotNull PauseSystem getInstance()
    {
        return instance;
    }

    public static @NotNull PauseSystem create()
    {
        return new PauseSystem();
    }
}