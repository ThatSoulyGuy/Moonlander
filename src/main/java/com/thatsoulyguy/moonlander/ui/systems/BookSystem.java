package com.thatsoulyguy.moonlander.ui.systems;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.audio.AudioClip;
import com.thatsoulyguy.moonlander.audio.AudioListener;
import com.thatsoulyguy.moonlander.audio.AudioManager;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.system.*;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.elements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.elements.TextUIElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@CustomConstructor("create")
public class BookSystem extends Component
{
    private int pageIndex = 0;
    private List<String> pages = new ArrayList<>();

    private static BookSystem instance = null;

    private BookSystem() { }

    @Override
    public void initialize()
    {
        instance = this;
    }

    @Override
    public void update()
    {
        GameObject nextPageObj = getGameObject().getChild("ui.next_page");

        ButtonUIElement nextButton = nextPageObj.getComponentNotNull(ButtonUIElement.class);
        if (pageIndex < pages.size() - 1)
            nextButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page")));
        else
            nextButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page_deactivated")));

        GameObject previousPageObj = getGameObject().getChild("ui.previous_page");

        ButtonUIElement prevButton = previousPageObj.getComponentNotNull(ButtonUIElement.class);

        if (pageIndex > 0)
            prevButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page")));
        else
            prevButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page_deactivated")));
    }

    public static void onLeftMousePressed(@NotNull ButtonUIElement element)
    {
        BookSystem instance = BookSystem.getInstance();

        if (element.getGameObject().getName().equals("ui.next_page") && !element.getTexture().getName().equals("ui.menu.next_page_deactivated"))
        {
            if (instance.pageIndex < instance.pages.size() - 1)
            {
                instance.pageIndex += 1;

                instance.getGameObject().getComponentNotNull(UIPanel.class).getNotNull("ui.text", TextUIElement.class).setText(instance.pages.get(instance.pageIndex));
                instance.getGameObject().getComponentNotNull(UIPanel.class).getNotNull("ui.text", TextUIElement.class).build();
            }
            else
                element.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page_deactivated")));

            GameObject soundObject = GameObject.create("ui.click" + new Random().nextInt(4096), Layer.DEFAULT);

            soundObject.addComponent(Objects.requireNonNull(AudioManager.get("ui.click")));

            soundObject.getTransform().setLocalPosition(AudioListener.getLocalListener().getGameObject().getTransform().getLocalPosition());

            AudioClip clip = soundObject.getComponentNotNull(AudioClip.class);

            clip.setLooping(false);
            clip.play(true);
        }
        else if (element.getGameObject().getName().equals("ui.previous_page") && !element.getTexture().getName().equals("ui.menu.previous_page_deactivated"))
        {
            if (instance.pageIndex > 0)
            {
                instance.pageIndex -= 1;

                instance.getGameObject().getComponentNotNull(UIPanel.class).getNotNull("ui.text", TextUIElement.class).setText(instance.pages.get(instance.pageIndex));
                instance.getGameObject().getComponentNotNull(UIPanel.class).getNotNull("ui.text", TextUIElement.class).build();
            }
            else
                element.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page_deactivated")));

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
        if (!element.getTexture().getName().equals("ui.menu.next_page_deactivated") && element.getGameObject().getName().equals("ui.next_page"))
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page_selected")));

        if (!element.getTexture().getName().equals("ui.menu.previous_page_deactivated") && element.getGameObject().getName().equals("ui.previous_page"))
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page_selected")));
    }

    public static void onHoverEnd(@NotNull ButtonUIElement element)
    {
        if (!element.getTexture().getName().equals("ui.menu.next_page_deactivated") && element.getGameObject().getName().equals("ui.next_page"))
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page")));

        if (!element.getTexture().getName().equals("ui.menu.previous_page_deactivated") && element.getGameObject().getName().equals("ui.previous_page"))
            element.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page")));
    }

    public @NotNull List<String> getPages()
    {
        return pages;
    }

    public void setPages(@NotNull List<String> pages)
    {
        this.pages = pages;

        getGameObject().getComponentNotNull(UIPanel.class).getNotNull("ui.text", TextUIElement.class).setText(instance.pages.get(instance.pageIndex));
        getGameObject().getComponentNotNull(UIPanel.class).getNotNull("ui.text", TextUIElement.class).build();
    }

    public static @NotNull BookSystem getInstance()
    {
        return instance;
    }

    public static @NotNull BookSystem create()
    {
        return new BookSystem();
    }
}