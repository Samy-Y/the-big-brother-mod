package com.bigbrother.loyalty;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class SneakUpAndAttackPlayerGoal extends Goal {
    private final VillagerEntity villager;
    private PlayerEntity targetPlayer;
    private final double sneakDistance;
    private final double attackDistance;
    private final double sneakSpeed;
    private int hitsDealt;
    private int maxHits;
    private int attackCooldown;
    private boolean isAttacking;
    private static final int ATTACK_COOLDOWN_TICKS = 20; // 1 second between attacks

    public SneakUpAndAttackPlayerGoal(VillagerEntity villager, double sneakDistance, double sneakSpeed) {
        this.villager = villager;
        this.sneakDistance = sneakDistance;
        this.attackDistance = 2.0; // Close enough to attack
        this.sneakSpeed = sneakSpeed;
        this.hitsDealt = 0;
        this.maxHits = 2 + villager.getRandom().nextInt(2); // 2-3 hits
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        // Only start if villager should run from player (disloyal) but not already fleeing
        PlayerEntity nearestPlayer = villager.getWorld().getClosestPlayer(villager, sneakDistance);
        if (nearestPlayer == null) return false;

        if (!LoyaltyManager.shouldRunFromPlayer(villager, nearestPlayer.getUuid())) {
            return false;
        }

        // 30% chance to sneak up instead of flee when disloyal
        if (villager.getRandom().nextFloat() > 0.3f) {
            return false;
        }

        // Check if villager can sneak up (player is not looking in villager's direction)
        if (canSneakUp(nearestPlayer)) {
            this.targetPlayer = nearestPlayer;
            this.hitsDealt = 0;
            this.maxHits = 2 + villager.getRandom().nextInt(2);
            this.isAttacking = false;
            this.attackCooldown = 0;
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (targetPlayer == null) return false;

        // Stop if we've dealt enough hits
        if (hitsDealt >= maxHits) return false;

        // Stop if player is too far away
        if (villager.squaredDistanceTo(targetPlayer) > sneakDistance * sneakDistance) {
            return false;
        }

        // Stop if player is now looking at us and we're not in attack range
        if (!isAttacking && !canSneakUp(targetPlayer) && villager.squaredDistanceTo(targetPlayer) > attackDistance * attackDistance) {
            return false;
        }

        return LoyaltyManager.shouldRunFromPlayer(villager, targetPlayer.getUuid());
    }

    @Override
    public void start() {
        // Reset attack state
        this.isAttacking = false;
        this.attackCooldown = 0;
    }

    @Override
    public void stop() {
        targetPlayer = null;
        villager.getNavigation().stop();
        isAttacking = false;
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        double distanceToPlayer = villager.squaredDistanceTo(targetPlayer);

        // If close enough to attack
        if (distanceToPlayer <= attackDistance * attackDistance) {
            if (!isAttacking) {
                isAttacking = true;
                villager.getNavigation().stop();
            }

            // Attack logic
            if (attackCooldown <= 0 && hitsDealt < maxHits) {
                // Look at player before attacking
                villager.getLookControl().lookAt(targetPlayer, 30.0F, 30.0F);

                // Deal damage
                targetPlayer.damage((ServerWorld) villager.getWorld(), villager.getWorld().getDamageSources().mobAttack(villager), 2.0f);
                hitsDealt++;
                attackCooldown = ATTACK_COOLDOWN_TICKS;

                // Apply loyalty penalty for attacking
                LoyaltyManager.applyHitPenalty(villager, targetPlayer.getUuid());
            } else if (attackCooldown > 0) {
                attackCooldown--;
            }
        } else {
            // Move towards player while staying hidden
            isAttacking = false;
            Vec3d sneakPosition = calculateSneakPosition();
            if (sneakPosition != null) {
                villager.getNavigation().startMovingTo(sneakPosition.x, sneakPosition.y, sneakPosition.z, sneakSpeed);
            }
        }
    }

    private boolean canSneakUp(PlayerEntity player) {
        Vec3d villagerPos = villager.getPos();
        Vec3d playerPos = player.getPos();

        // Get player's looking direction
        Vec3d playerLookDirection = player.getRotationVec(1.0f);

        // Calculate vector from player to villager
        Vec3d playerToVillager = villagerPos.subtract(playerPos).normalize();

        // Calculate the angle between player's look direction and player-to-villager vector
        double dotProduct = playerLookDirection.dotProduct(playerToVillager);

        // If dot product is negative, villager is behind player (can sneak up)
        // We use -0.5 as threshold, which corresponds to roughly 120 degrees field of view
        return dotProduct < -0.5;
    }

    private Vec3d calculateSneakPosition() {
        if (targetPlayer == null) return null;

        Vec3d villagerPos = villager.getPos();
        Vec3d playerPos = targetPlayer.getPos();
        Vec3d playerLookDirection = targetPlayer.getRotationVec(1.0f);

        // Try to find a position behind the player
        Vec3d behindPlayer = playerPos.subtract(playerLookDirection.multiply(3.0));

        // If we're already close to the ideal position, move directly towards player
        if (villagerPos.distanceTo(behindPlayer) < 2.0) {
            return playerPos;
        }

        return behindPlayer;
    }
}
