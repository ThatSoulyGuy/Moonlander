package com.thatsoulyguy.moonlander.ui.elements;

import com.thatsoulyguy.moonlander.core.Settings;
import com.thatsoulyguy.moonlander.render.Mesh;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.ui.UIElement;
import com.thatsoulyguy.moonlander.util.AssetPath;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Random;

public class TextUIElement extends UIElement
{
    private String text;
    private int fontSize;
    private AssetPath fontPath;
    private TextAlignment alignment;

    @Override
    public void onGenerate(@NotNull Mesh mesh)
    {
        mesh.setTransparent(true);
    }

    public void build()
    {
        Vector2i dimensions = new Vector2i((int) getGameObject().getTransform().getWorldScale().x, (int) getGameObject().getTransform().getWorldScale().y);

        getGameObject().setComponent(Texture.create("font" + new Random().nextInt(4096), Texture.Filter.NEAREST, Texture.Wrapping.REPEAT, dimensions.x, dimensions.y, createTextTexture(dimensions, text, fontPath, alignment, (int) (fontSize * Settings.UI_SCALE.getValue()))));
    }

    public @NotNull String getText()
    {
        return text;
    }

    public void setText(@NotNull String text)
    {
        this.text = text;
    }

    public int getFontSize()
    {
        return fontSize;
    }

    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

    public @NotNull AssetPath getFontPath()
    {
        return fontPath;
    }

    public void setFontPath(@NotNull AssetPath fontPath)
    {
        this.fontPath = fontPath;
    }

    public @NotNull TextAlignment getAlignment()
    {
        return alignment;
    }

    public void setAlignment(@NotNull TextAlignment alignment)
    {
        this.alignment = alignment;
    }

    private static @NotNull ByteBuffer createTextTexture(@NotNull Vector2i dimensions, @NotNull String text, @NotNull AssetPath fontPath, @NotNull TextAlignment alignment, int fontSize)
    {
        int width = dimensions.x;
        int height = dimensions.y;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font;

        try (InputStream fontStream = TextUIElement.class.getResourceAsStream(fontPath.getFullPath()))
        {
            if (fontStream == null)
                throw new IllegalArgumentException("Font file not found in classpath: " + fontPath.getFullPath());

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            font = baseFont.deriveFont((float) fontSize);
        }
        catch (FontFormatException | IOException e)
        {
            throw new RuntimeException("Could not load font from classpath: " + fontPath.getFullPath(), e);
        }

        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        FontRenderContext frc = g2d.getFontRenderContext();

        String[] lines = text.split("\n", -1);

        java.util.List<TextLayout> layouts = new ArrayList<>();
        java.util.List<Float> lineHeights = new ArrayList<>();

        for (String line : lines)
        {
            String processedLine = line.isEmpty() ? " " : line;

            AttributedString attributedLine = new AttributedString(processedLine);
            attributedLine.addAttribute(TextAttribute.FONT, font);
            AttributedCharacterIterator lineIterator = attributedLine.getIterator();
            LineBreakMeasurer measurer = new LineBreakMeasurer(lineIterator, frc);

            while (measurer.getPosition() < lineIterator.getEndIndex())
            {
                TextLayout layout = measurer.nextLayout((float) width);

                layouts.add(layout);

                float lh = layout.getAscent() + layout.getDescent() + layout.getLeading();

                lineHeights.add(lh);
            }
        }

        float totalTextHeight = 0;

        for (Float h : lineHeights)
            totalTextHeight += h;

        float yOffset;

        if (alignment.name().startsWith("UPPER"))
            yOffset = 0;
        else if (alignment.name().startsWith("MIDDLE"))
            yOffset = (height - totalTextHeight) / 2f;
        else if (alignment.name().startsWith("LOWER"))
            yOffset = height - totalTextHeight;
        else
            yOffset = 0;

        for (int i = 0; i < layouts.size(); i++)
        {
            TextLayout layout = layouts.get(i);

            float lineHeight = lineHeights.get(i);
            float lineWidth = layout.getAdvance();
            float x;

            if (alignment.name().endsWith("LEFT"))
                x = 0;
            else if (alignment.name().endsWith("CENTER"))
                x = (width - lineWidth) / 2f;
            else if (alignment.name().endsWith("RIGHT"))
                x = width - lineWidth;
            else
                x = 0;

            float y = yOffset + layout.getAscent();

            layout.draw(g2d, x, y);

            yOffset += lineHeight;
        }

        g2d.dispose();

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

        int[] pixels = new int[width * height];

        image.getRGB(0, 0, width, height, pixels, 0, width);

        for (int pixel : pixels)
        {
            int a = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            buffer.put((byte) r);
            buffer.put((byte) g);
            buffer.put((byte) b);
            buffer.put((byte) a);
        }

        buffer.flip();

        return buffer;
    }

    public enum TextAlignment
    {
        UPPER_LEFT,
        UPPER_CENTER,
        UPPER_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,
        LOWER_LEFT,
        LOWER_CENTER,
        LOWER_RIGHT;
    }
}