package com.thatsoulyguy.moonlander.core;

import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;

@Static
public class GameManager
{
    private static GameState state = GameState.IN_MENU;

    private GameManager() { }

    public static void setState(@NotNull GameState state)
    {
        GameManager.state = state;
    }

    public static @NotNull GameState getState()
    {
        return state;
    }
}