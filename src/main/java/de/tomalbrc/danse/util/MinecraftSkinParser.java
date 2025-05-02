package de.tomalbrc.danse.util;

import net.minecraft.core.Direction;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class MinecraftSkinParser {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;

    public enum BodyPart {
        NONE, HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG;

        public static BodyPart partFrom(String partName) {
            return switch (partName) {
                case "head" -> HEAD;
                case "body" -> BODY;
                case "arm_r" -> RIGHT_ARM;
                case "arm_l" -> LEFT_ARM;
                case "leg_l" -> LEFT_LEG;
                case "leg_r" -> RIGHT_LEG;
                default -> NONE;
            };
        }
    }

    public enum Layer {
        INNER, OUTER;
    }

    private static final Map<BodyPart, Map<Layer, Map<Direction, int[]>>> STEVE_TEXTURE_MAP = new EnumMap<>(BodyPart.class);
    private static final Map<BodyPart, Map<Layer, Map<Direction, int[]>>> ALEX_TEXTURE_MAP = new EnumMap<>(BodyPart.class);


    static {
        defineSteveTextures(STEVE_TEXTURE_MAP);
        defineSteveTextures(ALEX_TEXTURE_MAP);
    }

    private static void defineSteveTextures(Map<BodyPart, Map<Layer, Map<Direction, int[]>>> map) {
        Map<Layer, Map<Direction, int[]>> headMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> headInner = new EnumMap<>(Direction.class);
        headInner.put(Direction.NORTH, new int[]{8, 8, 8, 8});    // Front
        headInner.put(Direction.EAST,  new int[]{0, 8, 8, 8});    // Right
        headInner.put(Direction.SOUTH, new int[]{24, 8, 8, 8});   // Back
        headInner.put(Direction.WEST,  new int[]{16, 8, 8, 8});   // Left
        headInner.put(Direction.UP,    new int[]{8, 0, 8, 8});    // Top
        headInner.put(Direction.DOWN,  new int[]{16, 0, 8, 8});   // Bottom

        Map<Direction, int[]> headOuter = new EnumMap<>(Direction.class);
        headOuter.put(Direction.NORTH, new int[]{40, 8, 8, 8});   // Front (hat)
        headOuter.put(Direction.EAST,  new int[]{32, 8, 8, 8});   // Right
        headOuter.put(Direction.SOUTH, new int[]{56, 8, 8, 8});   // Back
        headOuter.put(Direction.WEST,  new int[]{48, 8, 8, 8});   // Left
        headOuter.put(Direction.UP,    new int[]{40, 0, 8, 8});   // Top
        headOuter.put(Direction.DOWN,  new int[]{48, 0, 8, 8});   // Bottom
        headMap.put(Layer.INNER, headInner);
        headMap.put(Layer.OUTER, headOuter);
        map.put(BodyPart.HEAD, headMap);

        // BODY
        Map<Layer, Map<Direction, int[]>> bodyMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> bodyInner = new EnumMap<>(Direction.class);
        bodyInner.put(Direction.NORTH, new int[]{20, 20, 8, 12}); // Front
        bodyInner.put(Direction.EAST,  new int[]{16, 20, 4, 12}); // Right
        bodyInner.put(Direction.SOUTH, new int[]{32, 20, 8, 12}); // Back
        bodyInner.put(Direction.WEST,  new int[]{28, 20, 4, 12}); // Left
        bodyInner.put(Direction.UP,    new int[]{20, 16, 8, 4});  // Top
        bodyInner.put(Direction.DOWN,  new int[]{28, 16, 8, 4});  // Bottom

        Map<Direction, int[]> bodyOuter = new EnumMap<>(Direction.class);
        bodyOuter.put(Direction.NORTH, new int[]{20, 36, 8, 12});
        bodyOuter.put(Direction.EAST,  new int[]{16, 36, 4, 12});
        bodyOuter.put(Direction.SOUTH, new int[]{32, 36, 8, 12});
        bodyOuter.put(Direction.WEST,  new int[]{28, 36, 4, 12});
        bodyOuter.put(Direction.UP,    new int[]{20, 32, 8, 4});
        bodyOuter.put(Direction.DOWN,  new int[]{28, 32, 8, 4});
        bodyMap.put(Layer.INNER, bodyInner);
        bodyMap.put(Layer.OUTER, bodyOuter);
        map.put(BodyPart.BODY, bodyMap);

        // RIGHT_ARM
        Map<Layer, Map<Direction, int[]>> rightArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> rightArmInner = new EnumMap<>(Direction.class);
        rightArmInner.put(Direction.NORTH, new int[]{44, 20, 4, 12});
        rightArmInner.put(Direction.EAST,  new int[]{40, 20, 4, 12});
        rightArmInner.put(Direction.SOUTH, new int[]{52, 20, 4, 12});
        rightArmInner.put(Direction.WEST,  new int[]{48, 20, 4, 12});
        rightArmInner.put(Direction.UP,    new int[]{44, 16, 4, 4});
        rightArmInner.put(Direction.DOWN,  new int[]{48, 16, 4, 4});

        Map<Direction, int[]> rightArmOuter = new EnumMap<>(Direction.class);
        rightArmOuter.put(Direction.NORTH, new int[]{44, 36, 4, 12});
        rightArmOuter.put(Direction.EAST,  new int[]{40, 36, 4, 12});
        rightArmOuter.put(Direction.SOUTH, new int[]{52, 36, 4, 12});
        rightArmOuter.put(Direction.WEST,  new int[]{48, 36, 4, 12});
        rightArmOuter.put(Direction.UP,    new int[]{44, 32, 4, 4});
        rightArmOuter.put(Direction.DOWN,  new int[]{48, 32, 4, 4});
        rightArmMap.put(Layer.INNER, rightArmInner);
        rightArmMap.put(Layer.OUTER, rightArmOuter);
        map.put(BodyPart.RIGHT_ARM, rightArmMap);

        // LEFT_ARM
        Map<Layer, Map<Direction, int[]>> leftArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftArmInner = new EnumMap<>(Direction.class);
        leftArmInner.put(Direction.NORTH, new int[]{36, 52, 4, 12});
        leftArmInner.put(Direction.EAST,  new int[]{32, 52, 4, 12});
        leftArmInner.put(Direction.SOUTH, new int[]{44, 52, 4, 12});
        leftArmInner.put(Direction.WEST,  new int[]{40, 52, 4, 12});
        leftArmInner.put(Direction.UP,    new int[]{36, 48, 4, 4});
        leftArmInner.put(Direction.DOWN,  new int[]{40, 48, 4, 4});

        Map<Direction, int[]> leftArmOuter = new EnumMap<>(Direction.class);
        leftArmOuter.put(Direction.NORTH, new int[]{36, 68, 4, 12});
        leftArmOuter.put(Direction.EAST,  new int[]{32, 68, 4, 12});
        leftArmOuter.put(Direction.SOUTH, new int[]{44, 68, 4, 12});
        leftArmOuter.put(Direction.WEST,  new int[]{40, 68, 4, 12});
        leftArmOuter.put(Direction.UP,    new int[]{36, 64, 4, 4});
        leftArmOuter.put(Direction.DOWN,  new int[]{40, 64, 4, 4});
        leftArmMap.put(Layer.INNER, leftArmInner);
        leftArmMap.put(Layer.OUTER, leftArmOuter);
        map.put(BodyPart.LEFT_ARM, leftArmMap);

        Map<Layer, Map<Direction, int[]>> rightLegMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> rightLegInner = new EnumMap<>(Direction.class);
        rightLegInner.put(Direction.NORTH, new int[]{4, 20, 4, 12});
        rightLegInner.put(Direction.EAST,  new int[]{0, 20, 4, 12});
        rightLegInner.put(Direction.SOUTH, new int[]{12, 20, 4, 12});
        rightLegInner.put(Direction.WEST,  new int[]{8, 20, 4, 12});
        rightLegInner.put(Direction.UP,    new int[]{4, 16, 4, 4});
        rightLegInner.put(Direction.DOWN,  new int[]{8, 16, 4, 4});

        Map<Direction, int[]> rightLegOuter = new EnumMap<>(Direction.class);
        rightLegOuter.put(Direction.NORTH, new int[]{4, 36, 4, 12});
        rightLegOuter.put(Direction.EAST,  new int[]{0, 36, 4, 12});
        rightLegOuter.put(Direction.SOUTH, new int[]{12, 36, 4, 12});
        rightLegOuter.put(Direction.WEST,  new int[]{8, 36, 4, 12});
        rightLegOuter.put(Direction.UP,    new int[]{4, 32, 4, 4});
        rightLegOuter.put(Direction.DOWN,  new int[]{8, 32, 4, 4});
        rightLegMap.put(Layer.INNER, rightLegInner);
        rightLegMap.put(Layer.OUTER, rightLegOuter);
        map.put(BodyPart.RIGHT_LEG, rightLegMap);

        // LEFT_LEG
        Map<Layer, Map<Direction, int[]>> leftLegMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftLegInner = new EnumMap<>(Direction.class);
        leftLegInner.put(Direction.NORTH, new int[]{20, 52, 4, 12});
        leftLegInner.put(Direction.EAST,  new int[]{16, 52, 4, 12});
        leftLegInner.put(Direction.SOUTH, new int[]{28, 52, 4, 12});
        leftLegInner.put(Direction.WEST,  new int[]{24, 52, 4, 12});
        leftLegInner.put(Direction.UP,    new int[]{20, 48, 4, 4});
        leftLegInner.put(Direction.DOWN,  new int[]{24, 48, 4, 4});

        Map<Direction, int[]> leftLegOuter = new EnumMap<>(Direction.class);
        leftLegOuter.put(Direction.NORTH, new int[]{20, 68, 4, 12});
        leftLegOuter.put(Direction.EAST,  new int[]{16, 68, 4, 12});
        leftLegOuter.put(Direction.SOUTH, new int[]{28, 68, 4, 12});
        leftLegOuter.put(Direction.WEST,  new int[]{24, 68, 4, 12});
        leftLegOuter.put(Direction.UP,    new int[]{20, 64, 4, 4});
        leftLegOuter.put(Direction.DOWN,  new int[]{24, 64, 4, 4});
        leftLegMap.put(Layer.INNER, leftLegInner);
        leftLegMap.put(Layer.OUTER, leftLegOuter);
        map.put(BodyPart.LEFT_LEG, leftLegMap);
    }


    public static boolean isSlimSkin(BufferedImage image) {
        return image.getWidth() == TEXTURE_WIDTH &&
                image.getHeight() == TEXTURE_HEIGHT &&
                image.getRGB(50, 16) == 0;
    }

    public static void extractTextureRGB(BufferedImage image, BodyPart part, Layer layer, Direction direction, Consumer<ColorData> consumer) {
        boolean isSlim = isSlimSkin(image);
        Map<BodyPart, Map<Layer, Map<Direction, int[]>>> textureMap = isSlim ? ALEX_TEXTURE_MAP : STEVE_TEXTURE_MAP;
        Map<Layer, Map<Direction, int[]>> partMap = textureMap.get(part);
        if (partMap == null) return;

        Map<Direction, int[]> layerMap = partMap.get(layer);
        if (layerMap == null) return;

        int[] region = layerMap.get(direction);
        if (region == null) return;

        int startX = region[0];
        int startY = region[1];
        int width = region[2];
        int height = region[3];

        for (int dx = 0; dx < width; dx++) {
            for (int dy = 0; dy < height; dy++) {
                int rgb = image.getRGB(startX + dx, startY + dy);
                consumer.accept(new ColorData(rgb, dx, dy, width, height));
            }
        }
    }

    public record ColorData(
            int color,
            int x, int y,
            int width, int height
    ) {}
}
