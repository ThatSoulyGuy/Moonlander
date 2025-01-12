package com.thatsoulyguy.moonlander.util;

import com.thatsoulyguy.moonlander.annotation.CustomConstructor;
import com.thatsoulyguy.moonlander.annotation.EffectivelyNotNull;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@CustomConstructor("create")
public class AssetPath implements Serializable
{
    private @EffectivelyNotNull String domain;
    private @EffectivelyNotNull String localPath;

    private AssetPath() { }

    public @NotNull String getDomain()
    {
        return domain;
    }

    public @NotNull String getLocalPath()
    {
        return localPath;
    }

    public @NotNull String getFullPath()
    {
        return "/assets/" + domain + "/" + localPath;
    }

    public static @NotNull AssetPath create(@NotNull String domain, @NotNull String localPath)
    {
        AssetPath result = new AssetPath();

        result.domain = domain;
        result.localPath = localPath;

        return result;
    }
}