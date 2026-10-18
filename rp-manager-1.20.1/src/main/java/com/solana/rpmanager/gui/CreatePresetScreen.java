package com.solana.rpmanager.gui;

import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.mixin.ResourcePackOrganizerAccessor;
import com.solana.rpmanager.model.Preset;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CreatePresetScreen extends Screen {
    private final PackScreen parent;
    private final ResourcePackOrganizer organizer;
    private TextFieldWidget nameField;
    private ButtonWidget saveButton;
    private final List<String> packsToSave = new ArrayList<>();

    public CreatePresetScreen(PackScreen parent, ResourcePackOrganizer organizer) {
        super(Text.literal("Управление заготовками ресурспаков"));
        this.parent = parent;
        this.organizer = organizer;

        ResourcePackOrganizerAccessor accessor = (ResourcePackOrganizerAccessor) organizer;
        for (ResourcePackProfile profile : accessor.getEnabledPacks()) {
            packsToSave.add(profile.getName());
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = 40;

        this.nameField = new TextFieldWidget(
                this.textRenderer,
                centerX - 150,
                topY,
                210,
                20,
                Text.literal("Имя заготовки")
        );
        this.nameField.setMaxLength(40);
        this.nameField.setChangedListener(text -> updateSaveButtonState());
        this.addDrawableChild(this.nameField);
        this.setInitialFocus(this.nameField);

        this.saveButton = ButtonWidget.builder(Text.literal("Сохранить"), button -> save())
                .dimensions(centerX + 65, topY, 85, 20)
                .build();
        this.addDrawableChild(this.saveButton);

        // List existing presets with delete buttons
        List<Preset> presets = PresetManager.getInstance().getPresets();
        int listStartY = topY + 45;
        int maxVisible = Math.min(presets.size(), 6);

        for (int i = 0; i < maxVisible; i++) {
            Preset preset = presets.get(i);
            int rowY = listStartY + i * 24;

            ButtonWidget deleteBtn = ButtonWidget.builder(
                    Text.literal("✕").formatted(Formatting.RED),
                    button -> {
                        PresetManager.getInstance().deletePreset(preset);
                        organizer.refresh();
                        this.clearAndInit();
                    }
            ).dimensions(centerX + 125, rowY, 24, 20).build();
            this.addDrawableChild(deleteBtn);
        }

        // Cancel / Back button at the bottom
        ButtonWidget backButton = ButtonWidget.builder(ScreenTexts.BACK, button -> close())
                .dimensions(centerX - 75, this.height - 30, 150, 20)
                .build();
        this.addDrawableChild(backButton);

        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        if (this.saveButton != null) {
            String text = this.nameField.getText().trim();
            this.saveButton.active = !text.isEmpty();
        }
    }

    private void save() {
        String name = this.nameField.getText().trim();
        if (!name.isEmpty()) {
            PresetManager.getInstance().savePreset(name, packsToSave);
            organizer.refresh();
            close();
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = this.width / 2;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title.copy().formatted(Formatting.GOLD, Formatting.BOLD),
                centerX,
                12,
                0xFFFFFF
        );

        Text info = Text.literal("Активных паков для новой заготовки: " + packsToSave.size())
                .formatted(Formatting.GRAY);
        context.drawCenteredTextWithShadow(this.textRenderer, info, centerX, 26, 0xAAAAAA);

        List<Preset> presets = PresetManager.getInstance().getPresets();
        int listStartY = 85;
        if (!presets.isEmpty()) {
            context.drawTextWithShadow(
                    this.textRenderer,
                    Text.literal("Сохранённые заготовки:").formatted(Formatting.YELLOW),
                    centerX - 150,
                    listStartY - 12,
                    0xFFFFFF
            );

            int maxVisible = Math.min(presets.size(), 6);
            for (int i = 0; i < maxVisible; i++) {
                Preset preset = presets.get(i);
                int rowY = listStartY + i * 24;

                context.fill(centerX - 150, rowY, centerX + 120, rowY + 20, 0x66000000);

                context.drawTextWithShadow(
                        this.textRenderer,
                        Text.literal("✦ " + preset.getName()).formatted(Formatting.WHITE),
                        centerX - 144,
                        rowY + 6,
                        0xFFFFFF
                );

                int count = preset.getPacks() != null ? preset.getPacks().size() : 0;
                context.drawTextWithShadow(
                        this.textRenderer,
                        Text.literal(count + " пак.").formatted(Formatting.AQUA),
                        centerX + 70,
                        rowY + 6,
                        0xAAAAAA
                );
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
