package com.bigbrother.camera;

import com.bigbrother.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SecurityCameraBlockEntity extends BlockEntity {
    private String owner;
    private boolean powered;
    private final Set<UUID> playersInside = new HashSet<>();

    public SecurityCameraBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SECURITY_CAMERA, pos, state);
    }

    public void setOwner(String owner) {
        this.owner = owner;
        markDirty();
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
        markDirty();
    }

    /**
     * Called from our WorldMixin on every block change.
     */
    public void onMonitoredBlockUpdate(BlockPos changedPos, BlockState newState) {
        if (!powered || owner == null) return;
        ServerWorld world = (ServerWorld) this.world;
        ServerPlayerEntity player = world.getServer()
                .getPlayerManager()
                .getPlayer(owner);
        if (player == null) return;

        Text msg = Text.literal(
                "Block at " + changedPos + " is now " + newState.getBlock().getName().getString()
        );
        // inside onMonitoredBlockUpdate(...)
        player.sendMessage(Text.literal(
                "Security camera at " + this.pos +
                        ": Block at " + changedPos + " → " + newState.getBlock().getName().getString()
        ), false);

        String MOD_ID = "the-big-brother-mod";
        Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
        LOGGER.info("Security camera at {}: {}", this.getPos(), msg.getString());
    }

    /**
     * Periodic tick (registered via FabricBlockEntityTypeBuilder.ticker)
     * to detect players entering/exiting the 30-block cone.
     */
    public static void tick(World _world, BlockPos _pos, BlockState _state, SecurityCameraBlockEntity cam) {
        if (_world.isClient || !cam.powered || cam.owner == null) return;
        ServerWorld world = (ServerWorld) _world;
        // compute a 30-block box in front of the lens
        var dir = cam.getCachedState().get(SecurityCameraBlock.FACING);
        var center = cam.getPos().toCenterPos().add(dir.getOffsetX()*2, 0, dir.getOffsetZ()*2);
        var box = new Box(center, center).expand(30);

        Set<UUID> nowInside = new HashSet<>();
        for (ServerPlayerEntity p : world.getPlayers(pl -> box.contains(pl.getPos()))) {
            nowInside.add(p.getUuid());
            if (cam.playersInside.add(p.getUuid())) {
                p.sendMessage(Text.literal("Entered camera area"), true);
            }
        }
        // players who left
        for (UUID old : new HashSet<>(cam.playersInside)) {
            if (!nowInside.contains(old)) {
                var p = world.getServer().getPlayerManager().getPlayer(old);
                if (p != null) p.sendMessage(Text.literal("Left camera area"), true);
                cam.playersInside.remove(old);
            }
        }
    }

    // ——— Persistence using ReadView / WriteView ———

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (owner != null) view.putString("owner", owner);
        view.putBoolean("powered", powered);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.owner  = view.getString("owner","null");
        this.powered = view.getBoolean("powered", false);
    }
}
