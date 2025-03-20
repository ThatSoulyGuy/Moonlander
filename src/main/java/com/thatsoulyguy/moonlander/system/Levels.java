package com.thatsoulyguy.moonlander.system;

import com.thatsoulyguy.moonlander.annotation.Static;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.core.Window;
import com.thatsoulyguy.moonlander.entity.entities.EntityPlayer;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIManager;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.systems.MainSystem;
import com.thatsoulyguy.moonlander.util.AssetPath;
import com.thatsoulyguy.moonlander.world.RegionalSpawner;
import com.thatsoulyguy.moonlander.world.TerrainGenerator;
import com.thatsoulyguy.moonlander.world.World;
import com.thatsoulyguy.moonlander.world.regionalspawners.AlienRegionalSpawner;
import com.thatsoulyguy.moonlander.world.terraingenerators.CaveTerrainGenerator;
import com.thatsoulyguy.moonlander.world.terraingenerators.GroundTerrainGenerator;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Objects;

@Static
public class Levels
{
    private static @Nullable ImageUIElement mainMenuBackground = null;

    private Levels() { }

    public static void createMainMenu()
    {
        UIManager.initialize();

        LevelManager.createLevel("main_menu", true);

        GameObject audioListener = GameObject.create("audio_listener", Layer.DEFAULT);

        audioListener.addComponent(AudioListener.create());

        mainMenuBackground = UIElement.createGameObject("ui.main_menu_background", ImageUIElement.class, new Vector2f(0.0f, 0.0f), new Vector2f(0.0f, 0.0f), UIManager.getCanvas()).getComponentNotNull(ImageUIElement.class);

        mainMenuBackground.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.main_menu_background")));

        GameObject panelObject = UIPanel.fromJson("ui.main", AssetPath.create("moonlander", "ui/MainPanel.json"));

        UIPanel panel = panelObject.getComponentNotNull(UIPanel.class);

        panel.setPanelAlignment(UIPanel.PanelAlignment.MIDDLE_CENTER);

        panelObject.addComponent(MainSystem.create());
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

    public static void update()
    {
        assert LevelManager.getCurrentLevel() != null;

        if (LevelManager.getCurrentLevel().getName().equals("main_menu"))
        {
            mainMenuBackground.getGameObject().getTransform().setLocalPosition(new Vector3f((float) Window.getDimensions().x / 2, (float) Window.getDimensions().y / 2, 0.0f));
            mainMenuBackground.getGameObject().getTransform().setLocalScale(new Vector3f(Window.getDimensions().x, Window.getDimensions().y, 0.0f));
        }
    }
}