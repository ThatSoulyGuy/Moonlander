package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.*;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.util.FileHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Random;

@CustomConstructor("create")
public class DeathSystem extends Component
{
    private static DeathSystem instance = null;

    private DeathSystem() { }

    @Override
    public void initialize()
    {
        instance = this;
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        if (element.getGameObject().getName().equals("ui.respawn"))
        {
            EntityPlayer.getLocalPlayer().getGameObject().getTransform().setLocalPosition(new Vector3f(0.0f, 180.0f, 0.0f));

            EntityPlayer.getLocalPlayer().setOxygen(20);
            EntityPlayer.getLocalPlayer().setCurrentHealth(20);

            instance.getGameObject().setActive(false);
            CreativeCraftingSystem.getInstance().getGameObject().setActive(false);
            HotbarSystem.getInstance().getGameObject().setActive(true);
            InventorySystem.getInstance().getGameObject().setActive(false);
            PauseSystem.getInstance().getGameObject().setActive(false);
            SurvivalCraftingSystem.getInstance().getGameObject().setActive(false);
            WinConditionSystem.getInstance().getGameObject().setActive(false);

            EntityPlayer.getLocalPlayer().setBackgroundShadingActive(false);
            EntityPlayer.getLocalPlayer().pause(false);

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

    public static @NotNull DeathSystem getInstance()
    {
        return instance;
    }

    public static @NotNull DeathSystem create()
    {
        return new DeathSystem();
    }
}