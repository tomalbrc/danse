package de.tomalbrc.danse.util;

import net.minecraft.core.Vec3i;

public class TextureUtil {
    public static Vec3i sizeFor(String name) {
        return switch (name) {
            case "body" -> new Vec3i(8,12,4);
            case "leg_r", "leg_l", "arm_r", "arm_l" -> new Vec3i(4,12,4);
            case "sarm_r", "sarm_l" -> new Vec3i(3,12,4);
            default -> new Vec3i(8, 8, 8);
        };
    }
}
