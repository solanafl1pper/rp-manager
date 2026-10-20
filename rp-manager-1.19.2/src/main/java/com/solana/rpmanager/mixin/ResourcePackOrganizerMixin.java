package com.solana.rpmanager.mixin;

import com.solana.rpmanager.gui.PresetPack;
import com.solana.rpmanager.manager.PresetManager;
import com.solana.rpmanager.model.Preset;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(ResourcePackOrganizer.class)
public class ResourcePackOrganizerMixin {
    @Inject(method = "getDisabledPacks", at = @At("RETURN"), cancellable = true)
    private void rpmanager$injectPresets(CallbackInfoReturnable<Stream<ResourcePackOrganizer.Pack>> cir) {
        PresetManager.getInstance().loadPresets();
        List<Preset> presets = PresetManager.getInstance().getPresets();
        if (presets.isEmpty()) return;

        List<ResourcePackOrganizer.Pack> presetPacks = new ArrayList<>();
        for (Preset preset : presets) {
            presetPacks.add(new PresetPack((ResourcePackOrganizer)(Object)this, preset));
        }

        cir.setReturnValue(Stream.concat(presetPacks.stream(), cir.getReturnValue()));
    }
}
