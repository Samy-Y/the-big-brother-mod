package com.bigbrother.mixin.client;

import com.bigbrother.entity.MilitiaVillagerEntity;
import com.bigbrother.loyalty.LoyaltyManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderLoyaltyOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client.player == null || client.world == null) return;

        // Check if player is looking at a villager
        HitResult hitResult = client.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity targetEntity = entityHit.getEntity();

            if (targetEntity instanceof VillagerEntity villager && !(targetEntity instanceof MilitiaVillagerEntity)) {
                // Check if villager is within reach (8 blocks)
                Vec3d playerPos = client.player.getEyePos();
                Vec3d villagerPos = villager.getPos();
                double distance = playerPos.distanceTo(villagerPos);

                if (distance <= 8.0) {
                    // Get loyalty percentage for this player
                    int loyalty = LoyaltyManager.getLoyalty(villager, client.player.getUuid());

                    // Determine color based on loyalty level
                    int color = loyalty < 50 ? 0xFFFF5555 : 0xFF55FF55; // Red if <50, Green if >=50

                    // Create the loyalty text
                    String loyaltyText = "Loyalty: " + loyalty + "%";
                    Text text = Text.literal(loyaltyText);

                    // Get text renderer and calculate position
                    TextRenderer textRenderer = client.textRenderer;
                    int textWidth = textRenderer.getWidth(text);
                    int screenWidth = context.getScaledWindowWidth();
                    int screenHeight = context.getScaledWindowHeight();

                    // Position the text slightly above the center of the screen
                    int x = (screenWidth - textWidth) / 2;
                    int y = (screenHeight / 2) - 20;

                    // Draw background for better readability
                    context.fill(x - 2, y - 2, x + textWidth + 2, y + textRenderer.fontHeight + 2, 0x80000000);

                    // Draw the loyalty text
                    context.drawText(textRenderer, text, x, y, color, false);
                }
            } else if (targetEntity instanceof MilitiaVillagerEntity militiaVillagerEntity) {
                // Check if villager is within reach (8 blocks)
                Vec3d playerPos = client.player.getEyePos();
                Vec3d militiaVillagerPos = militiaVillagerEntity.getPos();
                double distance = playerPos.distanceTo(militiaVillagerPos);

                if (distance <= 8.0) {
                    // Get loyalty percentage for this player
                    int loyalty = LoyaltyManager.getLoyalty(militiaVillagerEntity, client.player.getUuid());

                    // Determine color based on loyalty level
                    int color = loyalty < 50 ? 0xFFFF5555 : 0xFF55FF55; // Red if <50, Green if >=50

                    // Create the loyalty text
                    String loyaltyText = "Loyalty: " + loyalty + "%";
                    String ownerText = "Owner: " + militiaVillagerEntity.getOwner();
                    Text text1 = Text.literal(loyaltyText);
                    Text text2 = Text.literal(ownerText);
                    TextRenderer textRenderer = client.textRenderer;
                    // TEXT1 SPECIFIC
                    // Get text renderer and calculate position
                    int text1Width = textRenderer.getWidth(text1);
                    int screenWidth = context.getScaledWindowWidth();
                    int screenHeight = context.getScaledWindowHeight();

                    int x1 = (screenWidth - text1Width) / 2;
                    int y1 = (screenHeight / 2) - 40;

                    // Draw background for better readability
                    context.fill(x1 - 2, y1 - 2, x1 + text1Width + 2, y1 + textRenderer.fontHeight + 2, 0x80000000);

                    // Draw the loyalty text
                    context.drawText(textRenderer, text1, x1, y1, color, false);

                    // TEXT2 SPECIFIC
                    // Get text renderer and calculate position
                    int text2Width = textRenderer.getWidth(text2);
                    int x2 = (screenWidth - text2Width) / 2;
                    int y2 = (screenHeight / 2) - 20;
                    // Draw background for better readability
                    context.fill(x2 - 2, y2 - 2, x2 + text2Width + 2, y2 + textRenderer.fontHeight + 2, 0x80000000);
                    // Draw the owner text
                    context.drawText(textRenderer, text2, x2, y2, color, false);
                }
            }
        }
    }
}