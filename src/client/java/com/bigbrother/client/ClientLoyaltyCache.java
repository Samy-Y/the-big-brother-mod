package com.bigbrother.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientLoyaltyCache {
    private static final Map<UUID, Integer> villagerLoyalty = new HashMap<>();

    public static void updateVillagerLoyalty(UUID villagerId, int loyalty) {
        villagerLoyalty.put(villagerId, loyalty);
    }

    public static int getVillagerLoyalty(UUID villagerId) {
        return villagerLoyalty.getOrDefault(villagerId, 50); // Default loyalty is 50
    }

    public static void clearCache() {
        villagerLoyalty.clear();
    }
}
