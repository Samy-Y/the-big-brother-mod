package com.bigbrother;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import java.util.List;

public class FollowEmeraldPlayerGoal extends Goal {
    private final VillagerEntity villager;
    private PlayerEntity targetPlayer;
    private final double followSpeed;
    private final double maxDistance;
    private final double minDistance;

    public FollowEmeraldPlayerGoal(VillagerEntity villager, double speed, double maxDistance, double minDistance) {
        this.villager = villager;
        this.followSpeed = speed;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
    }

    @Override
    public boolean canStart() {
        List<PlayerEntity> players = villager.getWorld().getEntitiesByClass(PlayerEntity.class, villager.getBoundingBox().expand(maxDistance),
                p -> p.getMainHandStack().isOf(Items.EMERALD) || p.getOffHandStack().isOf(Items.EMERALD));
        if (players.isEmpty()) return false;
        this.targetPlayer = players.get(0); // pick closest or first found
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return targetPlayer != null &&
                targetPlayer.isAlive() &&
                (targetPlayer.getMainHandStack().isOf(Items.EMERALD) || targetPlayer.getOffHandStack().isOf(Items.EMERALD)) &&
                villager.squaredDistanceTo(targetPlayer) <= maxDistance * maxDistance;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {
        this.targetPlayer = null;
        villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        villager.getLookControl().lookAt(targetPlayer);

        double dist = villager.squaredDistanceTo(targetPlayer);
        if (dist > minDistance * minDistance) {
            villager.getNavigation().startMovingTo(targetPlayer, followSpeed);
        } else {
            villager.getNavigation().stop();
        }
    }
}

