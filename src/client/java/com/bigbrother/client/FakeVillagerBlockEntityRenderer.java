package com.bigbrother.client;

import com.bigbrother.fakevillager.FakeVillagerBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class FakeVillagerBlockEntityRenderer implements BlockEntityRenderer<FakeVillagerBlockEntity> {
    private final VillagerResemblingModel model;

    // Villager textures for different biomes
    private static final Identifier PLAINS_TEXTURE = Identifier.ofVanilla("textures/entity/villager/villager.png");
    private static final Identifier DESERT_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/desert.png");
    private static final Identifier JUNGLE_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/jungle.png");
    private static final Identifier SAVANNA_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/savanna.png");
    private static final Identifier SNOW_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/snow.png");
    private static final Identifier SWAMP_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/swamp.png");
    private static final Identifier TAIGA_TEXTURE = Identifier.ofVanilla("textures/entity/villager/type/taiga.png");

    public FakeVillagerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new VillagerResemblingModel(context.getLayerModelPart(EntityModelLayers.VILLAGER));
    }

    @Override
    public void render(FakeVillagerBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                      VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {

        matrices.push();

        // Center the model in the block
        matrices.translate(0.5, 0.0, 0.5);

        // Flip the model upright (villager models are rendered upside down by default)
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
        matrices.translate(0, -1.35f, 0); // Adjust position after flipping

        // Rotate based on facing direction
        Direction facing = blockEntity.getCachedState().get(Properties.HORIZONTAL_FACING);
        switch (facing) {
            case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));
            case EAST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            case WEST -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
        }

        // Scale down slightly to fit in block
        matrices.scale(0.9f, 0.9f, 0.9f);

        // Get appropriate texture based on biome
        Identifier texture = getTextureForBiome(blockEntity.getBiomeType());

        // Render the villager model using the correct render layer
        var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        model.render(matrices, vertexConsumer, light, overlay, -1);

        matrices.pop();
    }

    private Identifier getTextureForBiome(String biomeType) {
        return switch (biomeType) {
            case "desert" -> DESERT_TEXTURE;
            case "jungle" -> JUNGLE_TEXTURE;
            case "savanna" -> SAVANNA_TEXTURE;
            case "snow" -> SNOW_TEXTURE;
            case "swamp" -> SWAMP_TEXTURE;
            case "taiga" -> TAIGA_TEXTURE;
            default -> PLAINS_TEXTURE;
        };
    }
}
