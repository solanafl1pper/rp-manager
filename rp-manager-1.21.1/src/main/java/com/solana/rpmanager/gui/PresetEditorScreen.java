package com.solana.rpmanager.gui;

import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.mixin.ResourcePackOrganizerAccessor;
import com.solana.rpmanager.model.Preset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PresetEditorScreen extends Screen {
    private static final int PACKS_PER_PAGE = 5;

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GOLD = 0xFFFFAA00;
    private static final int COLOR_YELLOW = 0xFFFFFF55;
    private static final int COLOR_GRAY = 0xFFAAAAAA;
    private static final int COLOR_DARK_GRAY = 0xFF777777;
    private static final int COLOR_AQUA = 0xFF55FFFF;

    private final Screen parent;
    private final ResourcePackOrganizer organizer;
    private final Preset presetToEdit;

    private TextFieldWidget nameField;
    private TextFieldWidget searchField;
    private ButtonWidget saveButton;

    private final Set<String> selectedPackIds = new LinkedHashSet<>();
    private final List<ResourcePackProfile> allProfiles = new ArrayList<>();
    private final Set<String> activePackIds = new LinkedHashSet<>();

    private int page = 0;
    private String currentSearch = "";

    public PresetEditorScreen(Screen parent, ResourcePackOrganizer organizer, Preset presetToEdit) {
        super(Text.literal(presetToEdit == null ? "Create Preset" : "Edit Preset"));
        this.parent = parent;
        this.organizer = organizer;
        this.presetToEdit = presetToEdit;

        loadProfiles();
        if (presetToEdit != null && presetToEdit.getPacks() != null) {
            selectedPackIds.addAll(presetToEdit.getPacks());
        }
    }

    private void loadProfiles() {
        allProfiles.clear();
        activePackIds.clear();

        ResourcePackOrganizerAccessor accessor = (ResourcePackOrganizerAccessor) organizer;
        for (ResourcePackProfile p : accessor.getEnabledPacks()) {
            activePackIds.add(p.getId());
        }

        allProfiles.addAll(accessor.getResourcePackManager().getProfiles());
        // Sort: active packs first, then alphabetically by display name
        allProfiles.sort((a, b) -> {
            boolean aActive = activePackIds.contains(a.getId());
            boolean bActive = activePackIds.contains(b.getId());
            if (aActive != bActive) return aActive ? -1 : 1;
            return a.getDisplayName().getString().compareToIgnoreCase(b.getDisplayName().getString());
        });
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Top Row: Preset Name + Search packs
        String initialName = nameField != null ? nameField.getText() : (presetToEdit != null ? presetToEdit.getName() : "Preset " + (PresetManager.getInstance().getPresets().size() + 1));
        this.nameField = new TextFieldWidget(
                this.textRenderer,
                centerX - 170,
                34,
                160,
                20,
                Text.literal("Preset Name")
        );
        this.nameField.setMaxLength(40);
        this.nameField.setText(initialName);
        this.nameField.setPlaceholder(Text.literal("Preset name...").formatted(Formatting.GRAY));
        this.nameField.setChangedListener(text -> updateSaveButtonState());
        this.addDrawableChild(this.nameField);

        this.searchField = new TextFieldWidget(
                this.textRenderer,
                centerX + 10,
                34,
                160,
                20,
                Text.literal("Search")
        );
        this.searchField.setMaxLength(50);
        this.searchField.setText(currentSearch);
        this.searchField.setPlaceholder(Text.literal("Search packs...").formatted(Formatting.GRAY));
        this.searchField.setChangedListener(text -> {
            currentSearch = text.trim().toLowerCase();
            page = 0;
            this.clearAndInit();
        });
        this.addDrawableChild(this.searchField);

        // Action Buttons: [Select Active] and [Clear All]
        ButtonWidget selectActiveBtn = ButtonWidget.builder(
                Text.literal("Select Active").formatted(Formatting.AQUA),
                button -> {
                    selectedPackIds.addAll(activePackIds);
                    this.clearAndInit();
                }
        ).dimensions(centerX + 25, 58, 90, 18).build();
        this.addDrawableChild(selectActiveBtn);

        ButtonWidget clearBtn = ButtonWidget.builder(
                Text.literal("Clear All").formatted(Formatting.GRAY),
                button -> {
                    selectedPackIds.clear();
                    this.clearAndInit();
                }
            ).dimensions(centerX + 120, 58, 50, 18).build();
        this.addDrawableChild(clearBtn);

        // Filtered pack list
        List<ResourcePackProfile> filtered = getFilteredProfiles();
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PACKS_PER_PAGE));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        int startIndex = page * PACKS_PER_PAGE;
        int endIndex = Math.min(startIndex + PACKS_PER_PAGE, filtered.size());
        int listStartY = 80;

        for (int i = startIndex; i < endIndex; i++) {
            ResourcePackProfile profile = filtered.get(i);
            int slot = i - startIndex;
            int rowY = listStartY + slot * 32;
            String packId = profile.getId();
            boolean isSelected = selectedPackIds.contains(packId);

            // Checkbox button [✓] or [+]
            ButtonWidget toggleBtn = ButtonWidget.builder(
                    Text.literal(isSelected ? "✓" : "+").formatted(isSelected ? Formatting.GREEN : Formatting.GRAY),
                    button -> {
                        if (selectedPackIds.contains(packId)) {
                            selectedPackIds.remove(packId);
                        } else {
                            selectedPackIds.add(packId);
                        }
                        this.clearAndInit();
                    }
            ).dimensions(centerX - 168, rowY + 5, 22, 20).build();
            this.addDrawableChild(toggleBtn);
        }

        // Pagination buttons
        if (totalPages > 1) {
            int navY = listStartY + PACKS_PER_PAGE * 32 + 2;
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

        // Bottom Bar: Save & Cancel
        this.saveButton = ButtonWidget.builder(
                Text.literal("Save Preset").formatted(Formatting.GREEN),
                button -> saveAndExit()
        ).dimensions(centerX - 110, this.height - 28, 105, 20).build();
        this.addDrawableChild(this.saveButton);

        ButtonWidget cancelButton = ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> close()
        ).dimensions(centerX + 5, this.height - 28, 105, 20).build();
        this.addDrawableChild(cancelButton);

        updateSaveButtonState();
    }

    private List<ResourcePackProfile> getFilteredProfiles() {
        if (currentSearch.isEmpty()) return allProfiles;
        List<ResourcePackProfile> result = new ArrayList<>();
        for (ResourcePackProfile p : allProfiles) {
            String title = p.getDisplayName().getString().toLowerCase();
            String id = p.getId().toLowerCase();
            if (title.contains(currentSearch) || id.contains(currentSearch)) {
                result.add(p);
            }
        }
        return result;
    }

    private void updateSaveButtonState() {
        if (this.saveButton != null) {
            String name = this.nameField.getText().trim();
            this.saveButton.active = !name.isEmpty() && !selectedPackIds.isEmpty();
        }
    }

    private void saveAndExit() {
        String name = this.nameField.getText().trim();
        if (!name.isEmpty() && !selectedPackIds.isEmpty()) {
            PresetManager.getInstance().savePreset(name, new ArrayList<>(selectedPackIds));
            organizer.refresh();

            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getToastManager() != null) {
                SystemToast.show(
                        client.getToastManager(),
                        SystemToast.Type.PACK_LOAD_FAILURE,
                        Text.literal("Preset Saved!").formatted(Formatting.GREEN, Formatting.BOLD),
                        Text.literal(name + " (" + selectedPackIds.size() + " packs)").formatted(Formatting.WHITE)
                );
            }
            close();
        }
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
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        renderCardBackgrounds(context, mouseX, mouseY);
    }

    private void renderCardBackgrounds(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        List<ResourcePackProfile> filtered = getFilteredProfiles();
        int startIndex = page * PACKS_PER_PAGE;
        int endIndex = Math.min(startIndex + PACKS_PER_PAGE, filtered.size());
        int listStartY = 80;

        for (int i = startIndex; i < endIndex; i++) {
            ResourcePackProfile profile = filtered.get(i);
            int slot = i - startIndex;
            int rowY = listStartY + slot * 32;
            String packId = profile.getId();
            boolean isSelected = selectedPackIds.contains(packId);

            boolean isHovered = mouseX >= centerX - 170 && mouseX <= centerX + 170 && mouseY >= rowY && mouseY <= rowY + 30;
            int bgColor = isSelected ? (isHovered ? 0x771B4B1B : 0x55143414) : (isHovered ? 0x66333333 : 0x44000000);
            context.fill(centerX - 170, rowY, centerX + 170, rowY + 30, bgColor);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderContent(context, mouseX, mouseY);
    }

    private void renderContent(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title.copy().formatted(Formatting.GOLD, Formatting.BOLD),
                centerX,
                12,
                COLOR_GOLD
        );

        // Counter text at top
        String counterText = "Selected: " + selectedPackIds.size() + " / " + allProfiles.size() + " packs";
        drawLeftAlignedText(
                context,
                Text.literal(counterText).formatted(Formatting.YELLOW, Formatting.BOLD),
                centerX - 168,
                62,
                COLOR_YELLOW
        );

        // Packs list text
        List<ResourcePackProfile> filtered = getFilteredProfiles();
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PACKS_PER_PAGE));
        int listStartY = 80;

        if (filtered.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("No matching resource packs found.").formatted(Formatting.GRAY),
                    centerX,
                    listStartY + 40,
                    COLOR_GRAY
            );
        } else {
            int startIndex = page * PACKS_PER_PAGE;
            int endIndex = Math.min(startIndex + PACKS_PER_PAGE, filtered.size());

            for (int i = startIndex; i < endIndex; i++) {
                ResourcePackProfile profile = filtered.get(i);
                int slot = i - startIndex;
                int rowY = listStartY + slot * 32;
                String packId = profile.getId();
                boolean isSelected = selectedPackIds.contains(packId);
                boolean isActive = activePackIds.contains(packId);

                // Display name + Status badge
                String statusBadge = isActive ? " [Active]" : " [Available]";
                Formatting badgeColor = isActive ? Formatting.AQUA : Formatting.DARK_GRAY;

                Text titleText = profile.getDisplayName().copy().formatted(isSelected ? Formatting.WHITE : Formatting.GRAY)
                        .append(Text.literal(statusBadge).formatted(badgeColor));
                drawLeftAlignedText(context, titleText, centerX - 140, rowY + 4, COLOR_WHITE);

                // Clean filename preview
                String cleanedId = cleanPackId(packId);
                drawLeftAlignedText(context, Text.literal(cleanedId).formatted(Formatting.DARK_GRAY), centerX - 140, rowY + 16, COLOR_DARK_GRAY);
            }

            // Pagination indicator
            if (totalPages > 1) {
                int navY = listStartY + PACKS_PER_PAGE * 32 + 2;
                String pageText = (page + 1) + " / " + totalPages;
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(pageText).formatted(Formatting.WHITE), centerX - 12, navY + 4, COLOR_WHITE);
            }
        }
    }

    private String cleanPackId(String id) {
        if (id.startsWith("file/")) id = id.substring(5);
        if (id.endsWith(".zip")) id = id.substring(0, id.length() - 4);
        if (id.length() > 46) {
            id = id.substring(0, 43) + "...";
        }
        return id;
    }

    private void drawLeftAlignedText(DrawContext context, Text text, int x, int y, int color) {
        int width = this.textRenderer.getWidth(text);
        context.drawCenteredTextWithShadow(this.textRenderer, text, x + width / 2, y, color);
    }
}
