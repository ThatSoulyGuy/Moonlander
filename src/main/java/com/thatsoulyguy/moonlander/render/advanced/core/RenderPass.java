package com.thatsoulyguy.moonlander.render.advanced.core;

import com.thatsoulyguy.moonlander.render.Camera;
import org.jetbrains.annotations.Nullable;

public interface RenderPass
{
    void initialize();

    void render(@Nullable Camera camera);

    void uninitialize();
}