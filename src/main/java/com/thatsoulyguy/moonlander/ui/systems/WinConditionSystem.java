package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.Component;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

@CustomConstructor("create")
public class WinConditionSystem extends Component
{
    private static WinConditionSystem instance = null;

    private WinConditionSystem() { }

    @Override
    public void initialize()
    {
        instance = this;
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        if (element.getGameObject().getName().equals("ui.quit"))
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

    public static @NotNull WinConditionSystem getInstance()
    {
        return instance;
    }

    public static @NotNull WinConditionSystem create()
    {
        return new WinConditionSystem();
    }
}