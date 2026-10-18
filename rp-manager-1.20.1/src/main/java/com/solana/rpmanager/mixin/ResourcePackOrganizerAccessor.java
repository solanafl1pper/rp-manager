package com.solana.rpmanager.mixin;

import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ResourcePackOrganizer.class)
public interface ResourcePackOrganizerAccessor {
    @Accessor("enabledPacks")
    List<ResourcePackProfile> getEnabledPacks();

    @Accessor("disabledPacks")
    List<ResourcePackProfile> getDisabledPacks();

    @Accessor("resourcePackManager")
    ResourcePackManager getResourcePackManager();
}
