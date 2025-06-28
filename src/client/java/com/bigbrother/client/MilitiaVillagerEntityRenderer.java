package com.bigbrother.client;

import com.bigbrother.entity.MilitiaVillagerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public class MilitiaVillagerEntityRenderer extends VillagerEntityRenderer {
    private static final Identifier MILITIA_TEXTURE = Identifier.of("the-big-brother-mod", "textures/entity/militia_villager.png");

    public MilitiaVillagerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(VillagerEntityRenderState renderState) {
        // Always return our custom texture for militia villagers, ignoring biome overlays
        return MILITIA_TEXTURE;
    }

    @Override
    public void updateRenderState(VillagerEntity entity, VillagerEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        // Override the villager data to prevent biome-specific overlays
        // Force the villager to use PLAINS type (neutral) and NONE profession using registry entries
        if (entity instanceof MilitiaVillagerEntity) {
            state.villagerData = new VillagerData(
                Registries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS),
                Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE),
                1
            );
        }
    }
}
