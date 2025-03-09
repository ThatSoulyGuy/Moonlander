package com.thatsoulyguy.moonlander.core;

import com.thatsoulyguy.moonlander.annotation.Static;

@Static
public class Time
{
    private static long startTime = System.nanoTime();
    private static long lastTime = System.nanoTime();
    private static float deltaTime = 0;
    private static int frames = 0;
    private static float fps = 0;
    private static float timeElapsed = 0;

    private Time() { }

    public static float getDeltaTime()
    {
        deltaTime = Math.min(deltaTime, 0.03f);

        return deltaTime;
    }

    public static float getFPS()
    {
        return fps;
    }

    public static float getTime()
    {
        return (System.nanoTime() - startTime) / 1_000_000_000.0f;
    }

    public static void update()
    {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;

        frames++;
        timeElapsed += deltaTime;

        if (timeElapsed >= 1.0f)
        {
            fps = frames / timeElapsed;
            frames = 0;
            timeElapsed = 0;
        }
    }

    public static void reset()
    {
        startTime = System.nanoTime();
        lastTime = System.nanoTime();
        deltaTime = 0;
        frames = 0;
        fps = 0;
        timeElapsed = 0;
    }
}