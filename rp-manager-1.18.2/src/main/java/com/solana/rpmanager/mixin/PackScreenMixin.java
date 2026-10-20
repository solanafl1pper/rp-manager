package com.solana.rpmanager.mixin;

import com.solana.rpmanager.gui.CreatePresetScreen;
import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.util.Texts;
import net.minecraft.client.gui.Element;
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
    @Shadow private ButtonWidget doneButton;

    private ButtonWidget rpmanager$presetsButton;
    private ButtonWidget rpmanager$folderButton;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void rpmanager$addButtons(CallbackInfo ci) {
        boolean isRu = false;
        try {
            if (this.client != null && this.client.getLanguageManager() != null) {
                net.minecraft.client.resource.language.LanguageDefinition langDef = this.client.getLanguageManager().getLanguage();
                if (langDef != null && langDef.getCode() != null && langDef.getCode().toLowerCase().startsWith("ru")) {
                    isRu = true;
                }
            }
        } catch (Throwable ignored) {}

        Text presetsTooltip = Texts.literal(isRu ? "Пресеты" : "Presets");
        Text folderTooltip = Texts.literal(isRu ? "Папка с пресетами" : "Presets Folder");

        this.rpmanager$presetsButton = new ButtonWidget(
                0, 0, 20, 20,
                Texts.literal("✦").formatted(Formatting.GOLD),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new CreatePresetScreen((PackScreen)(Object)this, this.organizer));
                    }
                },
                (button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, presetsTooltip, mouseX, mouseY)
        );

        this.rpmanager$folderButton = new ButtonWidget(
                0, 0, 20, 20,
                Texts.literal("📁").formatted(Formatting.AQUA),
                button -> Util.getOperatingSystem().open(PresetManager.getInstance().getPresetsDir().toFile()),
                (button, matrices, mouseX, mouseY) -> this.renderTooltip(matrices, folderTooltip, mouseX, mouseY)
        );

        this.addDrawableChild(this.rpmanager$presetsButton);
        this.addDrawableChild(this.rpmanager$folderButton);

        rpmanager$updateButtonPositions();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void rpmanager$onTick(CallbackInfo ci) {
        rpmanager$updateButtonPositions();
    }

    private void rpmanager$updateButtonPositions() {
        if (this.rpmanager$presetsButton == null || this.rpmanager$folderButton == null) return;

        ButtonWidget openFolderButton = null;

        if (this.doneButton != null) {
            int doneY = this.doneButton.y;
            for (Element child : this.children()) {
                if (child instanceof ButtonWidget button && button != this.doneButton) {
                    if (button != this.rpmanager$presetsButton && button != this.rpmanager$folderButton) {
                        // Find the button on the same horizontal row to the left of doneButton
                        if (Math.abs(button.y - doneY) <= 4 && button.x < this.doneButton.x) {
                            if (openFolderButton == null || button.x > openFolderButton.x) {
                                openFolderButton = button;
                            }
                        }
                    }
                }
            }
        }

        int btnY;
        int openFolderX;

        if (openFolderButton != null) {
            btnY = openFolderButton.y;
            openFolderX = openFolderButton.x;
        } else if (this.doneButton != null) {
            btnY = this.doneButton.y;
            openFolderX = this.doneButton.x - 8 - 150;
        } else {
            btnY = this.height - 48;
            openFolderX = (this.width / 2) - 154;
        }

        int presetsX;
        int folderX;

        if (openFolderX >= 48) {
            presetsX = openFolderX - 48;
            folderX = openFolderX - 24;
        } else if (this.doneButton != null && (this.doneButton.x + this.doneButton.getWidth() + 48) <= this.width) {
            int doneRight = this.doneButton.x + this.doneButton.getWidth();
            presetsX = doneRight + 4;
            folderX = doneRight + 28;
        } else {
            presetsX = openFolderX - 24;
            folderX = openFolderX - 48;
        }

        this.rpmanager$presetsButton.x = presetsX;
        this.rpmanager$presetsButton.y = btnY;
        this.rpmanager$folderButton.x = folderX;
        this.rpmanager$folderButton.y = btnY;
    }
}
