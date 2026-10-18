package com.solana.rpmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Preset {
    private String name;
    private List<String> packs;
    private long createdAt;

    public Preset() {
        this.packs = new ArrayList<>();
    }

    public Preset(String name, List<String> packs) {
        this.name = name;
        this.packs = new ArrayList<>(packs);
        this.createdAt = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPacks() {
        return packs;
    }

    public void setPacks(List<String> packs) {
        this.packs = packs;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFileName() {
        if (name == null || name.trim().isEmpty()) {
            return "preset_" + createdAt + ".json";
        }
        String clean = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return clean.isEmpty() ? "preset_" + createdAt + ".json" : clean + ".json";
    }
}
