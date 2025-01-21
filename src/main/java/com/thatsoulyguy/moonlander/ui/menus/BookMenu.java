package com.thatsoulyguy.moonlander.ui.menus;

import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.render.TextureManager;
import com.thatsoulyguy.moonlander.ui.Menu;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.ui.UIPanel;
import com.thatsoulyguy.moonlander.ui.uielements.ButtonUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.ImageUIElement;
import com.thatsoulyguy.moonlander.ui.uielements.TextUIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookMenu extends Menu
{
    private @EffectivelyNotNull TextUIElement pagesText;

    private @NotNull List<String> pages = new ArrayList<>();
    private byte pageIndex = 0;

    private @EffectivelyNotNull UIPanel menu;

    @Override
    public void initialize()
    {
        menu = UIPanel.create("book_menu");

        UIElement background = menu.addElement(UIElement.create(ImageUIElement.class, "background", new Vector2f(0.0f, 0.0f), new Vector2f(100.0f, 100.0f)));

        background.setTransparent(true);
        background.setTexture(Objects.requireNonNull(TextureManager.get("ui.background")));
        background.setStretch(List.of(UIElement.Stretch.LEFT, UIElement.Stretch.RIGHT, UIElement.Stretch.TOP, UIElement.Stretch.BOTTOM));

        UIElement book = menu.addElement(UIElement.create(ImageUIElement.class, "book", new Vector2f(0.0f, 0.0f), new Vector2f(590.0f, 358.0f).mul(Settings.UI_SCALE.getValue())));

        book.setTransparent(true);
        book.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.book")));
        book.setAlignment(UIElement.Alignment.CENTER);

        pagesText = (TextUIElement) menu.addElement(UIElement.create(TextUIElement.class, "text", new Vector2f(0.0f, 0.0f), new Vector2f(534.0f, 315.0f).mul(Settings.UI_SCALE.getValue())));

        pagesText.setColor(new Vector3f(0.0f, 0.0f, 0.0f));

        if (pages.isEmpty())
            pagesText.setText("");
        else
            pagesText.setText(pages.getFirst());

        pagesText.setFontPath(AssetPath.create("moonlander", "font/Invasion2-Default.ttf"));
        pagesText.setFontSize(18);
        pagesText.setAlignment(TextUIElement.TextAlignment.VERTICAL_TOP, TextUIElement.TextAlignment.HORIZONTAL_LEFT);

        pagesText.build();

        {
            ButtonUIElement nextPageButton = (ButtonUIElement) menu.addElement(UIElement.create(ButtonUIElement.class, "next_page_button", new Vector2f(0.0f, 0.0f), new Vector2f(36, 20).mul(Settings.UI_SCALE.getValue())));

            nextPageButton.setTransparent(true);
            nextPageButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page")));
            nextPageButton.addOnLeftClickedEvent(() ->
            {
                if (pageIndex == pages.size() - 1)
                    nextPageButton.setActive(false);
                else
                {
                    pageIndex++;
                    pagesText.setText(pages.get(pageIndex));
                    pagesText.build();

                    if (pageIndex == pages.size() - 1)
                        nextPageButton.setActive(false);
                }
            });

            nextPageButton.addOnHoveringBeginEvent(() ->
            {
                if (nextPageButton.getTexture() != Objects.requireNonNull(TextureManager.get("ui.transparency")))
                    nextPageButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page_deactivated")));
            });

            nextPageButton.addOnHoveringEndEvent(() ->
            {
                if (nextPageButton.getTexture() != Objects.requireNonNull(TextureManager.get("ui.transparency")))
                    nextPageButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.next_page")));
            });

            nextPageButton.setOffset(new Vector2f(355.0f, 200.0f));


            ButtonUIElement previousPageButton = (ButtonUIElement) menu.addElement(UIElement.create(ButtonUIElement.class, "previous_page_button", new Vector2f(0.0f, 0.0f), new Vector2f(36, 20).mul(Settings.UI_SCALE.getValue())));

            previousPageButton.setTransparent(true);
            previousPageButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page")));
            previousPageButton.addOnLeftClickedEvent(() ->
            {
                if (pageIndex == 0)
                    previousPageButton.setActive(false);
                else
                {
                    pageIndex--;
                    pagesText.setText(pages.get(pageIndex));
                    pagesText.build();

                    if (pageIndex == 0)
                        previousPageButton.setActive(false);
                }
            });

            previousPageButton.addOnHoveringBeginEvent(() ->
            {
                if (previousPageButton.getTexture() != Objects.requireNonNull(TextureManager.get("ui.transparency")))
                    previousPageButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page_deactivated")));
            });

            previousPageButton.addOnHoveringEndEvent(() ->
            {
                if (previousPageButton.getTexture() != Objects.requireNonNull(TextureManager.get("ui.transparency")))
                    previousPageButton.setTexture(Objects.requireNonNull(TextureManager.get("ui.menu.previous_page")));
            });

            previousPageButton.setOffset(new Vector2f(-355.0f, 200.0f));

            previousPageButton.setActive(false);
        }
    }

    public void update()
    {
        if (pageIndex > 0 && menu.isActive())
            Objects.requireNonNull(menu.getElement("previous_page_button")).setActive(true);

        if (pageIndex < pages.size() - 1 && menu.isActive())
            Objects.requireNonNull(menu.getElement("next_page_button")).setActive(true);
    }

    public void setPages(@NotNull List<String> pages)
    {
        this.pages = pages;
    }

    public @NotNull List<String> getPages()
    {
        return pages;
    }

    public @NotNull String getText()
    {
        return pagesText.getText();
    }

    public void rebuildPages()
    {
        if (pages.isEmpty())
            pagesText.setText("");
        else
            pagesText.setText(pages.getFirst());

        pagesText.build();
    }

    @Override
    public @NotNull String getRegistryName()
    {
        return "menu_book";
    }

    public void setActive(boolean active)
    {
        menu.setActive(active);
    }

    public boolean isActive()
    {
        return menu.isActive();
    }
}