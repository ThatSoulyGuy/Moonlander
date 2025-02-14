package com.thatsoulyguy.moonlander.ui.menus;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.GameObject;
import com.thatsoulyguy.moonlander.system.Layer;
import com.thatsoulyguy.moonlander.ui.Menu;
import com.thatsoulyguy.moonlander.ui.MenuManager;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.ManagerLinkedClass;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;
import org.joml.Vector2f;

import java.util.List;
import java.util.Objects;

public class PauseMenu extends Menu
{
    private @EffectivelyNotNull EntityPlayer host;
    private @EffectivelyNotNull UIPanel menu;

    @Override
    public void initialize()
    {
        menu = UIPanel.create("pause_menu");

        UIElement background = menu.addElement(UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f, 0.0f), new Vector2f(100.0f, 100.0f)));

        background.setTransparent(true);
        background.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));
        background.setStretch(List.of(UIElement.Stretch.LEFT, UIElement.Stretch.RIGHT, UIElement.Stretch.TOP, UIElement.Stretch.BOTTOM));

        {
            ButtonUIElement button = (ButtonUIElement) menu.addElement(UIElement.create(ButtonUIElement.class, "back_to_game", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f)));

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

                host.setPaused(false);
                host.setPauseMenuActive(false);
            });

            button.addOnHoveringBeginEvent(() -> button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected"))));
            button.addOnHoveringEndEvent(() -> button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default"))));

            button.setOffset(new Vector2f(0.0f, -40.0f));


            TextUIElement text = (TextUIElement) menu.addElement(UIElement.create(TextUIElement.class, "back_to_game_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f)));

            text.setText("Back to game");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize(20);
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, -40.0f));
        }

        {
            ButtonUIElement button = (ButtonUIElement) menu.addElement(UIElement.create(ButtonUIElement.class, "save_and_quit", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f)));

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

                //Save and quit
            });

            button.addOnHoveringBeginEvent(() -> button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected"))));
            button.addOnHoveringEndEvent(() -> button.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default"))));

            button.setOffset(new Vector2f(0.0f, 10.0f));


            TextUIElement text = (TextUIElement) menu.addElement(UIElement.create(TextUIElement.class, "save_and_quit_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f)));

            text.setText("Save and quit to title");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize(20);
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, 10.0f));
        }
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_pause";
    }

    public void setHost(@NotNull EntityPlayer host)
    {
        this.host = host;
    }

    public @NotNull EntityPlayer getHost()
    {
        return host;
    }

    public void setActive(boolean active)
    {
        menu.setActive(active);
    }

    public boolean getActive()
    {
        return menu.isActive();
    }
}