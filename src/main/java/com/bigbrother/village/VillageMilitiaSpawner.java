package com.bigbrother.village;

import com.bigbrother.ModEntities;
import com.bigbrother.entity.MilitiaVillagerEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;

import java.util.Random;

public class VillageMilitiaSpawner {

    public static void spawnMilitiaInVillage(StructureWorldAccess world, BlockPos villageCenter, Random random) {
        if (world.isClient()) return;

        // Determine number of militia to spawn (2-10)
        int militiaCount = 2 + random.nextInt(9); // 2 + (0-8) = 2-10

        // Spawn militia villagers around the village center
        for (int i = 0; i < militiaCount; i++) {
            // Find a suitable spawn position around the village center
            BlockPos spawnPos = findSuitableSpawnPos(world, villageCenter, random, 32);

            if (spawnPos != null) {
                // Create and spawn militia villager
                MilitiaVillagerEntity militia = new MilitiaVillagerEntity(ModEntities.MILITIA_VILLAGER, world.toServerWorld());
                militia.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    random.nextFloat() * 360.0F,
                    0.0F
                );

                // Initialize the militia villager
                militia.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.STRUCTURE, null);

                // Add to world
                world.spawnEntity(militia);
            }
        }
    }

    private static BlockPos findSuitableSpawnPos(StructureWorldAccess world, BlockPos center, Random random, int radius) {
        // Try to find a suitable spawn position within the given radius
        for (int attempts = 0; attempts < 20; attempts++) {
            int x = center.getX() + random.nextInt(radius * 2) - radius;
            int z = center.getZ() + random.nextInt(radius * 2) - radius;

            // Find the surface Y level using the height map
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) - 1;
            while (y > world.getBottomY() && world.getBlockState(new BlockPos(x, y, z)).isAir()) {
                y--;
            }
            y++; // Move one block up to spawn on top of the surface

            BlockPos spawnPos = new BlockPos(x, y, z);

            // Check if the position is suitable (solid block below, air above)
            if (!world.getBlockState(spawnPos.down()).isAir() &&
                world.getBlockState(spawnPos).isAir() &&
                world.getBlockState(spawnPos.up()).isAir()) {
                return spawnPos;
            }
        }

        return null; // Couldn't find a suitable position
    }
}
