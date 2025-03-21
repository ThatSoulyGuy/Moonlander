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

        boolean[][][] clusterFilled = new boolean[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];

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

                            if (!clusterFilled[x][y][z])
                            {
                                double oreNoise = OpenSimplex2.noise3_ImproveXZ(
                                        getSeed() + 500,
                                        worldX * 0.1,
                                        worldY * 0.1,
                                        worldZ * 0.1
                                );

                                oreNoise = (oreNoise + 1) / 2.0;

                                Short targetOreId = null;

                                if (oreNoise > 0.9)
                                    targetOreId = BlockRegistry.BLOCK_ALUMINUM_ORE.getId();
                                else if (oreNoise > 0.85)
                                    targetOreId = BlockRegistry.BLOCK_IRON_ORE.getId();
                                else if (oreNoise > 0.8)
                                    targetOreId = BlockRegistry.BLOCK_REDSTONE_ORE.getId();
                                else if (oreNoise > 0.7)
                                    targetOreId = BlockRegistry.BLOCK_COAL_ORE.getId();
                                else if (oreNoise > 0.65)
                                    targetOreId = BlockRegistry.BLOCK_OIL.getId();

                                if (targetOreId != null)
                                    fillCluster(blocks, clusterFilled, x, y, z, 4, 4, 4, targetOreId);
                            }
                        }
                    }
                    else
                        blocks[x][y][z] = BlockRegistry.BLOCK_AIR.getId();
                }
            }
        }
    }

    private void fillCluster(short[][][] blocks, boolean[][][] clusterFilled, int sx, int sy, int sz, int dx, int dy, int dz, short targetId)
    {
        for (int ix = 0; ix < dx; ix++)
        {
            int x = sx + ix;

            if (x >= Chunk.SIZE)
                continue;

            for (int iy = 0; iy < dy; iy++)
            {
                int y = sy + iy;

                if (y >= Chunk.SIZE)
                    continue;

                for (int iz = 0; iz < dz; iz++)
                {
                    int z = sz + iz;

                    if (z >= Chunk.SIZE)
                        continue;

                    if (blocks[x][y][z] == BlockRegistry.BLOCK_STONE.getId())
                    {
                        blocks[x][y][z] = targetId;
                        clusterFilled[x][y][z] = true;
                    }
                }
            }
        }
    }

    private void fillOvalCluster(short[][][] blocks, boolean[][][] clusterFilled, int sx, int sy, int sz, int dx, int dy, int dz, short targetId)
    {
        float centerX = dx / 2f;
        float centerZ = dz / 2f;

        float radiusX = dx / 2f;
        float radiusZ = dz / 2f;

        for (int ix = 0; ix < dx; ix++)
        {
            int x = sx + ix;

            if (x < 0 || x >= Chunk.SIZE)
                continue;

            for (int iy = 0; iy < dy; iy++)
            {
                int y = sy + iy;

                if (y < 0 || y >= Chunk.SIZE)
                    continue;

                for (int iz = 0; iz < dz; iz++)
                {
                    int z = sz + iz;

                    if (z < 0 || z >= Chunk.SIZE)
                        continue;

                    float nx = (ix - centerX) / radiusX;
                    float nz = (iz - centerZ) / radiusZ;

                    if (nx * nx + nz * nz <= 1.0f)
                    {
                        if (blocks[x][y][z] == BlockRegistry.BLOCK_STONE.getId())
                        {
                            blocks[x][y][z] = targetId;
                            clusterFilled[x][y][z] = true;
                        }
                    }
                }
            }
        }
    }
}