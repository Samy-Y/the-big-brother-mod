package com.bigbrother;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("tutorial", path), blockEntityType);
    }

    public static final BlockEntityType<LoudspeakerBlockEntity> LOUDSPEAKER = register(
            "loudspeaker",
            FabricBlockEntityTypeBuilder.create(LoudspeakerBlockEntity::new, ModBlocks.LOUDSPEAKER).build()
    );

    public static void initialize() {
        // stays empty (again)
    }
}


