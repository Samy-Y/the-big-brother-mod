package com.bigbrother.entity;

import com.bigbrother.loyalty.LoyaltyManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.UUID;

public class MilitiaVillagerEntity extends VillagerEntity {
    private static String owner;
    private UUID lastAttackerUuid;
    private int attackCooldown = 0;
    private int previousBribeCount = 0;

    public MilitiaVillagerEntity(EntityType<? extends VillagerEntity> type, World world) {
        super(type, world);
        // Give the militia villager an iron sword
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
    }

    public static DefaultAttributeContainer.Builder createMilitiaVillagerAttributes() {
        return VillagerEntity.createVillagerAttributes()
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void initGoals() {
        // Add custom goals before calling super to ensure proper priority
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(2, new DefendOwnerGoal(this));
        this.goalSelector.add(3, new AttackOwnerTargetGoal(this));
        this.goalSelector.add(4, new PickupEmeraldGoal(this));

        super.initGoals();

        // Remove work-related goals to prevent job taking
        this.goalSelector.getGoals().removeIf(goal ->
            goal.getGoal().getClass().getSimpleName().contains("Work") ||
            goal.getGoal().getClass().getSimpleName().contains("Job"));
    }

    @Override
    public void tick() {
        super.tick();

        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player && isOwned()) {
            // Track who attacked the owner's militia villager
            if (!player.getName().getString().equals(owner)) {
                lastAttackerUuid = player.getUuid();
            }
        }
        return super.damage(world, source, amount);
    }

