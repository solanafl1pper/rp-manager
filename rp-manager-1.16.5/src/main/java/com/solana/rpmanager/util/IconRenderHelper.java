package com.solana.rpmanager.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class IconRenderHelper {
    private IconRenderHelper() {}

    public static void drawPackIcon(MatrixStack matrices, Identifier iconId, int x, int y, int width, int height) {
        if (matrices == null || iconId == null) return;
        try {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getTextureManager() != null) {
                client.getTextureManager().bindTexture(iconId);
            }
            DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, width, height, width, height);
        } catch (Throwable ignored) {
            // Never crash UI rendering if texture draw fails
        }
    }
}
