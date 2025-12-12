package de.tomalbrc.danse.util;

import net.minecraft.resources.Identifier;

public class CacheKey {
    private final Identifier assetLocation;
    private final MinecraftSkinParser.BodyPart part;
    private final boolean inner;

    public CacheKey(Identifier assetLocation, MinecraftSkinParser.BodyPart part, boolean inner) {
        this.assetLocation = assetLocation;
        this.part = part;
        this.inner = inner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheKey that)) return false;
        return inner == that.inner
            && assetLocation.equals(that.assetLocation)
            && part == that.part;
    }

    @Override
    public int hashCode() {
        int result = assetLocation.hashCode();
        result = 31 * result + part.hashCode();
        result = 31 * result + (inner ? 1 : 0);
        return result;
    }
}