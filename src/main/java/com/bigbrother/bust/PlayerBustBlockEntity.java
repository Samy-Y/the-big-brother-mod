package com.bigbrother.bust;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import com.bigbrother.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class PlayerBustBlockEntity extends BlockEntity {
    private static String owner;

    public PlayerBustBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLAYER_BUST, pos, state);
        System.out.println("PlayerBustBlockEntity created at position: " + pos);
    }

    public void setOwner(String s_owner) {
        owner = s_owner;
        System.out.println("PlayerBustBlockEntity owner set to: " + owner);
        markDirty();
        // Sync to client immediately
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
            System.out.println("Updated listeners for client sync");
        }
    }

    public String getOwner() {
        return owner;
    }

    // For client-server synchronization
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        if (owner != null) {
            nbt.putString("Owner", owner);
            System.out.println("toInitialChunkDataNbt - Adding owner to NBT: " + owner);
        } else {
            System.out.println("toInitialChunkDataNbt - Owner is null, not adding to NBT");
        }
        return nbt;
    }
}