    // Override to prevent profession changes
    @Override
    public void setVillagerData(net.minecraft.village.VillagerData villagerData) {
        // Only allow profession to be NONE
        super.setVillagerData(villagerData.withProfession(Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE)));
    }

    @Override
    public void writeCustomData(WriteView nbt) {
        super.writeCustomData(nbt);
        if (owner != null) {
            nbt.putString("Owner", owner);
        }
        if (lastAttackerUuid != null) {
            nbt.putString("LastAttacker", lastAttackerUuid.toString());
        }
    }

    @Override
    public void readCustomData(ReadView nbt) {
        super.readCustomData(nbt);
        owner = nbt.getString("Owner", null);
        String uuidString = nbt.getString("LastAttacker", "");
        if (!uuidString.isEmpty()) {
            try {
                lastAttackerUuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException ignored) {
                lastAttackerUuid = null;
            }
        }
    }

    public boolean isOwned() {
        return owner != null;
    }

    public String getOwner() {
        return owner;
    }

    public PlayerEntity getOwnerPlayer() {
        if (owner == null || getWorld().isClient) return null;

        if (getWorld() instanceof ServerWorld serverWorld) {
            return serverWorld.getServer().getPlayerManager().getPlayer(owner);
        }
        return null;
    }

    public void setOwner(String ownerName) {
        this.owner = ownerName;
    }

    public boolean tryAttack(LivingEntity target) {
        if (attackCooldown > 0) return false;

        float damage = (float) this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        boolean success = target.damage((ServerWorld) this.getWorld(), this.getDamageSources().mobAttack(this), damage);

        if (success) {
            attackCooldown = 20; // 1 second cooldown
        }

        return success;
    }

    // Custom AI Goals
    private static class PickupEmeraldGoal extends Goal {
        private final MilitiaVillagerEntity militia;
        private ItemEntity targetEmerald;

        public PickupEmeraldGoal(MilitiaVillagerEntity militia) {
            this.militia = militia;
        }

        @Override
        public boolean canStart() {
            if (militia.isOwned()) return false;

            List<ItemEntity> emeralds = militia.getWorld().getEntitiesByClass(
                ItemEntity.class,
                militia.getBoundingBox().expand(8.0),
                item -> item.getStack().isOf(Items.EMERALD) && !item.getStack().isEmpty()
            );

            if (emeralds.isEmpty()) return false;

            targetEmerald = emeralds.get(0);
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return targetEmerald != null && targetEmerald.isAlive() &&
                   militia.squaredDistanceTo(targetEmerald) <= 64.0;
        }

        @Override
        public void tick() {
            if (targetEmerald == null) return;

            militia.getLookControl().lookAt(targetEmerald);
            militia.getNavigation().startMovingTo(targetEmerald, 1.0);

            if (militia.squaredDistanceTo(targetEmerald) <= 2.0) {
                // Bribe the militia villager
                PlayerEntity nearest = militia.getWorld().getClosestPlayer(militia, 8.0);
                if (nearest != null && !militia.getWorld().isClient) {
                    militia.setOwner(nearest.getName().getString());
                    owner = nearest.getName().getString();
                    nearest.sendMessage(Text.literal("Militia villager bribed by " + nearest.getName().getString() + "!"), true);

                    ItemStack stack = targetEmerald.getStack();
                    stack.decrement(1);
                    if (stack.isEmpty()) {
                        targetEmerald.discard();
                    }
                }
                targetEmerald = null;
            }
        }

        @Override
        public void stop() {
            targetEmerald = null;
            militia.getNavigation().stop();
        }
    }

    private static class DefendOwnerGoal extends Goal {
        private final MilitiaVillagerEntity militia;
        private LivingEntity target;

        public DefendOwnerGoal(MilitiaVillagerEntity militia) {
            this.militia = militia;
        }

        @Override
        public boolean canStart() {
            if (!militia.isOwned()) return false;
            // Check if someone attacked the militia villager
            if (militia.lastAttackerUuid != null && militia.getServer() != null) {
                ServerPlayerEntity lastAttacker = militia.getServer().getPlayerManager().getPlayer(militia.lastAttackerUuid);
                if (lastAttacker != null && lastAttacker.isAlive() && militia.squaredDistanceTo(lastAttacker) <= 256.0) {
                    if (militia.getOwnerPlayer() != null) {
                        // Check if they are loyal enough
                        if (LoyaltyManager.getLoyalty(militia, militia.getOwnerPlayer().getUuid()) < 25) {
                            // Sends message in red color
                            militia.getOwnerPlayer().sendMessage(Text.literal("A militia villager won't defend you, they are too disloyal.").withColor(10), true);
                            return false;
                        } else {
                            militia.getOwnerPlayer().sendMessage(Text.literal("Militia villager is defending you!"), true);
                        }
                    }
                    target = lastAttacker;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean shouldContinue() {
            return target != null && target.isAlive() && militia.isOwned() &&
                   militia.squaredDistanceTo(target) <= 400.0;
        }

        @Override
        public void tick() {
            if (target == null) return;

            militia.getLookControl().lookAt(target);
            militia.getNavigation().startMovingTo(target, 1.2);

            if (militia.squaredDistanceTo(target) <= 4.0) {
                militia.tryAttack(target);
            }
        }

        @Override
        public void stop() {
            target = null;
            militia.getNavigation().stop();
            militia.lastAttackerUuid = null;
        }
    }

    // New AI Goal to attack what the owner attacks
    private static class AttackOwnerTargetGoal extends Goal {
        private final MilitiaVillagerEntity militia;
        private LivingEntity target;
        private int targetSearchDelay = 0;

        public AttackOwnerTargetGoal(MilitiaVillagerEntity militia) {
            this.militia = militia;
        }

        @Override
        public boolean canStart() {
            if (!militia.isOwned()) return false;

            if (targetSearchDelay > 0) {
                targetSearchDelay--;
                return false;
            }

            // Find owner player
            ServerPlayerEntity owner = null;
            if (militia.getServer() != null) {
                for (ServerPlayerEntity player : militia.getServer().getPlayerManager().getPlayerList()) {
                    if (player.getName().getString().equals(militia.getOwner())) {
                        owner = player;
                        break;
                    }
                }
            }

            if (owner == null) return false;

            // Check if owner is attacking something
            LivingEntity ownerTarget = owner.getAttacker();
            if (ownerTarget == null) {
                // Also check what the owner is looking at and recently attacked
                ownerTarget = owner.getAttacking();
            }

            if (ownerTarget != null && ownerTarget != militia && ownerTarget.isAlive() &&
                militia.squaredDistanceTo(ownerTarget) <= 256.0) {
                // Check if they are loyal enough
                if (LoyaltyManager.getLoyalty(militia, militia.getOwnerPlayer().getUuid()) < 25) {
                    // Sends message in red color
                    militia.getOwnerPlayer().sendMessage(Text.literal("A militia villager won't defend you, they are too disloyal.").withColor(10), true);
                    return false;
                } else {
                    militia.getOwnerPlayer().sendMessage(Text.literal("Militia villager is attacking " + ownerTarget.getName().getString()), true);
                }
                target = ownerTarget;
                return true;
            }

            targetSearchDelay = 20; // Check every second
            return false;
        }

        @Override
        public boolean shouldContinue() {
            return target != null && target.isAlive() && militia.isOwned() &&
                   militia.squaredDistanceTo(target) <= 400.0;
        }

        @Override
        public void tick() {
            if (target == null) return;

            militia.getLookControl().lookAt(target);
            militia.getNavigation().startMovingTo(target, 1.2);

            if (militia.squaredDistanceTo(target) <= 4.0) {
                militia.tryAttack(target);
            }
        }

        @Override
        public void stop() {
            target = null;
            militia.getNavigation().stop();
            targetSearchDelay = 10; // Small delay before searching again
        }
    }
}
