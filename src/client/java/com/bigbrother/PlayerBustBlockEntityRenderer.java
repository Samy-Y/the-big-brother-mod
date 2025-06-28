package com.bigbrother;

import com.bigbrother.bust.PlayerBustBlock;
import com.bigbrother.bust.PlayerBustBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class PlayerBustBlockEntityRenderer implements BlockEntityRenderer<PlayerBustBlockEntity> {

    private final PlayerEntityModel model;

    public PlayerBustBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        ModelPart root = ctx.getLayerModelPart(EntityModelLayers.PLAYER);
        this.model = new PlayerEntityModel(root, false);
        //System.out.println("PlayerBustBlockEntityRenderer created successfully!");
    }

    @Override
    public void render(PlayerBustBlockEntity be,
                       float tickDelta,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       int overlay,
                       Vec3d cameraPos) {

        //System.out.println("PlayerBustBlockEntityRenderer.render() called!");

        matrices.push();

        // Always render a bright green test cube above the block
        matrices.translate(0.5, 1.2, 0.5);
        matrices.scale(0.1f, 0.1f, 0.1f);

        // Render bright green cube
        matrices.pop();

        // Try to render the player model
        String owner = be.getOwner();
        //System.out.println("Owner: " + owner);

        if (owner != null && !owner.isEmpty()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() != null) {
                PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(owner);
                if (entry != null && entry.getSkinTextures() != null && entry.getSkinTextures().texture() != null) {

                    Identifier skin = entry.getSkinTextures().texture();
                    // Making the texture GRAYSCALE (i wanted to create a stone texture but i'm too lazy)


                    //System.out.println("Found skin: " + skin);

                    matrices.push();
                    matrices.translate(0.5, 0.75, 0.5);

                    // Get facing direction
                    Direction facing = be.getCachedState().get(PlayerBustBlock.FACING);
                    matrices.multiply(facing.getRotationQuaternion());

                    // Rotate the model 90 degrees around X-axis to make it stand upright
                    matrices.multiply(new org.joml.Quaternionf().rotationX((float) Math.toRadians(+90)));

                    matrices.scale(0.75f, 0.75f, 0.75f);

                    // Set up model visibility
                    model.head.visible = true;
                    model.hat.visible = true;
                    model.body.visible = true;
                    model.leftArm.visible = false;
                    model.rightArm.visible = false;
                    model.leftLeg.visible = false;
                    model.rightLeg.visible = false;

                    try {
                        // Get proper lighting from the world
                        int worldLight = be.getWorld() != null ? be.getWorld().getLightLevel(be.getPos().up()) : 15;
                        int finalLight = Math.max(light, worldLight << 4 | worldLight << 20);

                        // Simple approach - just render the model normally for now
                        // TODO: Implement grayscale effect when we find the correct API
                        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(skin));
                        model.render(matrices, consumer, finalLight, overlay);

                        //System.out.println("Successfully rendered player model!");
                    } catch (Exception e) {
                        System.err.println("Error rendering player model: " + e.getMessage());
                    }

                    matrices.pop();
                } else {
                    System.out.println("Could not find player skin data");
                }
            } else {
                System.out.println("Network handler is null");
            }
        } else {
            System.out.println("Owner is null or empty");
        }
    }

    @Override
    public boolean isInRenderDistance(PlayerBustBlockEntity be, Vec3d cameraPos) {
        return true;
    }
}
