package com.thatsoulyguy.moonlander.ui.menus;

import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.system.LevelManager;
import com.thatsoulyguy.moonlander.system.Levels;
import com.thatsoulyguy.moonlander.ui.Menu;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.FileHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;
import org.joml.Vector2f;

import java.util.List;
import java.util.Objects;

public class WinConditionMenu extends Menu
{
    private UIPanel panel;

    @Override
    public void initialize()
    {
        panel = UIPanel.create("menu_win_condition");

        UIElement background = panel.addElement(UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f, 0.0f), new Vector2f(100.0f, 100.0f)));

        background.setTransparent(true);
        background.setTexture(Objects.requireNonNull(TextureManager.get("ui.win_green")));
        background.setStretch(List.of(UIElement.Stretch.LEFT, UIElement.Stretch.RIGHT, UIElement.Stretch.TOP, UIElement.Stretch.BOTTOM));

        {
            TextUIElement text = (TextUIElement) panel.addElement(UIElement.create(TextUIElement.class, "information_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 80.0f)));

            text.setText("You Win!");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize(50);
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, -120.0f));
        }

        {
            ButtonUIElement button = (ButtonUIElement) panel.addElement(UIElement.create(ButtonUIElement.class, "save_and_quit", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f)));

            button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default")));

            button.addOnLeftClickedEvent(() ->
            {
                button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_disabled")));

                GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

                soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

                soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

                AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                clip.setLooping(false);
                clip.play(true);

                LevelManager.unloadCurrentLevel();

                Levels.createMainMenu();
            });

            button.addOnHoveringBeginEvent(() -> button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected"))));
            button.addOnHoveringEndEvent(() -> button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default"))));

            button.setOffset(new Vector2f(0.0f, 10.0f));


            TextUIElement text = (TextUIElement) panel.addElement(UIElement.create(TextUIElement.class, "save_and_quit_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f)));

            text.setText("Quit to title");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize(20);
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, 10.0f));
        }
    }

    public void setActive(boolean active)
    {
        panel.setActive(active);
    }

    public boolean isActive()
    {
        return panel.isActive();
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_win_condition";
    }
}
