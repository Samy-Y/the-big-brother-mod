package com.bigbrother;

import com.bigbrother.client.FakeVillagerBlockEntityRenderer;
import com.bigbrother.client.MilitiaVillagerEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TheBigBrotherModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register custom militia villager renderer with custom texture
		EntityRendererRegistry.register(
				ModEntities.MILITIA_VILLAGER,
				MilitiaVillagerEntityRenderer::new
		);
		BlockEntityRendererRegistry.register(
				ModBlockEntities.PLAYER_BUST,
				PlayerBustBlockEntityRenderer::new
		);
		BlockEntityRendererRegistry.register(
				ModBlockEntities.FAKE_VILLAGER,
				FakeVillagerBlockEntityRenderer::new
		);

	}
}