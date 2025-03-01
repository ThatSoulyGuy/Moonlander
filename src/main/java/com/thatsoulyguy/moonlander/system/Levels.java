package com.thatsoulyguy.moonlander.system;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.entity.entities.EntityAlien;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.entity.entities.EntityRocket;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.util.FileHelper;
import com.thatsoulyguy.moonlander.world.RegionalSpawner;
import com.thatsoulyguy.moonlander.world.TerrainGenerator;
import com.thatsoulyguy.moonlander.world.World;
import com.thatsoulyguy.moonlander.world.regionalspawners.AlienRegionalSpawner;
import com.thatsoulyguy.moonlander.world.terraingenerators.CaveTerrainGenerator;
import com.thatsoulyguy.moonlander.world.terraingenerators.GroundTerrainGenerator;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Static
public class Levels
{
    private static final Object lock = new Object();

    private Levels() { }

    public static void createMainMenu()
    {
        UIManager.initialize();

        LevelManager.createLevel("main_menu", true);

        GameObject audioListener = GameObject.create("audio_listener", Layer.DEFAULT);

        audioListener.addComponent(AudioListener.create());

        UIPanel panel = UIPanel.create("main_menu");

        {
            UIElement background = panel.addElement(UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f), new Vector2f(100.0f)));

            background.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.main_menu_background")));
            background.setAlignment(UIElement.Alignment.CENTER);
            background.setStretch(List.of(UIElement.Stretch.LEFT, UIElement.Stretch.RIGHT, UIElement.Stretch.TOP, UIElement.Stretch.BOTTOM));


            UIElement title = panel.addElement(UIElement.create(ImageUIElement.class, "title", new Vector2f(0.0f), new Vector2f(2260, 505).mul(0.15f).mul(Settings.UI_SCALE.getValue())));

            title.setTexture(Objects.requireNonNull(TextureManager.get("ui.title")));
            title.setTransparent(true);
            title.setOffset(new Vector2f(0.0f, -100.0f).mul(Settings.UI_SCALE.getValue()));
        }

        {
            ButtonUIElement newSave = (ButtonUIElement) panel.addElement(UIElement.create(ButtonUIElement.class, "new_save", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f).mul(0.5f).mul(Settings.UI_SCALE.getValue())));

            newSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default")));

            newSave.addOnLeftClickedEvent(() ->
            {
                newSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_disabled")));

                GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

                soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

                soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

                AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                clip.setLooping(false);
                clip.play(true);

                LevelManager.unloadCurrentLevel();

                AudioListener.reset();

                synchronized (lock)
                {
                    createOverworld();
                }
            });

            newSave.addOnHoveringBeginEvent(() -> newSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected"))));
            newSave.addOnHoveringEndEvent(() -> newSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default"))));

            newSave.setOffset(new Vector2f(0.0f, -40.0f));

            TextUIElement text = (TextUIElement) panel.addElement(UIElement.create(TextUIElement.class, "new_save_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f).mul(0.5f).mul(Settings.UI_SCALE.getValue())));

            text.setText("New Game");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize((int) (10 * Settings.UI_SCALE.getValue()));
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, -40.0f));
        }

        {
            ButtonUIElement loadSave = (ButtonUIElement) panel.addElement(UIElement.create(ButtonUIElement.class, "load_save", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f).mul(0.5f).mul(Settings.UI_SCALE.getValue())));

            if (new File(FileHelper.getPersistentDataPath("Moonlander") + "/overworld").exists())
            {
                loadSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default")));

                loadSave.addOnLeftClickedEvent(() ->
                {
                    loadSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_disabled")));

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
                });

                loadSave.addOnHoveringBeginEvent(() -> loadSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected"))));
                loadSave.addOnHoveringEndEvent(() -> loadSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default"))));
            }
            else
                loadSave.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_disabled")));

            loadSave.setOffset(new Vector2f(0.0f, 10.0f));

            TextUIElement text = (TextUIElement) panel.addElement(UIElement.create(TextUIElement.class, "load_save_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f).mul(0.5f).mul(Settings.UI_SCALE.getValue())));

            text.setText("Load Last Game");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize((int) (10 * Settings.UI_SCALE.getValue()));
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, 10.0f));
        }

        {
            ButtonUIElement quit = (ButtonUIElement) panel.addElement(UIElement.create(ButtonUIElement.class, "quit", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f).mul(0.5f).mul(Settings.UI_SCALE.getValue())));

            quit.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default")));

            quit.addOnLeftClickedEvent(() ->
            {
                quit.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_disabled")));

                GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

                soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

                soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

                AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

                clip.setLooping(false);
                clip.play(true);

                Window.exit();
            });

            quit.addOnHoveringBeginEvent(() -> quit.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_selected"))));
            quit.addOnHoveringEndEvent(() -> quit.setTexture(Objects.requireNonNull(TextureManager.get("ui.button_default"))));

            quit.setOffset(new Vector2f(0.0f, 60.0f));

            TextUIElement text = (TextUIElement) panel.addElement(UIElement.create(TextUIElement.class, "quit_text", new Vector2f(0.0f, 0.0f), new Vector2f(400.0f, 40.0f).mul(0.5f).mul(Settings.UI_SCALE.getValue())));

            text.setText("Quit");
            text.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
            text.setFontSize((int) (10 * Settings.UI_SCALE.getValue()));
            text.setAlignment(TextUIElement.TextAlignment.VERTICAL_CENTER, TextUIElement.TextAlignment.HORIZONTAL_CENTER);

            text.build();

            text.setOffset(new Vector2f(0.0f, 60.0f));
        }
    }

    public static void createOverworld()
    {
        UIManager.initialize();

        LevelManager.createLevel("overworld", true);

        GameObject overworld = GameObject.create("default.world", Layer.DEFAULT);

        overworld.addComponent(World.create("overworld"));

        World world = overworld.getComponentNotNull(World.class);

        world.addTerrainGenerator(TerrainGenerator.create(GroundTerrainGenerator.class));
        world.addTerrainGenerator(TerrainGenerator.create(CaveTerrainGenerator.class));

        world.addRegionalSpawner(RegionalSpawner.create(AlienRegionalSpawner.class));

        world.spawnEntity(new Vector3f(0.0f, 180.0f, 0.0f), EntityPlayer.class);
    }
}