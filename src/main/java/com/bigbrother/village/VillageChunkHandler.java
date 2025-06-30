package com.bigbrother.village;

import com.bigbrother.ModEntities;
import com.bigbrother.entity.MilitiaVillagerEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.structure.Structure;

import java.util.Map;
import java.util.Random;

public class VillageChunkHandler {
    private static final Random RANDOM = new Random();

    public static void initialize() {
        // Register chunk load event to detect villages and spawn militia
        ServerChunkEvents.CHUNK_LOAD.register(VillageChunkHandler::onChunkLoad);
    }

    private static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        // Check if this chunk contains any village structures
        Map<Structure, StructureStart> structures = chunk.getStructureStarts();

        for (Map.Entry<Structure, StructureStart> entry : structures.entrySet()) {
            Structure structure = entry.getKey();
            StructureStart structureStart = entry.getValue();

            // Check if this is a village structure
            if (isVillageStructure(structure) && structureStart.hasChildren()) {
                // Get the village center
                BlockPos villageCenter = structureStart.getBoundingBox().getCenter();

                // Check if we've already spawned militia for this village
                if (!hasSpawnedMilitiaForVillage(world, villageCenter)) {
                    spawnMilitiaInVillage(world, villageCenter);
                    markVillageAsProcessed(world, villageCenter);
                }
            }
        }
    }

    private static boolean isVillageStructure(Structure structure) {
        // Check if the structure is a village by examining its registry key
        String structureId = structure.getType().toString().toLowerCase();
        return structureId.contains("village");
    }

    private static void spawnMilitiaInVillage(ServerWorld world, BlockPos villageCenter) {
        // Determine number of militia to spawn (2-10)
        int militiaCount = 2 + RANDOM.nextInt(9); // 2 + (0-8) = 2-10

        System.out.println("Spawning " + militiaCount + " militia villagers for village at " + villageCenter);

        // Spawn militia villagers around the village center
        for (int i = 0; i < militiaCount; i++) {
            // Find a suitable spawn position around the village center
            BlockPos spawnPos = findSuitableSpawnPos(world, villageCenter, 40);

            if (spawnPos != null) {
                // Create and spawn militia villager
                MilitiaVillagerEntity militia = new MilitiaVillagerEntity(ModEntities.MILITIA_VILLAGER, world);
                militia.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    RANDOM.nextFloat() * 360.0F,
                    0.0F
                );

                // Initialize the militia villager
                militia.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.STRUCTURE, null);

                // Add to world
                world.spawnEntity(militia);

                System.out.println("Spawned militia villager " + (i + 1) + "/" + militiaCount + " at " + spawnPos);
            } else {
                System.out.println("Could not find suitable spawn position for militia villager " + (i + 1));
            }
        }
    }

    private static BlockPos findSuitableSpawnPos(ServerWorld world, BlockPos center, int radius) {
        // Try to find a suitable spawn position within the given radius
        for (int attempts = 0; attempts < 30; attempts++) {
            int x = center.getX() + RANDOM.nextInt(radius * 2) - radius;
            int z = center.getZ() + RANDOM.nextInt(radius * 2) - radius;

            // Find the surface Y level using the correct world height method
            int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            int y = topY - 1;
            while (y > world.getBottomY() && world.getBlockState(new BlockPos(x, y, z)).isAir()) {
                y--;
            }
            y++; // Move one block up to spawn on top of the surface

            BlockPos spawnPos = new BlockPos(x, y, z);

            // Check if the position is suitable (solid block below, air above)
            if (world.getBlockState(spawnPos.down()).isSolid() &&
                world.getBlockState(spawnPos).isAir() &&
                world.getBlockState(spawnPos.up()).isAir() &&
                y > world.getSeaLevel() - 5) { // Don't spawn too far underground
                return spawnPos;
            }
        }

        return null; // Couldn't find a suitable position
    }

    private static boolean hasSpawnedMilitiaForVillage(ServerWorld world, BlockPos villageCenter) {
        // Check if there are already militia villagers near this village center
        return world.getEntitiesByClass(MilitiaVillagerEntity.class,
            new net.minecraft.util.math.Box(villageCenter).expand(50),
            entity -> true).size() >= 2;
    }

    private static void markVillageAsProcessed(ServerWorld world, BlockPos villageCenter) {
        // This could be enhanced with persistent data storage if needed
        // For now, we rely on the hasSpawnedMilitiaForVillage check
    }
}
