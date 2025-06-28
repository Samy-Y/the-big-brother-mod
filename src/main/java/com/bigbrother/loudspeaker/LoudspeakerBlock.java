package com.bigbrother.loudspeaker;

import com.bigbrother.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LoudspeakerBlock extends BlockWithEntity {
    public LoudspeakerBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(LoudspeakerBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        LoudspeakerBlockEntity lsBe = new LoudspeakerBlockEntity(pos, state);
        return lsBe;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (placer instanceof PlayerEntity player) {
            if (world.getBlockEntity(pos) instanceof LoudspeakerBlockEntity lsBe) {
                lsBe.setOwner(player.getName().getString());
            }
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        if (type == ModBlockEntities.LOUDSPEAKER) {
            return (BlockEntityTicker<T>) (w, pos, st, be) -> {
                ((LoudspeakerBlockEntity) be).tick(w, pos, st, (LoudspeakerBlockEntity) be);
            };
        }
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof LoudspeakerBlockEntity loudspeakerBlockEntity)) {
            return super.onUse(state, world, pos, player, hit);
        }
        // I know the Loudspeaker[...] and loudspeaker[...] var names are very confusing lol
        player.sendMessage(Text.literal("This loudspeaker is diffusing propaganda for "+ loudspeakerBlockEntity.getOwner()), true);

        return ActionResult.SUCCESS;
    }

}
