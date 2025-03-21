package com.thatsoulyguy.moonlander.world.terraingenerators;

import com.thatsoulyguy.moonlander.block.BlockRegistry;
import com.thatsoulyguy.moonlander.noise.OpenSimplex2;
import com.thatsoulyguy.moonlander.world.Chunk;
import com.thatsoulyguy.moonlander.world.TerrainGenerator;
import org.joml.Vector3i;

public class CaveTerrainGenerator extends TerrainGenerator
{
    private static final double CAVE_THRESHOLD = 0.7;
    private static final double PRIMARY_SCALE = 0.02;
    private static final double DETAIL_SCALE = 0.05;
    private static final double DETAIL_WEIGHT = 0.1;

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
                for (int y = 0; y < Chunk.SIZE; y++)
                {
                    int worldX = worldXOffset + x;
                    int worldY = worldYOffset + y;
                    int worldZ = worldZOffset + z;

                    double primaryNoise = OpenSimplex2.noise3_ImproveXZ(getSeed(),
                            worldX * PRIMARY_SCALE,
                            worldY * PRIMARY_SCALE,
                            worldZ * PRIMARY_SCALE);
                    primaryNoise = (primaryNoise + 1) / 2.0;

                    double detailNoise = OpenSimplex2.noise3_ImproveXZ(getSeed() + 1000,
                            worldX * DETAIL_SCALE,
                            worldY * DETAIL_SCALE,
                            worldZ * DETAIL_SCALE);
                    detailNoise = (detailNoise + 1) / 2.0;

                    double caveNoise = (primaryNoise * (1 - DETAIL_WEIGHT) + detailNoise * DETAIL_WEIGHT);

                    double depthBias = 0;

                    if (worldY < 30)
                        depthBias = (30 - worldY) / 30.0 * 0.2;

                    caveNoise -= depthBias;

                    if (caveNoise > CAVE_THRESHOLD)
                        blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getId();
                }
            }
        }
    }
}
