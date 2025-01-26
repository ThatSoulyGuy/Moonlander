package com.thatsoulyguy.moonlander.world.terraingenerators;

import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.noise.OpenSimplex2;
import com.thatsoulyguy.moonlander.world.Chunk;
import com.thatsoulyguy.moonlander.world.TerrainGenerator;
import com.thatsoulyguy.moonlander.world.World;
import org.joml.Vector3i;

public class GroundTerrainGenerator extends TerrainGenerator
{
    public int getHeight(int x, int z)
    {
        double flatnessControl = OpenSimplex2.noise2(getSeed() + 300, x * getScale() * 0.1, z * getScale() * 0.1);
        flatnessControl = (flatnessControl + 1) / 2.0;

        double hillThreshold = 0.8;

        double baseNoise = OpenSimplex2.noise2(getSeed(), x * getScale() * 0.1, z * getScale() * 0.1);
        baseNoise = (baseNoise + 1) / 2.0;

        double hillNoise = 0;

        if (flatnessControl > hillThreshold)
        {
            hillNoise = OpenSimplex2.noise2(getSeed() + 100, x * getScale() * 0.1, z * getScale() * 0.1);
            hillNoise = (hillNoise + 1) / 2.0;
        }

        double combinedNoise = flatnessControl <= hillThreshold
                ? baseNoise * 0.1
                : (0.7 * baseNoise + 0.3 * hillNoise);

        int maxTerrainHeight = World.WORLD_HEIGHT / 4;

        int rawHeight = (int) (combinedNoise * maxTerrainHeight);

        int minHeight = 6 * Chunk.SIZE;

        return rawHeight + minHeight;
    }

    @Override
    public void generateBlocks(short[][][] blocks, Vector3i chunkPosition)
    {
        int worldXOffset = chunkPosition.x * Chunk.SIZE;
        int worldZOffset = chunkPosition.z * Chunk.SIZE;
        int worldYOffset = chunkPosition.y * Chunk.SIZE;

        for (int x = 0; x < Chunk.SIZE; x++)
        {
            for (int z = 0; z < Chunk.SIZE; z++)
            {
                int worldX = worldXOffset + x;
                int worldZ = worldZOffset + z;

                int terrainHeight = getHeight(worldX, worldZ);

                for (int y = 0; y < Chunk.SIZE; y++)
                {
                    int worldY = worldYOffset + y;

                    if (worldY < terrainHeight)
                    {
                        if (worldY == terrainHeight - 1)
                            blocks[x][y][z] = BlockRegistry.BLOCK_SOFT_MOON_ROCK.getId();
                        else if (worldY >= terrainHeight - 5)
                            blocks[x][y][z] = BlockRegistry.BLOCK_MOON_ROCK.getId();
                        else
                        {
                            blocks[x][y][z] = BlockRegistry.BLOCK_STONE.getId();

                            double oreNoise = OpenSimplex2.noise3_ImproveXZ(
                                    getSeed() + 500,
                                    worldX * 0.1,
                                    worldY * 0.1,
                                    worldZ * 0.1
                            );

                            oreNoise = (oreNoise + 1) / 2.0;

                            if (oreNoise > 0.9)
                                blocks[x][y][z] = BlockRegistry.BLOCK_ALUMINUM_ORE.getId();
                            else if (oreNoise > 0.85)
                                blocks[x][y][z] = BlockRegistry.BLOCK_IRON_ORE.getId();
                            else if (oreNoise > 0.8)
                                blocks[x][y][z] = BlockRegistry.BLOCK_REDSTONE_ORE.getId();
                            else if (oreNoise > 0.78)
                                blocks[x][y][z] = BlockRegistry.BLOCK_OIL.getId();
                            else if (oreNoise > 0.7)
                                blocks[x][y][z] = BlockRegistry.BLOCK_COAL_ORE.getId();
                        }
                    }
                    else
                        blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getId();
                }
            }
        }
    }
}