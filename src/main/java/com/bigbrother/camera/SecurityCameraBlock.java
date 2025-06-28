package com.bigbrother.camera;

import com.bigbrother.TheBigBrotherMod;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityCameraBlock extends Block implements BlockEntityProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheBigBrotherMod.MOD_ID);
    public static final EnumProperty<Direction> FACING  = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty      POWERED = Properties.POWERED;

    private static final VoxelShape SHAPE_N = Block.createCuboidShape(6, 6, 8, 10, 10, 16);  // North: z+
    private static final VoxelShape SHAPE_S = Block.createCuboidShape(6, 6, 0, 10, 10, 8);   // South: z-
    private static final VoxelShape SHAPE_W = Block.createCuboidShape(8, 6, 6, 16, 10, 10);  // West: x+
    private static final VoxelShape SHAPE_E = Block.createCuboidShape(0, 6, 6, 8, 10, 10);   // East: x-

    public SecurityCameraBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(POWERED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block,BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (placer instanceof PlayerEntity player) {
            if (world.getBlockEntity(pos) instanceof SecurityCameraBlockEntity scBe) {
                scBe.setOwner(player.getName().getString());
            }
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Only horizontal directions—never UP or DOWN
        Direction horiz = ctx.getHorizontalPlayerFacing().getOpposite();
        return getDefaultState()
                .with(FACING, horiz)
                .with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos,
                                  Block sourceBlock, @Nullable WireOrientation fromOrientation, boolean notify) {
        LOGGER.info("[CameraBlock] neighborUpdate at {} (from {}) client={}", pos, world.isClient());
        if (world.isClient) return;
        boolean nowPowered = world.isReceivingRedstonePower(pos);
        LOGGER.info("[CameraBlock] isReceivingRedstonePower at {} = {}", pos, nowPowered);
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SecurityCameraBlockEntity)) {
            LOGGER.warn("[CameraBlock] BlockEntity at {} is {}, not CameraBE", pos, be);
        } else {
            ((SecurityCameraBlockEntity)be).setPowered(nowPowered);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view,
                                      BlockPos pos, ShapeContext ctx) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_N;
            case SOUTH -> SHAPE_S;
            case WEST  -> SHAPE_W;
            case EAST  -> SHAPE_E;
            default    -> SHAPE_N;
        };
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SecurityCameraBlockEntity(pos, state);
    }
}
