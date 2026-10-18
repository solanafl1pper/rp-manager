package com.solana.rpmanager.mixin;

import com.solana.rpmanager.gui.CreatePresetScreen;
import com.solana.rpmanager.manager.PresetManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Shadow @Final private ResourcePackOrganizer organizer;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void rpmanager$addButtons(CallbackInfo ci) {
        int centerX = this.width / 2;
        int buttonY = 22;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("✦ Presets").formatted(Formatting.GOLD),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new CreatePresetScreen((PackScreen)(Object)this, this.organizer));
                    }
                }
        ).dimensions(centerX - 205, buttonY, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("📁 Folder").formatted(Formatting.AQUA),
                button -> Util.getOperatingSystem().open(PresetManager.getInstance().getPresetsDir().toFile())
        ).dimensions(centerX + 110, buttonY, 95, 20).build());
    }
}
