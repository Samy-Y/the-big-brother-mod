package com.bigbrother.loudspeaker;

import com.bigbrother.ModBlockEntities;
import com.bigbrother.ModSounds;
import com.bigbrother.loyalty.LoyaltyManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoudspeakerBlockEntity extends BlockEntity implements BlockEntityTicker<LoudspeakerBlockEntity> {

    private static final String MOD_ID = "the-big-brother-mod";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static int isPlaying;

    public static final int BRIBE_RADIUS = 50; // the effect radius of the loudspeaker
    // the loudspeaker will affect villagers in a RADIUS block radius!
    private static final double PRICE_MULTIPLIER = 0.8;
    // how much the villagers will lower their prices.

    private final Set<VillagerEntity> bribedVillagers = new HashSet<>();

    public LoudspeakerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOUDSPEAKER, pos, state);
    }

    private String owner;
    public void setOwner(String username){
        owner = username;
    }
    public String getOwner() {
        return owner;
    }

    @Override
    public void writeData(WriteView nbt) {
        // Always call super.writeNbt first to save parent class data
        super.writeData(nbt);
        
        // Write the owner data if it exists
        if (owner != null) {
            nbt.putString("Owner", owner);
        }
    }

    @Override
    public void readData(ReadView nbt) {
        super.readData(nbt);
        // I SPENT LITERAL HOURS TO FIND OUT THAT IT'S NOT ACTUALLY readNbt() BUT readData()
        // 😭
        owner = nbt.getString("Owner","unknown");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, LoudspeakerBlockEntity be) {
        isPlaying++;
        // this is probably the worst implementation of a sound loop, ever.
        int time = isPlaying - 1;
        //LOGGER.info("Tick: " + time);
        if (time % 320 == 0) {
            world.playSound(null, pos, ModSounds.LOUDSPEAKER, SoundCategory.AMBIENT, 5f, 1f);
        }
        //LOGGER.info("Loudspeaker at " + pos + " is playing!");
        //if (world == null || world.isClient) return;

        Box area = new Box(pos).expand(BRIBE_RADIUS);
        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, area, VillagerEntity::isAlive);
        //LOGGER.info("Found " + villagers.size() + " villagers near loudspeaker.");

        Set<VillagerEntity> currentlyAdjacent = new HashSet<>();

        for (VillagerEntity villager : villagers) {
            if (villager.getPos().distanceTo(pos.toCenterPos()) <= 50) {
                applyBribeDiscount(villager);
                currentlyAdjacent.add(villager);
                //LOGGER.info("Bribed for " + PRICE_MULTIPLIER * 100 + "% off!");
            }
        }

        // Apply loyalty bonus every 500 seconds (10000 ticks) (approximately 8.3 minutes)
        if (time % 10000 == 0) {
            LoyaltyManager.applyLoudspeakerBonus(world, be, pos, BRIBE_RADIUS);
        }

        // Remove villagers that are no longer adjacent
        bribedVillagers.retainAll(currentlyAdjacent);
        // Add new adjacent villagers
        bribedVillagers.addAll(currentlyAdjacent);
    }

    private void applyBribeDiscount(VillagerEntity villager) {
        if (bribedVillagers.contains(villager)) return;

        // Apply discount to all trade offers by creating new offers with reduced prices
        for (TradeOffer offer : villager.getOffers()) {
            if (offer.getOriginalFirstBuyItem().getCount() > 1) {
                // Create a new ItemStack with reduced count for the discount
                int originalCount = offer.getOriginalFirstBuyItem().getCount();
                int discountedCount = Math.max(1, (int) (originalCount * PRICE_MULTIPLIER));

                // Note: In Minecraft 1.21.6, TradeOffer prices are immutable once created
                // The discount effect would need to be implemented differently,
                // possibly through a custom trade offer system or mixin
                // For now, we'll log the discount that would be applied
                //LOGGER.info("Would apply " + (100 - PRICE_MULTIPLIER * 100) + "% discount to trade offer");
            }
        }
    }

}