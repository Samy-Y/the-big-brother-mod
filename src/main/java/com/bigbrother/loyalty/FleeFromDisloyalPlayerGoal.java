package com.bigbrother.loyalty;

import com.bigbrother.mixin.MobEntityAccessor;
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
            // Don't flee if sneak up goal is active (higher priority)
            if (isSneakUpGoalActive()) {
                return false;
            }

            this.targetPlayer = nearestPlayer;
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (targetPlayer == null) return false;

        // Stop fleeing if sneak up goal becomes active
        if (isSneakUpGoalActive()) {
            return false;
        }

        // Continue fleeing if player is still close and loyalty is still low
        return villager.squaredDistanceTo(targetPlayer) < fleeDistance * fleeDistance &&
               LoyaltyManager.shouldRunFromPlayer(villager, targetPlayer.getUuid());
    }

    private boolean isSneakUpGoalActive() {
        // Check if any sneak up goal is currently running
        MobEntityAccessor mobAccessor = (MobEntityAccessor) villager;
        return mobAccessor.getGoalSelector().getGoals().stream()
            .anyMatch(goal -> goal.getGoal() instanceof SneakUpAndAttackPlayerGoal &&
                     goal.isRunning());
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
