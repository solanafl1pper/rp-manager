package com.solana.rpmanager.gui;

import com.solana.rpmanager.mixin.ResourcePackOrganizerAccessor;
import com.solana.rpmanager.model.Preset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PresetPack implements ResourcePackOrganizer.Pack {
    private static final Identifier ICON = new Identifier("rpmanager", "textures/gui/preset_icon.png");

    private final ResourcePackOrganizer organizer;
    private final Preset preset;

    public PresetPack(ResourcePackOrganizer organizer, Preset preset) {
        this.organizer = organizer;
        this.preset = preset;
    }

    public Preset getPreset() {
        return preset;
    }

    @Override
    public Identifier getIconId() {
        return ICON;
    }

    @Override
    public ResourcePackCompatibility getCompatibility() {
        return ResourcePackCompatibility.COMPATIBLE;
    }

    @Override
    public String getName() {
        return "preset_" + preset.getName();
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("✦ ").formatted(Formatting.GOLD)
                .append(Text.literal(preset.getName()).formatted(Formatting.YELLOW, Formatting.BOLD));
    }

    @Override
    public Text getDescription() {
        int count = preset.getPacks() != null ? preset.getPacks().size() : 0;
        return Text.literal("Заготовка • Паков: " + count + "\nНажмите для применения").formatted(Formatting.AQUA);
    }

    @Override
    public ResourcePackSource getSource() {
        return ResourcePackSource.BUILTIN;
    }

    @Override
    public boolean isPinned() {
        return false;
    }

    @Override
    public boolean isAlwaysEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean canBeEnabled() {
        return true;
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean canMoveTowardStart() {
        return false;
    }

    @Override
    public boolean canMoveTowardEnd() {
        return false;
    }

    @Override
    public void moveTowardStart() {}

    @Override
    public void moveTowardEnd() {}

    @Override
    public void disable() {}

    @Override
    public void enable() {
        applyPreset();
    }

    public void applyPreset() {
        ResourcePackOrganizerAccessor accessor = (ResourcePackOrganizerAccessor) organizer;
        List<ResourcePackProfile> enabledList = accessor.getEnabledPacks();
        List<ResourcePackProfile> disabledList = accessor.getDisabledPacks();
        ResourcePackManager manager = accessor.getResourcePackManager();

        // 1. Keep pinned or always-enabled packs (e.g. Vanilla, Fabric mods)
        List<ResourcePackProfile> pinnedPacks = new ArrayList<>();
        for (ResourcePackProfile profile : enabledList) {
            if (profile.isPinned() || profile.isAlwaysEnabled()) {
                pinnedPacks.add(profile);
            }
        }

        // 2. Resolve packs from preset in the exact order specified
        List<ResourcePackProfile> newEnabled = new ArrayList<>();
        List<String> missingPacks = new ArrayList<>();

        if (preset.getPacks() != null) {
            for (String packName : preset.getPacks()) {
                ResourcePackProfile profile = manager.getProfile(packName);
                if (profile != null) {
                    if (!newEnabled.contains(profile)) {
                        newEnabled.add(profile);
                    }
                } else {
                    missingPacks.add(packName);
                }
            }
        }

        // Combine: put user packs in order, and ensure pinned packs are included
        for (ResourcePackProfile pinned : pinnedPacks) {
            if (!newEnabled.contains(pinned)) {
                newEnabled.add(pinned);
            }
        }

        // 3. Update enabledPacks
        enabledList.clear();
        enabledList.addAll(newEnabled);

        // 4. Update disabledPacks (all known profiles that are NOT enabled)
        disabledList.clear();
        for (ResourcePackProfile profile : manager.getProfiles()) {
            if (!enabledList.contains(profile)) {
                disabledList.add(profile);
            }
        }

        // 5. Trigger refresh callback to update the screen lists
        organizer.refresh();

        // 6. If there were missing packs, show a warning toast
        if (!missingPacks.isEmpty()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getToastManager() != null) {
                String missingNames = String.join(", ", missingPacks);
                SystemToast.show(
                        client.getToastManager(),
                        SystemToast.Type.PACK_LOAD_FAILURE,
                        Text.literal("Не найдены ресурспаки!").formatted(Formatting.RED, Formatting.BOLD),
                        Text.literal(missingNames).formatted(Formatting.GRAY)
                );
            }
        }
    }
}
