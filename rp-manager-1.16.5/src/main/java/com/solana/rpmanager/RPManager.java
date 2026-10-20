package com.solana.rpmanager;

import com.solana.rpmanager.manager.PresetManager;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RPManager implements ClientModInitializer {
    public static final String MOD_ID = "rpmanager";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Resource Pack Manager initialized for 1.16.5! Presets directory: {}",
                PresetManager.getInstance().getPresetsDir());
    }
}
