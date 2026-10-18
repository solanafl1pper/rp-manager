package com.solana.rpmanager.gui;

import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.mixin.ResourcePackOrganizerAccessor;
import com.solana.rpmanager.model.Preset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class CreatePresetScreen extends Screen {
    private static final int ITEMS_PER_PAGE = 4;

    private final PackScreen parent;
    private final ResourcePackOrganizer organizer;
    private TextFieldWidget quickNameField;
    private ButtonWidget quickSaveButton;
    private final List<String> activePackIds = new ArrayList<>();
    private int page = 0;

    public CreatePresetScreen(PackScreen parent, ResourcePackOrganizer organizer) {
        super(Text.literal("Resource Pack Presets"));
        this.parent = parent;
        this.organizer = organizer;
        updateActivePacks();
    }

    private void updateActivePacks() {
        activePackIds.clear();
        ResourcePackOrganizerAccessor accessor = (ResourcePackOrganizerAccessor) organizer;
        for (ResourcePackProfile profile : accessor.getEnabledPacks()) {
            activePackIds.add(profile.getId());
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Top Row: [+ New Preset (Pick Packs)] on left, [Quick Save Active] on right
        ButtonWidget newPresetBtn = ButtonWidget.builder(
                Text.literal("+ New Preset (Pick Packs)").formatted(Formatting.GOLD),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(new PresetEditorScreen(this, this.organizer, null));
                    }
                }
        ).dimensions(centerX - 170, 36, 160, 20).build();
        this.addDrawableChild(newPresetBtn);

        this.quickNameField = new TextFieldWidget(
                this.textRenderer,
                centerX + 2,
                36,
                95,
                20,
                Text.literal("Quick Name")
        );
        this.quickNameField.setMaxLength(30);
        this.quickNameField.setPlaceholder(Text.literal("Quick name...").formatted(Formatting.DARK_GRAY));
        this.quickNameField.setChangedListener(text -> updateQuickSaveState());
        this.addDrawableChild(this.quickNameField);

        this.quickSaveButton = ButtonWidget.builder(
                Text.literal("+ Save (" + activePackIds.size() + ")").formatted(Formatting.GREEN),
                button -> quickSave()
        ).dimensions(centerX + 101, 36, 69, 20).build();
        this.addDrawableChild(this.quickSaveButton);

        // Presets list
        List<Preset> presets = PresetManager.getInstance().getPresets();
        int totalPages = Math.max(1, (int) Math.ceil((double) presets.size() / ITEMS_PER_PAGE));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, presets.size());
        int listStartY = 84;

        for (int i = startIndex; i < endIndex; i++) {
            Preset preset = presets.get(i);
            int slot = i - startIndex;
            int rowY = listStartY + slot * 34;

            // Apply Button [Apply]
            ButtonWidget applyBtn = ButtonWidget.builder(
                    Text.literal("Apply").formatted(Formatting.GREEN),
                    button -> applyPreset(preset)
            ).dimensions(centerX + 58, rowY + 5, 44, 20).build();
            this.addDrawableChild(applyBtn);

            // Edit Button [✎]
            ButtonWidget editBtn = ButtonWidget.builder(
                    Text.literal("✎").formatted(Formatting.YELLOW),
                    button -> {
                        if (this.client != null) {
                            this.client.setScreen(new PresetEditorScreen(this, this.organizer, preset));
                        }
                    }
            ).dimensions(centerX + 106, rowY + 5, 24, 20).build();
            this.addDrawableChild(editBtn);

            // Delete Button [✕]
            ButtonWidget deleteBtn = ButtonWidget.builder(
                    Text.literal("✕").formatted(Formatting.RED),
                    button -> {
                        PresetManager.getInstance().deletePreset(preset);
                        organizer.refresh();
                        this.clearAndInit();
                    }
            ).dimensions(centerX + 134, rowY + 5, 24, 20).build();
            this.addDrawableChild(deleteBtn);
        }

        // Pagination buttons
        if (totalPages > 1) {
            int navY = listStartY + ITEMS_PER_PAGE * 34 + 2;
            ButtonWidget prevBtn = ButtonWidget.builder(Text.literal("◀"), button -> {
                if (page > 0) {
                    page--;
                    this.clearAndInit();
                }
            }).dimensions(centerX - 60, navY, 24, 18).build();
            prevBtn.active = page > 0;
            this.addDrawableChild(prevBtn);

            ButtonWidget nextBtn = ButtonWidget.builder(Text.literal("▶"), button -> {
                if (page < totalPages - 1) {
                    page++;
                    this.clearAndInit();
                }
            }).dimensions(centerX + 36, navY, 24, 18).build();
            nextBtn.active = page < totalPages - 1;
            this.addDrawableChild(nextBtn);
        }

        // Bottom Bar: [Open Folder] and [Done]
        ButtonWidget folderButton = ButtonWidget.builder(
                Text.literal("📁 Presets Folder").formatted(Formatting.AQUA),
                button -> Util.getOperatingSystem().open(PresetManager.getInstance().getPresetsDir().toFile())
        ).dimensions(centerX - 154, this.height - 28, 150, 20).build();
        this.addDrawableChild(folderButton);

        ButtonWidget backButton = ButtonWidget.builder(
                ScreenTexts.DONE,
                button -> close()
        ).dimensions(centerX + 4, this.height - 28, 150, 20).build();
        this.addDrawableChild(backButton);

        updateQuickSaveState();
    }

    private void updateQuickSaveState() {
        if (this.quickSaveButton != null) {
            String text = this.quickNameField.getText().trim();
            this.quickSaveButton.active = !text.isEmpty();
        }
    }

    private void quickSave() {
        String name = this.quickNameField.getText().trim();
        if (!name.isEmpty()) {
            PresetManager.getInstance().savePreset(name, activePackIds);
            organizer.refresh();
            this.quickNameField.setText("");
            this.clearAndInit();
        }
    }

    private void applyPreset(Preset preset) {
        new PresetPack(organizer, preset).applyPreset();
        updateActivePacks();
        this.clearAndInit();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
            if (this.parent != null) {
                this.parent.init(this.client, this.parent.width, this.parent.height);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        int centerX = this.width / 2;

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title.copy().formatted(Formatting.GOLD, Formatting.BOLD),
                centerX,
                10,
                0xFFFFFF
        );

        // Section header for existing presets
        List<Preset> presets = PresetManager.getInstance().getPresets();
        int totalPages = Math.max(1, (int) Math.ceil((double) presets.size() / ITEMS_PER_PAGE));
        int listStartY = 84;

        drawLeftAlignedText(
                context,
                Text.literal("Saved Presets (" + presets.size() + "):").formatted(Formatting.YELLOW, Formatting.BOLD),
                centerX - 168,
                listStartY - 14,
                0xFFFFFF
        );

        if (presets.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("No presets saved yet. Click \"+ New Preset\" above!").formatted(Formatting.GRAY),
                    centerX,
                    listStartY + 40,
                    0x888888
            );
        } else {
            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, presets.size());

            for (int i = startIndex; i < endIndex; i++) {
                Preset preset = presets.get(i);
                int slot = i - startIndex;
                int rowY = listStartY + slot * 34;

                // Card background
                boolean isHovered = mouseX >= centerX - 170 && mouseX <= centerX + 170 && mouseY >= rowY && mouseY <= rowY + 30;
                int bgColor = isHovered ? 0x77333333 : 0x55000000;
                context.fill(centerX - 170, rowY, centerX + 170, rowY + 30, bgColor);

                // Preset Name + Count
                int count = preset.getPacks() != null ? preset.getPacks().size() : 0;
                Text nameText = Text.literal("✦ " + preset.getName()).formatted(Formatting.WHITE, Formatting.BOLD)
                        .append(Text.literal(" (" + count + " packs)").formatted(Formatting.AQUA));
                drawLeftAlignedText(context, nameText, centerX - 164, rowY + 4, 0xFFFFFF);

                // Packs Preview List
                String packsPreview = getPacksPreview(preset);
                drawLeftAlignedText(
                        context,
                        Text.literal(packsPreview).formatted(Formatting.GRAY),
                        centerX - 164,
                        rowY + 16,
                        0x888888
                );
            }

            // Pagination indicator
            if (totalPages > 1) {
                int navY = listStartY + ITEMS_PER_PAGE * 34 + 2;
                String pageText = (page + 1) + " / " + totalPages;
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(pageText).formatted(Formatting.WHITE), centerX - 12, navY + 4, 0xFFFFFF);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private String getPacksPreview(Preset preset) {
        if (preset.getPacks() == null || preset.getPacks().isEmpty()) {
            return "No packs included";
        }
        List<String> cleaned = new ArrayList<>();
        for (String id : preset.getPacks()) {
            String name = id;
            if (name.startsWith("file/")) name = name.substring(5);
            if (name.endsWith(".zip")) name = name.substring(0, name.length() - 4);
            cleaned.add(name);
        }
        String joined = String.join(", ", cleaned);
        if (joined.length() > 36) {
            joined = joined.substring(0, 33) + "...";
        }
        return joined;
    }

    private void drawLeftAlignedText(DrawContext context, Text text, int x, int y, int color) {
        int width = this.textRenderer.getWidth(text);
        context.drawCenteredTextWithShadow(this.textRenderer, text, x + width / 2, y, color);
    }
}
