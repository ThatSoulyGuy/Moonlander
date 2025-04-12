package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.*;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import com.thatsoulyguy.moonlander.util.FileHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.Random;

@CustomConstructor("create")
public class MainSystem extends Component
{
    private static MainSystem instance = null;

    private MainSystem() { }

    @Override
    public void initialize()
    {
        UIPanel panel = getGameObject().getComponentNotNull(UIPanel.class);

        if (!new File(FileHelper.getPersistentDataPath("Moonlander") + "/overworld/").exists())
            panel.getNotNull("ui.load_last_game", ButtonUIElement.class).setTexture(Objects.requireNonNull(TextureManager.get("ui.button_disabled")));

        instance = this;
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        if (element.getGameObject().getName().equals("ui.new_game") && !element.getTexture().getName().equals("ui.button_disabled"))
        {
            GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

            soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

            soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

            AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

            clip.setLooping(false);
            clip.play(true);

            LevelManager.unloadCurrentLevel();

            AudioListener.reset();

            Levels.createOverworld();
        }
        else if (element.getGameObject().getName().equals("ui.load_last_game") && !element.getTexture().getName().equals("ui.button_disabled"))
        {
            GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

            soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

            soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

            AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

            clip.setLooping(false);
            clip.play(true);

            LevelManager.unloadCurrentLevel();

            AudioListener.reset();

            UIManager.initialize();

            LevelManager.loadLevel(FileHelper.getPersistentDataPath("Moonlander") + "/overworld", true);
        }
        else if (element.getGameObject().getName().equals("ui.quit") && !element.getTexture().getName().equals("ui.button_disabled"))
            Window.exit();
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

    public static @NotNull MainSystem getInstance()
    {
        return instance;
    }

    public static @NotNull MainSystem create()
    {
        return new MainSystem();
    }
}