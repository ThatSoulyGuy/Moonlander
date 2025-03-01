package com.thatsoulyguy.moonlander.world.regionalspawners;

import com.thatsoulyguy.moonlander.entity.entities.EntityAlien;
import com.thatsoulyguy.moonlander.world.RegionalSpawner;
import com.thatsoulyguy.moonlander.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AlienRegionalSpawner extends RegionalSpawner
{
    @Override
    public void onSpawnCycle(@NotNull World world, @NotNull Vector3f selectedPosition)
    {
        world.spawnEntity(selectedPosition, EntityAlien.class);
    }
}
