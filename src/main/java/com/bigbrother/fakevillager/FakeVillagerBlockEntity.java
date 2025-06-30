package com.bigbrother.fakevillager;

import com.bigbrother.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FakeVillagerBlockEntity extends BlockEntity {
    private String biomeType = "plains";
    private final Map<VillagerEntity, VillagerStareData> staringVillagers = new HashMap<>();
    private final Random random = new Random();

    public FakeVillagerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FAKE_VILLAGER, pos, state);
    }

    public void setBiomeType(RegistryEntry<Biome> biome) {
        if (biome.matchesKey(BiomeKeys.DESERT)) {
            this.biomeType = "desert";
        } else if (biome.matchesKey(BiomeKeys.SAVANNA) || biome.matchesKey(BiomeKeys.SAVANNA_PLATEAU)) {
            this.biomeType = "savanna";
        } else if (biome.matchesKey(BiomeKeys.SNOWY_PLAINS) || biome.matchesKey(BiomeKeys.SNOWY_TAIGA)) {
            this.biomeType = "snow";
        } else if (biome.matchesKey(BiomeKeys.JUNGLE) || biome.matchesKey(BiomeKeys.SPARSE_JUNGLE)) {
            this.biomeType = "jungle";
        } else if (biome.matchesKey(BiomeKeys.SWAMP) || biome.matchesKey(BiomeKeys.MANGROVE_SWAMP)) {
            this.biomeType = "swamp";
        } else if (biome.matchesKey(BiomeKeys.TAIGA) || biome.matchesKey(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA)) {
            this.biomeType = "taiga";
        } else {
            this.biomeType = "plains";
        }
        markDirty();
    }

    public String getBiomeType() {
        return biomeType;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FakeVillagerBlockEntity blockEntity) {
        if (world instanceof ServerWorld serverWorld) {
            blockEntity.tickVillagerInteraction(serverWorld, pos);
        }
    }

    private void tickVillagerInteraction(ServerWorld world, BlockPos pos) {
        // Find villagers within 5 block radius
        Box searchBox = new Box(pos).expand(5.0);
        List<VillagerEntity> nearbyVillagers = world.getEntitiesByClass(VillagerEntity.class, searchBox,
            villager -> villager.isAlive() && !villager.isBaby());

        // Update existing staring villagers
        staringVillagers.entrySet().removeIf(entry -> {
            VillagerEntity villager = entry.getKey();
            VillagerStareData stareData = entry.getValue();

            if (!nearbyVillagers.contains(villager) || !villager.isAlive()) {
                return true; // Remove from map
            }

            // Check if villager is still looking at the fake villager
            if (isVillagerLookingAt(villager, pos)) {
                stareData.stareTime++;

                // After 10 seconds (200 ticks), make villager angry and run away
                if (stareData.stareTime >= 200 && !stareData.hasBeenAngered) {
                    makeVillagerAngryAndRunAway(world, villager, pos);
                    stareData.hasBeenAngered = true;
                }
                return false; // Keep in map
            } else {
                return true; // Remove from map if not looking anymore
            }
        });

        // Check for new villagers that might start staring
        for (VillagerEntity villager : nearbyVillagers) {
            if (!staringVillagers.containsKey(villager) && isVillagerLookingAt(villager, pos)) {
                // 20% chance to start staring
                if (random.nextFloat() < 0.2f) {
                    staringVillagers.put(villager, new VillagerStareData());
                }
            }
        }
    }

    private boolean isVillagerLookingAt(VillagerEntity villager, BlockPos blockPos) {
        Vec3d villagerPos = villager.getPos();
        Vec3d blockCenter = Vec3d.ofCenter(blockPos);
        Vec3d lookDirection = villager.getRotationVec(1.0f);
        Vec3d toBlock = blockCenter.subtract(villagerPos).normalize();

        // Check if the villager is facing roughly towards the block (within 45 degrees)
        double dotProduct = lookDirection.dotProduct(toBlock);
        return dotProduct > 0.7; // cos(45°) ≈ 0.707
    }

    private void makeVillagerAngryAndRunAway(ServerWorld world, VillagerEntity villager, BlockPos blockPos) {
        // Spawn anger particles
        Vec3d villagerPos = villager.getPos();
        for (int i = 0; i < 5; i++) {
            world.spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                villagerPos.x + (random.nextDouble() - 0.5) * 0.5,
                villagerPos.y + villager.getHeight() + 0.5,
                villagerPos.z + (random.nextDouble() - 0.5) * 0.5,
                1, 0, 0, 0, 0);
        }

        // Make villager run away in a random direction
        double angle = random.nextDouble() * 2 * Math.PI;
        double runDistance = 10.0 + random.nextDouble() * 10.0; // Run 10-20 blocks away

        Vec3d runDirection = new Vec3d(Math.cos(angle), 0, Math.sin(angle));
        Vec3d targetPos = villagerPos.add(runDirection.multiply(runDistance));

        // Set villager's movement target to run away
        villager.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, 1.5); // Fast movement

        // Add some panic behavior
        villager.setTarget(null); // Clear any current target
    }

    @Override
    protected void writeData(WriteView nbt) {
        super.writeData(nbt);
        nbt.putString("BiomeType", biomeType);
    }

    @Override
    protected void readData(ReadView nbt) {
        super.readData(nbt);
        biomeType = nbt.getString("BiomeType","plains");
    }

    private static class VillagerStareData {
        int stareTime = 0;
        boolean hasBeenAngered = false;
    }
}
