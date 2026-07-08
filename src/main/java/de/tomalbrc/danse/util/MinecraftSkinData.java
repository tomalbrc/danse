package de.tomalbrc.danse.util;

import java.awt.image.BufferedImage;

public record MinecraftSkinData(BufferedImage image, Model model) {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;

    public enum Model {
        WIDE, SLIM;

        public static Model from(String str) {
            if ("slim".equalsIgnoreCase(str)) return SLIM;
            return WIDE;
        }
    }

    public static Model guessSkinModel(BufferedImage image) {
        boolean isSlim = image.getWidth() == TEXTURE_WIDTH &&
                image.getHeight() == TEXTURE_HEIGHT &&
                image.getRGB(50, 16) == 0;

        return isSlim ? Model.SLIM : Model.WIDE;
    }

    public static MinecraftSkinData from(BufferedImage image) {
        return new MinecraftSkinData(image, guessSkinModel(image));
    }
}
