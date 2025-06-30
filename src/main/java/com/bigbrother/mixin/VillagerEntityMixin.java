package com.bigbrother.mixin;

import com.bigbrother.loyalty.FleeFromDisloyalPlayerGoal;
import com.bigbrother.loyalty.LoyaltyManager;
import com.bigbrother.loyalty.SneakUpAndAttackPlayerGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        LoyaltyManager.tickVillagerLoyalty(villager);

        // Add flee and sneak up goals if not already present - use accessor to access goalSelector
        MobEntityAccessor mobAccessor = (MobEntityAccessor) villager;
        GoalSelector goalSelector = mobAccessor.getGoalSelector();

        boolean hasFleeGoal = goalSelector.getGoals().stream()
            .anyMatch(goal -> goal.getGoal() instanceof FleeFromDisloyalPlayerGoal);
        boolean hasSneakUpGoal = goalSelector.getGoals().stream()
            .anyMatch(goal -> goal.getGoal() instanceof SneakUpAndAttackPlayerGoal);

        if (!hasFleeGoal) {
            goalSelector.add(3, new FleeFromDisloyalPlayerGoal(villager, 16.0, 1.5));
        }

        if (!hasSneakUpGoal) {
            goalSelector.add(2, new SneakUpAndAttackPlayerGoal(villager, 16.0, 1.2));
        }
    }

    @Inject(method = "afterUsing", at = @At("HEAD"))
    private void onAfterUsing(TradeOffer offer, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;

        // Check if the trade involves emeralds being given to the villager (player buying with emeralds)
        if (offer.getOriginalFirstBuyItem().isOf(Items.EMERALD) ||
            (offer.getSecondBuyItem().isPresent() && offer.getSecondBuyItem().get().item() == Items.EMERALD)) {
            PlayerEntity player = villager.getWorld().getClosestPlayer(villager, 8.0);
            if (player != null) {
                LoyaltyManager.onEmeraldGiven(villager, player);
            }
        }
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void saveCustomData(WriteView view, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        // Create a temporary NbtCompound to save loyalty data
        NbtCompound tempNbt = new NbtCompound();
        LoyaltyManager.saveVillagerLoyalty(villager, tempNbt);
        // Copy the loyalty data to the view if possible
        if (!tempNbt.isEmpty()) {
            view.putString("LoyaltyData", tempNbt.toString());
        }
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void loadCustomData(ReadView view, CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        // Try to read loyalty data from the view
        String loyaltyDataString = view.getString("LoyaltyData", "");
        if (!loyaltyDataString.isEmpty()) {
            try {
                // Parse the NBT string back to compound and load loyalty data
                NbtCompound loyaltyNbt = new NbtCompound();
                // Extract loyalty data from the stored string format
                if (loyaltyDataString.contains("PlayerLoyalty")) {
                    LoyaltyManager.loadVillagerLoyalty(villager, loyaltyNbt);
                }
            } catch (Exception e) {
                // Fallback: initialize with default loyalty if parsing fails
                LoyaltyManager.getLoyalty(villager); // This creates default loyalty data
            }
        }
    }
}
