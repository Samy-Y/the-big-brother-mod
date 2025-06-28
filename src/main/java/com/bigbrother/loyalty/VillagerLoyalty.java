package com.bigbrother.loyalty;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VillagerLoyalty {
    private static final int DEFAULT_LOYALTY = 50;
    private static final int MAX_LOYALTY = 100;
    private static final int MIN_LOYALTY = 0;
    private static final int HIT_PENALTY_DURATION = 12000; // 10 minutes in ticks

    private final Map<UUID, Integer> playerLoyalty = new HashMap<>();
    private final Map<UUID, Long> hitPenalties = new HashMap<>(); // Player UUID -> expiry time

    public VillagerLoyalty() {
        // Default constructor
    }

    public int getLoyalty(UUID playerId) {
        return playerLoyalty.getOrDefault(playerId, DEFAULT_LOYALTY);
    }

    public void setLoyalty(UUID playerId, int loyalty) {
        loyalty = MathHelper.clamp(loyalty, MIN_LOYALTY, MAX_LOYALTY);
        playerLoyalty.put(playerId, loyalty);
    }

    public void addLoyalty(UUID playerId, int amount) {
        int current = getLoyalty(playerId);
        setLoyalty(playerId, current + amount);
    }

    public void removeLoyalty(UUID playerId, int amount) {
        int current = getLoyalty(playerId);
        setLoyalty(playerId, current - amount);
    }

    public void applyHitPenalty(UUID playerId, long worldTime) {
        removeLoyalty(playerId, 20);
        hitPenalties.put(playerId, worldTime + HIT_PENALTY_DURATION);
    }

    public boolean isUnderHitPenalty(UUID playerId, long worldTime) {
        Long penaltyExpiry = hitPenalties.get(playerId);
        if (penaltyExpiry == null) return false;

        if (worldTime >= penaltyExpiry) {
            hitPenalties.remove(playerId);
            return false;
        }
        return true;
    }

    public boolean shouldRunFromPlayer(UUID playerId, long worldTime) {
        return getLoyalty(playerId) < 25 || isUnderHitPenalty(playerId, worldTime);
    }

    public void tick(long worldTime) {
        // Clean up expired hit penalties
        hitPenalties.entrySet().removeIf(entry -> worldTime >= entry.getValue());
    }

    public void writeToNbt(NbtCompound nbt) {
        NbtCompound loyaltyNbt = new NbtCompound();
        for (Map.Entry<UUID, Integer> entry : playerLoyalty.entrySet()) {
            loyaltyNbt.putInt(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("PlayerLoyalty", loyaltyNbt);

        NbtCompound penaltiesNbt = new NbtCompound();
        for (Map.Entry<UUID, Long> entry : hitPenalties.entrySet()) {
            penaltiesNbt.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("HitPenalties", penaltiesNbt);
    }

    public void readFromNbt(NbtCompound nbt) {
        playerLoyalty.clear();
        hitPenalties.clear();

        if (nbt.contains("PlayerLoyalty")) {
            NbtCompound loyaltyNbt = nbt.getCompoundOrEmpty("PlayerLoyalty");
            if (loyaltyNbt != null) {
                for (String key : loyaltyNbt.getKeys()) {
                    try {
                        UUID playerId = UUID.fromString(key);
                        int loyalty = loyaltyNbt.getInt(key,DEFAULT_LOYALTY);
                        playerLoyalty.put(playerId, loyalty);
                    } catch (IllegalArgumentException ignored) {
                        // Invalid UUID, skip
                    }
                }
            }
        }

        if (nbt.contains("HitPenalties")) {
            NbtCompound penaltiesNbt = nbt.getCompoundOrEmpty("HitPenalties");
            if (penaltiesNbt != null) {
                for (String key : penaltiesNbt.getKeys()) {
                    try {
                        UUID playerId = UUID.fromString(key);
                        Long expiry = penaltiesNbt.getLong(key, HIT_PENALTY_DURATION);
                    } catch (IllegalArgumentException ignored) {
                        // Invalid UUID, skip
                    }
                }
            }
        }
    }
}
