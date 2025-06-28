package com.bigbrother.bust;

import com.bigbrother.TheBigBrotherMod;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;


import java.util.Optional;

public class PlayerBustBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    // Full‐block shape
    private static final VoxelShape SHAPE = Block.createCuboidShape(0,0,0,16,16,16);

    public PlayerBustBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block,BlockState> b) {
        b.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        System.out.println("PlayerBustBlock.createBlockEntity() called at position: " + pos);
        PlayerBustBlockEntity entity = new PlayerBustBlockEntity(pos, state);
        System.out.println("Created PlayerBustBlockEntity: " + entity);
        return entity;
    }

    /** When placed, record the owner's username in NBT **/
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        System.out.println("PlayerBustBlock.onPlaced() called with placer: " + (placer != null ? placer.getName().getString() : "null"));

        if (!world.isClient && placer instanceof ServerPlayerEntity sp) {
            System.out.println("Setting owner to: " + sp.getGameProfile().getName());
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PlayerBustBlockEntity bust) {
                bust.setOwner(sp.getGameProfile().getName());
                System.out.println("Owner set successfully!");
            } else {
                System.out.println("Block entity is not PlayerBustBlockEntity: " + (be != null ? be.getClass().getName() : "null"));
            }
        } else {
            System.out.println("Conditions not met - world.isClient: " + world.isClient + ", placer type: " + (placer != null ? placer.getClass().getName() : "null"));
        }
    }

}
