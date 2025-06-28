package com.bigbrother.loyalty;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class FleeFromDisloyalPlayerGoal extends Goal {
    private final VillagerEntity villager;
    private PlayerEntity targetPlayer;
    private final double fleeDistance;
    private final double fleeSpeed;

    public FleeFromDisloyalPlayerGoal(VillagerEntity villager, double fleeDistance, double fleeSpeed) {
        this.villager = villager;
        this.fleeDistance = fleeDistance;
        this.fleeSpeed = fleeSpeed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        PlayerEntity nearestPlayer = villager.getWorld().getClosestPlayer(villager, fleeDistance);
        if (nearestPlayer == null) return false;

        // Check if villager should run from this player due to low loyalty
        if (LoyaltyManager.shouldRunFromPlayer(villager, nearestPlayer.getUuid())) {
            this.targetPlayer = nearestPlayer;
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (targetPlayer == null) return false;

        // Continue fleeing if player is still close and loyalty is still low
        return villager.squaredDistanceTo(targetPlayer) < fleeDistance * fleeDistance &&
               LoyaltyManager.shouldRunFromPlayer(villager, targetPlayer.getUuid());
    }

    @Override
    public void start() {
        // Start fleeing
    }

    @Override
    public void stop() {
        targetPlayer = null;
        villager.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        // Calculate flee direction (away from player)
        Vec3d villagerPos = villager.getPos();
        Vec3d playerPos = targetPlayer.getPos();
        Vec3d fleeDirection = villagerPos.subtract(playerPos).normalize();

        // Calculate flee position
        Vec3d fleePos = villagerPos.add(fleeDirection.multiply(10));

        // Navigate away from the player
        villager.getNavigation().startMovingTo(fleePos.x, fleePos.y, fleePos.z, fleeSpeed);
    }
}
