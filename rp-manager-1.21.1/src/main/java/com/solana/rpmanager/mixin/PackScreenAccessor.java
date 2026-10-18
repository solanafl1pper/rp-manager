package com.solana.rpmanager.mixin;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PackScreen.class)
public interface PackScreenAccessor {
    @Accessor("organizer")
    ResourcePackOrganizer getOrganizer();

    @Accessor("availablePackList")
    PackListWidget getAvailablePackList();

    @Accessor("selectedPackList")
    PackListWidget getSelectedPackList();

    @Invoker("updatePackLists")
    void invokeUpdatePackLists();
}
