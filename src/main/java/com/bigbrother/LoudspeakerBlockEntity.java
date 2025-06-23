package com.bigbrother;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class LoudspeakerBlockEntity extends BlockEntity implements BlockEntityTicker<LoudspeakerBlockEntity> {

    private static int isPlaying;

    private static final int RADIUS = 50; // the effect radius of the loudspeaker
    // the loudspeaker will affect villagers in a RADIUS block radius!
    private static final double PRICE_MULTIPLIER = 0.8;
    // how much the villagers will lower their prices.

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
        if (isPlaying == 1) world.playSound(null,pos,ModSounds.LOUDSPEAKER, SoundCategory.AMBIENT, 1f, 1f);
        if (world.isClient()) return; // server side only

        Box area = new Box(pos).expand(RADIUS);

        List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, area, villager -> true);

        for (VillagerEntity villager : villagers) {
            applyPriceReduction(villager);
        }
    }

    private void applyPriceReduction(VillagerEntity villager) {

    }
}