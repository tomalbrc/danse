package de.tomalbrc.danse.util;

import com.google.gson.annotations.SerializedName;

public class EquipmentAsset {
    public Layers layers;

    public static class Layers {
        public TextureEntry[] humanoid;
        @SerializedName("humanoid_leggings")
        public TextureEntry[] humanoidLeggings;
    }

    public static class TextureEntry {
        public String texture;
    }
}