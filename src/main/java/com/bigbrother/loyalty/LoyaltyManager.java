package com.bigbrother.loyalty;

import com.bigbrother.entity.MilitiaVillagerEntity;
import com.bigbrother.loudspeaker.LoudspeakerBlockEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

public class LoyaltyManager {
    private static final WeakHashMap<VillagerEntity, VillagerLoyalty> villagerLoyaltyMap = new WeakHashMap<>();

    public static VillagerLoyalty getLoyalty(VillagerEntity villager) {
        return villagerLoyaltyMap.computeIfAbsent(villager, v -> new VillagerLoyalty());
    }

    public static int getLoyalty(VillagerEntity villager, UUID playerId) {
        return getLoyalty(villager).getLoyalty(playerId);
    }

    public static void addLoyalty(VillagerEntity villager, UUID playerId, int amount) {
        getLoyalty(villager).addLoyalty(playerId, amount);
    }

    public static void removeLoyalty(VillagerEntity villager, UUID playerId, int amount) {
        getLoyalty(villager).removeLoyalty(playerId, amount);
    }

    public static void applyHitPenalty(VillagerEntity villager, UUID playerId) {
        getLoyalty(villager).applyHitPenalty(playerId, villager.getWorld().getTime());
    }

    public static boolean shouldRunFromPlayer(VillagerEntity villager, UUID playerId) {
        return getLoyalty(villager).shouldRunFromPlayer(playerId, villager.getWorld().getTime()) && !(villager instanceof MilitiaVillagerEntity);
    }

    public static void applyLoudspeakerBonus(World world, LoudspeakerBlockEntity loudspeakerBlockEntity, BlockPos loudspeakerPos, int radius) {
        Box area = Box.of(loudspeakerPos.toCenterPos(), radius * 2, radius * 2, radius * 2);

        List<VillagerEntity> villagers = world.getEntitiesByClass(
            VillagerEntity.class,
            area,
            villager -> villager.squaredDistanceTo(loudspeakerPos.toCenterPos()) <= radius * radius
        );

        List<PlayerEntity> players = world.getEntitiesByClass(
            PlayerEntity.class,
            area,
            player -> player.squaredDistanceTo(loudspeakerPos.toCenterPos()) <= radius * radius
        );

        for (VillagerEntity villager : villagers) {
            VillagerLoyalty loyalty = getLoyalty(villager);
            for (PlayerEntity player : players) {
                if (player.getName().equals(loudspeakerBlockEntity.getOwner())) loyalty.addLoyalty(player.getUuid(), 10);
            }
        }
    }

    public static void onEmeraldGiven(VillagerEntity villager, PlayerEntity player) {
        addLoyalty(villager, player.getUuid(), 5);
    }

    public static void saveVillagerLoyalty(VillagerEntity villager, NbtCompound nbt) {
        VillagerLoyalty loyalty = villagerLoyaltyMap.get(villager);
        if (loyalty != null) {
            NbtCompound loyaltyNbt = new NbtCompound();
            loyalty.writeToNbt(loyaltyNbt);
            nbt.put("VillagerLoyalty", loyaltyNbt);
        }
    }

    public static void loadVillagerLoyalty(VillagerEntity villager, NbtCompound nbt) {
        if (nbt.contains("VillagerLoyalty")) {
            VillagerLoyalty loyalty = new VillagerLoyalty();
            NbtCompound loyaltyNbt = new NbtCompound();
            if (nbt.getCompoundOrEmpty("VillagerLoyalty") instanceof NbtCompound) {
                loyaltyNbt = nbt.getCompoundOrEmpty("VillagerLoyalty");
            }
            if (loyaltyNbt != null) {
                loyalty.readFromNbt(loyaltyNbt);
                villagerLoyaltyMap.put(villager, loyalty);
            }
        }
    }

    public static void tickVillagerLoyalty(VillagerEntity villager) {
        VillagerLoyalty loyalty = villagerLoyaltyMap.get(villager);
        if (loyalty != null) {
            loyalty.tick(villager.getWorld().getTime());
        }
    }
}
