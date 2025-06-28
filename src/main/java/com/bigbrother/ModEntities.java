package com.bigbrother;

import com.bigbrother.entity.MilitiaVillagerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModEntities {
    public static final RegistryKey<EntityType<?>> MILITIA_VILLAGER_KEY = RegistryKey.of(
            Registries.ENTITY_TYPE.getKey(),
            Identifier.of("the-big-brother-mod", "militia_villager")
    );

    public static final EntityType<MilitiaVillagerEntity> MILITIA_VILLAGER = Registry.register(
            Registries.ENTITY_TYPE,
            MILITIA_VILLAGER_KEY.getValue(),
            EntityType.Builder.create(MilitiaVillagerEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6F, 1.95F)
                    .build(MILITIA_VILLAGER_KEY)
    );

    public static void initialize(){
        // Register custom attributes for militia villager
        FabricDefaultAttributeRegistry.register(MILITIA_VILLAGER, MilitiaVillagerEntity.createMilitiaVillagerAttributes());
    }
}
