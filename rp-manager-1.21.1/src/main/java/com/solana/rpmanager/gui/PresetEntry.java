package com.solana.rpmanager.gui;

import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.mixin.PackScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PresetEntry extends PackListWidget.ResourcePackEntry {
    private final PackListWidget packListWidget;
    private final PresetPack presetPack;

    public PresetEntry(MinecraftClient client, PackListWidget widget, PresetPack pack) {
        super(client, widget, pack);
        this.packListWidget = widget;
        this.presetPack = pack;
    }

    public PresetPack getPresetPack() {
        return presetPack;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

        // Delete button [✕] in top-right of the entry
        int deleteX = x + entryWidth - 14;
        int deleteY = y + 3;
        boolean deleteHovered = mouseX >= deleteX - 3 && mouseX <= deleteX + 11 && mouseY >= deleteY - 3 && mouseY <= deleteY + 11;

        Text deleteText = Text.literal("✕").formatted(deleteHovered ? Formatting.RED : Formatting.DARK_RED);
        context.drawTextWithShadow(this.client.textRenderer, deleteText, deleteX, deleteY, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int rowLeft = this.packListWidget.getRowLeft();
            int rowWidth = this.packListWidget.getRowWidth();
            double relX = mouseX - rowLeft;

            // Check if clicked the delete button [✕] on the right side
            if (relX >= rowWidth - 22 && relX <= rowWidth) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.setScreen(new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) {
                                PresetManager.getInstance().deletePreset(presetPack.getPreset());
                                ((PackScreenAccessor) presetPack.getScreen()).invokeUpdatePackLists();
                            }
                            client.setScreen(presetPack.getScreen());
                        },
                        Text.literal("Удаление заготовки").formatted(Formatting.BOLD, Formatting.RED),
                        Text.literal("Удалить заготовку '")
                                .append(Text.literal(presetPack.getPreset().getName()).formatted(Formatting.YELLOW))
                                .append(Text.literal("'?"))
                ));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
