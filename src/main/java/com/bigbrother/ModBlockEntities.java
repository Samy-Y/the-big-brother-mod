package com.bigbrother;

import com.bigbrother.bust.PlayerBustBlockEntity;
import com.bigbrother.camera.SecurityCameraBlockEntity;
import com.bigbrother.loudspeaker.LoudspeakerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.bigbrother.TheBigBrotherMod.MOD_ID;

public class ModBlockEntities {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, path), blockEntityType);
    }

    public static final BlockEntityType<LoudspeakerBlockEntity> LOUDSPEAKER = register(
            "loudspeaker",
            FabricBlockEntityTypeBuilder.create(LoudspeakerBlockEntity::new, ModBlocks.LOUDSPEAKER).build()
    );

    public static void initialize() {
        // stays empty (again)
    }

    public static final BlockEntityType<SecurityCameraBlockEntity> SECURITY_CAMERA = register(
            "security_camera",
            FabricBlockEntityTypeBuilder.create(SecurityCameraBlockEntity::new, ModBlocks.SECURITY_CAMERA).build()
    );

    public static final BlockEntityType<PlayerBustBlockEntity> PLAYER_BUST = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID,"player_bust"),
            FabricBlockEntityTypeBuilder
                    .create(PlayerBustBlockEntity::new, ModBlocks.PLAYER_BUST)
                    .build()
    );

}
