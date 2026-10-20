package com.solana.rpmanager.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.solana.rpmanager.model.Preset;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PresetManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("RPManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final PresetManager INSTANCE = new PresetManager();

    private final Path presetsDir;
    private final List<Preset> presets = new ArrayList<>();

    private PresetManager() {
        this.presetsDir = FabricLoader.getInstance().getGameDir().resolve("resourcepack_presets");
        ensureDirectory();
        loadPresets();
    }

    public static PresetManager getInstance() {
        return INSTANCE;
    }

    public Path getPresetsDir() {
        return presetsDir;
    }

    private void ensureDirectory() {
        try {
            if (!Files.exists(presetsDir)) {
                Files.createDirectories(presetsDir);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create presets directory: {}", presetsDir, e);
        }
    }

    public synchronized void loadPresets() {
        presets.clear();
        ensureDirectory();
        File folder = presetsDir.toFile();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    Preset preset = GSON.fromJson(reader, Preset.class);
                    if (preset != null && preset.getName() != null) {
                        presets.add(preset);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to read preset from file: {}", file.getName(), e);
                }
            }
        }
        presets.sort(Comparator.comparing(Preset::getName, String.CASE_INSENSITIVE_ORDER));
    }

    public synchronized Preset savePreset(String name, List<String> packs) {
        ensureDirectory();
        Preset preset = new Preset(name, packs);
        File file = presetsDir.resolve(preset.getFileName()).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(preset, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save preset '{}' to {}", name, file, e);
        }
        loadPresets();
        return preset;
    }

    public synchronized boolean deletePreset(Preset preset) {
        if (preset == null) return false;
        File file = presetsDir.resolve(preset.getFileName()).toFile();
        boolean deleted = false;
        if (file.exists()) {
            deleted = file.delete();
        }
        loadPresets();
        return deleted;
    }

    public synchronized List<Preset> getPresets() {
        return new ArrayList<>(presets);
    }
}
