package com.thatsoulyguy.moonlander.entity.model.models;

import com.thatsoulyguy.moonlander.entity.model.EntityModel;
import com.thatsoulyguy.moonlander.entity.model.ModelPart;
import com.thatsoulyguy.moonlander.render.Texture;
import com.thatsoulyguy.moonlander.render.TextureManager;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Objects;

public class ModelAlien extends EntityModel
{
    @Override
    public void initialize()
    {
        addPart(
                ModelPart.create("right_leg")
                        .setPosition(new Vector3f(-3.0f, 16.0f, 0.0f))
                        .setPivot(new Vector3f(-3.0f, 0.0f, 0.0f))
                        .addCube(
                                ModelPart.Cube.create()
                                        .setUVs(new Vector2i(24, 22))
                                        .setPosition(new Vector3f(-1.0f, 0.0f, -1.0f))
                                        .setSize(new Vector3f(2.0f, 8.0f, 2.0f))
                        )
        );

        addPart(
                ModelPart.create("left_leg")
                        .setPosition(new Vector3f(3.0f, 16.0f, 0.0f))
                        .addCube(
                                ModelPart.Cube.create()
                                        .setUVs(new Vector2i(32, 22))
                                        .setPosition(new Vector3f(-1.0f, 0.0f, -1.0f))
                                        .setSize(new Vector3f(2.0f, 8.0f, 2.0f))
                        )
        );

        addPart(
                ModelPart.create("head")
                        .setPosition(new Vector3f(0.0f, 4.0f, 0.0f))
                        .addCube(
                                ModelPart.Cube.create()
                                        .setUVs(new Vector2i(0, 22))
                                        .setPosition(new Vector3f(-3.0f, -6.0f, -3.0f))
                                        .setSize(new Vector3f(6.0f, 6.0f, 6.0f))
                        )
        );

        addPart(
                ModelPart.create("body")
                        .setPosition(new Vector3f(0.0f, 24.0f, 0.0f))
                        .addCube(
                                ModelPart.Cube.create()
                                        .setUVs(new Vector2i(0, 0))
                                        .setPosition(new Vector3f(-7.0f, -20.0f, -5.0f))
                                        .setSize(new Vector3f(14.0f, 12.0f, 10.0f))
                        )
        );
    }

    @Override
    public @NotNull Texture getTexture()
    {
        return Objects.requireNonNull(TextureManager.get("entity.alien"));
    }
}