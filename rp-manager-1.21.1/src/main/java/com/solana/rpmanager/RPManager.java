package com.solana.rpmanager;

import com.solana.rpmanager.manager.PresetManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPManager implements ClientModInitializer {
    public static final String MOD_ID = "rpmanager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Resource Pack Manager initialized for 1.21.1! Presets directory: {}",
                PresetManager.getInstance().getPresetsDir());
    }
}
