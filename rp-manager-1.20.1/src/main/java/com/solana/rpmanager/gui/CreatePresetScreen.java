package com.solana.rpmanager.gui;

import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.mixin.PackScreenAccessor;
import com.solana.rpmanager.mixin.ResourcePackOrganizerAccessor;
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
        super(Text.literal("Создать заготовку ресурспаков"));
        this.parent = parent;
        this.organizer = organizer;

        // Gather currently active packs
        ResourcePackOrganizerAccessor accessor = (ResourcePackOrganizerAccessor) organizer;
        for (ResourcePackProfile profile : accessor.getEnabledPacks()) {
            packsToSave.add(profile.getName());
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new TextFieldWidget(
                this.textRenderer,
                centerX - 120,
                centerY - 20,
                240,
                20,
                Text.literal("Имя заготовки")
        );
        this.nameField.setMaxLength(50);
        this.nameField.setChangedListener(text -> updateSaveButtonState());
        this.addSelectableChild(this.nameField);
        this.setInitialFocus(this.nameField);

        this.saveButton = ButtonWidget.builder(Text.literal("Сохранить"), button -> save())
                .dimensions(centerX - 122, centerY + 20, 118, 20)
                .build();
        this.addDrawableChild(this.saveButton);

        ButtonWidget cancelButton = ButtonWidget.builder(ScreenTexts.CANCEL, button -> close())
                .dimensions(centerX + 4, centerY + 20, 118, 20)
                .build();
        this.addDrawableChild(cancelButton);

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
            ((PackScreenAccessor) parent).invokeUpdatePackLists();
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
        int centerY = this.height / 2;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title.copy().formatted(Formatting.GOLD, Formatting.BOLD),
                centerX,
                centerY - 60,
                0xFFFFFF
        );

        Text info = Text.literal("Будет сохранено активных ресурспаков: " + packsToSave.size())
                .formatted(Formatting.GRAY);
        context.drawCenteredTextWithShadow(this.textRenderer, info, centerX, centerY - 45, 0xAAAAAA);

        this.nameField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}
