package com.solana.rpmanager.mixin;

import com.solana.rpmanager.gui.CreatePresetScreen;
import com.solana.rpmanager.manager.PresetManager;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
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
        // Find vanilla bottom buttons (Pack folder and Done)
        ClickableWidget openFolderWidget = null;
        ClickableWidget doneWidget = null;

        for (Element child : this.children()) {
            if (child instanceof ClickableWidget widget) {
                if (widget.getY() >= this.height - 56) {
                    if (widget.getX() < this.width / 2) {
                        if (openFolderWidget == null || widget.getY() > openFolderWidget.getY()) {
                            openFolderWidget = widget;
                        }
                    } else {
                        if (doneWidget == null || widget.getY() > doneWidget.getY()) {
                            doneWidget = widget;
                        }
                    }
                }
            }
        }

        int btnY = openFolderWidget != null ? openFolderWidget.getY() : (this.height - 48);
        int openFolderX = openFolderWidget != null ? openFolderWidget.getX() : (this.width / 2 - 154);

        int presetsX;
        int folderX;

        if (openFolderX >= 48) {
            presetsX = openFolderX - 48;
            folderX = openFolderX - 24;
        } else if (doneWidget != null && (doneWidget.getX() + doneWidget.getWidth() + 48) <= this.width) {
            int doneRight = doneWidget.getX() + doneWidget.getWidth();
            presetsX = doneRight + 4;
            folderX = doneRight + 28;
        } else {
            presetsX = this.width / 2 - 198;
            folderX = this.width / 2 - 174;
        }

        boolean isRu = false;
        try {
            if (this.client != null && this.client.getLanguageManager() != null) {
                String lang = this.client.getLanguageManager().getLanguage();
                if (lang != null && lang.toLowerCase().startsWith("ru")) {
                    isRu = true;
                }
            }
        } catch (Throwable ignored) {}

        Text presetsTooltip = Text.literal(isRu ? "Пресеты" : "Presets");
        Text folderTooltip = Text.literal(isRu ? "Папка с пресетами" : "Presets Folder");

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("✦").formatted(Formatting.GOLD),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new CreatePresetScreen((PackScreen)(Object)this, this.organizer));
                    }
                }
        ).dimensions(presetsX, btnY, 20, 20)
         .tooltip(Tooltip.of(presetsTooltip))
         .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("📁").formatted(Formatting.AQUA),
                button -> Util.getOperatingSystem().open(PresetManager.getInstance().getPresetsDir().toFile())
        ).dimensions(folderX, btnY, 20, 20)
         .tooltip(Tooltip.of(folderTooltip))
         .build());
    }
}
