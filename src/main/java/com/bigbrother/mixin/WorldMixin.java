package com.bigbrother.mixin;

import com.bigbrother.camera.SecurityCameraBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Inject(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            at = @At("TAIL")
    )
    private void onAnyBlockChange(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        // only if the block actually changed
        if (!cir.getReturnValue()) return;

        // explicit Object → ServerWorld cast avoids “inconvertible types” at compile time
        Object self = this;
        if (!(self instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) self;

        for (BlockPos checkPos : BlockPos.iterateOutwards(pos, 30, 30, 30)) {
            if (world.getBlockEntity(checkPos) instanceof SecurityCameraBlockEntity cam) {
                cam.onMonitoredBlockUpdate(pos, newState);
            }
        }
    }
}
