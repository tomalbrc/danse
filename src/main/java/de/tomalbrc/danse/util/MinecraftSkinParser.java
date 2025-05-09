package de.tomalbrc.danse.util;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.CustomModelData;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class MinecraftSkinParser {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    public static final Map<BodyPart, Map<Layer, Map<Direction, int[]>>> NOTCH_TEXTURE_MAP = new EnumMap<>(BodyPart.class);
    public static final Map<BodyPart, Map<Layer, Map<Direction, int[]>>> CLASSIC_TEXTURE_MAP = new EnumMap<>(BodyPart.class);
    public static final Map<BodyPart, Map<Layer, Map<Direction, int[]>>> SLIM_TEXTURE_MAP = new EnumMap<>(BodyPart.class);

    static {
        defineSteveTextures(CLASSIC_TEXTURE_MAP);
        defineNotchTextures(CLASSIC_TEXTURE_MAP, NOTCH_TEXTURE_MAP);
        defineAlexTextures(CLASSIC_TEXTURE_MAP, SLIM_TEXTURE_MAP);
    }

    private static void defineNotchTextures(Map<BodyPart, Map<Layer, Map<Direction, int[]>>> classic, Map<BodyPart, Map<Layer, Map<Direction, int[]>>> notch) {
        notch.putAll(classic);

        // LEFT_ARM
        Map<Layer, Map<Direction, int[]>> leftArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftArmInner = new EnumMap<>(Direction.class);
        leftArmInner.put(Direction.NORTH, new int[]{44+3, 20, -4, 12});
        leftArmInner.put(Direction.WEST, new int[]{40+3, 20, -4, 12});
        leftArmInner.put(Direction.SOUTH, new int[]{52+3, 20, -4, 12});
        leftArmInner.put(Direction.EAST, new int[]{48+3, 20, -4, 12});
        leftArmInner.put(Direction.UP, new int[]{44+3, 16, -4, 4});
        leftArmInner.put(Direction.DOWN, new int[]{48+3, 16, -4, 4});

        Map<Direction, int[]> leftArmOuter = new EnumMap<>(Direction.class);
        leftArmOuter.put(Direction.NORTH, new int[]{44+3, 36, -4, 12});
        leftArmOuter.put(Direction.WEST, new int[]{40+3, 36, -4, 12});
        leftArmOuter.put(Direction.SOUTH, new int[]{52+3, 36, -4, 12});
        leftArmOuter.put(Direction.EAST, new int[]{48+3, 36, -4, 12});
        leftArmOuter.put(Direction.UP, new int[]{44+3, 32, -4, 4});
        leftArmOuter.put(Direction.DOWN, new int[]{48+3, 32, -4, 4});
        leftArmMap.put(Layer.INNER, leftArmInner);
        leftArmMap.put(Layer.OUTER, leftArmOuter);
        notch.put(BodyPart.LEFT_ARM, leftArmMap);

        // LEFT_LEG
        Map<Layer, Map<Direction, int[]>> leftLegMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftLegInner = new EnumMap<>(Direction.class);
        leftLegInner.put(Direction.NORTH, new int[]{4+3, 20, -4, 12});
        leftLegInner.put(Direction.WEST, new int[]{0+3, 20, -4, 12});
        leftLegInner.put(Direction.SOUTH, new int[]{12+3, 20, -4, 12});
        leftLegInner.put(Direction.EAST, new int[]{8+3, 20, -4, 12});
        leftLegInner.put(Direction.UP, new int[]{4+3, 16, -4, 4});
        leftLegInner.put(Direction.DOWN, new int[]{8+3, 16, -4, 4});

        Map<Direction, int[]> leftLegOuter = new EnumMap<>(Direction.class);
        leftLegOuter.put(Direction.NORTH, new int[]{4+3, 36, -4, 12});
        leftLegOuter.put(Direction.WEST, new int[]{0+3, 36, -4, 12});
        leftLegOuter.put(Direction.SOUTH, new int[]{12+3, 36, -4, 12});
        leftLegOuter.put(Direction.EAST, new int[]{8+3, 36, -4, 12});
        leftLegOuter.put(Direction.UP, new int[]{4+3, 32, -4, 4});
        leftLegOuter.put(Direction.DOWN, new int[]{8+3, 32, -4, 4});
        leftLegMap.put(Layer.INNER, leftLegInner);
        leftLegMap.put(Layer.OUTER, leftLegOuter);
        notch.put(BodyPart.LEFT_LEG, leftLegMap);
    }

    private static void defineAlexTextures(Map<BodyPart, Map<Layer, Map<Direction, int[]>>> classic, Map<BodyPart, Map<Layer, Map<Direction, int[]>>> slim) {
        slim.putAll(classic);

        // RIGHT_ARM
        Map<Layer, Map<Direction, int[]>> rightArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> rightArmInner = new EnumMap<>(Direction.class);
        rightArmInner.put(Direction.EAST, new int[]{40, 20, 4, 12});
        rightArmInner.put(Direction.NORTH, new int[]{44, 20, 3, 12});
        rightArmInner.put(Direction.WEST, new int[]{47, 20, 4, 12});
        rightArmInner.put(Direction.SOUTH, new int[]{51, 20, 3, 12});
        rightArmInner.put(Direction.UP, new int[]{44, 16, 3, 4});
        rightArmInner.put(Direction.DOWN, new int[]{47, 16, 3, 4});

        Map<Direction, int[]> rightArmOuter = new EnumMap<>(Direction.class);
        rightArmOuter.put(Direction.EAST, new int[]{40, 36, 4, 12});
        rightArmOuter.put(Direction.NORTH, new int[]{44, 36, 3, 12});
        rightArmOuter.put(Direction.WEST, new int[]{47, 36, 4, 12});
        rightArmOuter.put(Direction.SOUTH, new int[]{51, 36, 3, 12});
        rightArmOuter.put(Direction.UP, new int[]{44, 32, 3, 4});
        rightArmOuter.put(Direction.DOWN, new int[]{47, 32, 3, 4});
        rightArmMap.put(Layer.INNER, rightArmInner);
        rightArmMap.put(Layer.OUTER, rightArmOuter);
        slim.put(BodyPart.RIGHT_ARM, rightArmMap);

        // LEFT_ARM
        Map<Layer, Map<Direction, int[]>> leftArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftArmInner = new EnumMap<>(Direction.class);
        leftArmInner.put(Direction.EAST, new int[]{32, 52, 4, 12});
        leftArmInner.put(Direction.NORTH, new int[]{36, 52, 3, 12});
        leftArmInner.put(Direction.WEST, new int[]{39, 52, 4, 12});
        leftArmInner.put(Direction.SOUTH, new int[]{43, 52, 3, 12});
        leftArmInner.put(Direction.UP, new int[]{36, 48, 3, 4});
        leftArmInner.put(Direction.DOWN, new int[]{39, 48, 3, 4});

        Map<Direction, int[]> leftArmOuter = new EnumMap<>(Direction.class);
        leftArmOuter.put(Direction.EAST, new int[]{48, 52, 4, 12});
        leftArmOuter.put(Direction.NORTH, new int[]{52, 52, 3, 12});
        leftArmOuter.put(Direction.WEST, new int[]{55, 52, 4, 12});
        leftArmOuter.put(Direction.SOUTH, new int[]{59, 52, 3, 12});
        leftArmOuter.put(Direction.UP, new int[]{52, 48, 3, 4});
        leftArmOuter.put(Direction.DOWN, new int[]{55, 48, 3, 4});
        leftArmMap.put(Layer.INNER, leftArmInner);
        leftArmMap.put(Layer.OUTER, leftArmOuter);
        slim.put(BodyPart.LEFT_ARM, leftArmMap);
    }

    private static void defineSteveTextures(Map<BodyPart, Map<Layer, Map<Direction, int[]>>> map) {
        Map<Layer, Map<Direction, int[]>> headMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> headInner = new EnumMap<>(Direction.class);
        headInner.put(Direction.NORTH, new int[]{8, 8, 8, 8});
        headInner.put(Direction.EAST, new int[]{0, 8, 8, 8});
        headInner.put(Direction.SOUTH, new int[]{24, 8, 8, 8});
        headInner.put(Direction.WEST, new int[]{16, 8, 8, 8});
        headInner.put(Direction.UP, new int[]{8, 0, 8, 8});
        headInner.put(Direction.DOWN, new int[]{16, 0, 8, 8});

        Map<Direction, int[]> headOuter = new EnumMap<>(Direction.class);
        headOuter.put(Direction.NORTH, new int[]{40, 8, 8, 8});
        headOuter.put(Direction.EAST, new int[]{32, 8, 8, 8});
        headOuter.put(Direction.SOUTH, new int[]{56, 8, 8, 8});
        headOuter.put(Direction.WEST, new int[]{48, 8, 8, 8});
        headOuter.put(Direction.UP, new int[]{40, 0, 8, 8});
        headOuter.put(Direction.DOWN, new int[]{48, 0, 8, 8});
        headMap.put(Layer.INNER, headInner);
        headMap.put(Layer.OUTER, headOuter);
        map.put(BodyPart.HEAD, headMap);

        // BODY
        Map<Layer, Map<Direction, int[]>> bodyMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> bodyInner = new EnumMap<>(Direction.class);
        bodyInner.put(Direction.NORTH, new int[]{20, 20, 8, 12});
        bodyInner.put(Direction.EAST, new int[]{16, 20, 4, 12});
        bodyInner.put(Direction.SOUTH, new int[]{32, 20, 8, 12});
        bodyInner.put(Direction.WEST, new int[]{28, 20, 4, 12});
        bodyInner.put(Direction.UP, new int[]{20, 16, 8, 4});
        bodyInner.put(Direction.DOWN, new int[]{28, 16, 8, 4});

        Map<Direction, int[]> bodyOuter = new EnumMap<>(Direction.class);
        bodyOuter.put(Direction.NORTH, new int[]{20, 36, 8, 12});
        bodyOuter.put(Direction.EAST, new int[]{16, 36, 4, 12});
        bodyOuter.put(Direction.SOUTH, new int[]{32, 36, 8, 12});
        bodyOuter.put(Direction.WEST, new int[]{28, 36, 4, 12});
        bodyOuter.put(Direction.UP, new int[]{20, 32, 8, 4});
        bodyOuter.put(Direction.DOWN, new int[]{28, 32, 8, 4});
        bodyMap.put(Layer.INNER, bodyInner);
        bodyMap.put(Layer.OUTER, bodyOuter);
        map.put(BodyPart.BODY, bodyMap);

        // RIGHT_ARM
        Map<Layer, Map<Direction, int[]>> rightArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> rightArmInner = new EnumMap<>(Direction.class);
        rightArmInner.put(Direction.NORTH, new int[]{44, 20, 4, 12});
        rightArmInner.put(Direction.EAST, new int[]{40, 20, 4, 12});
        rightArmInner.put(Direction.SOUTH, new int[]{52, 20, 4, 12});
        rightArmInner.put(Direction.WEST, new int[]{48, 20, 4, 12});
        rightArmInner.put(Direction.UP, new int[]{44, 16, 4, 4});
        rightArmInner.put(Direction.DOWN, new int[]{48, 16, 4, 4});

        Map<Direction, int[]> rightArmOuter = new EnumMap<>(Direction.class);
        rightArmOuter.put(Direction.NORTH, new int[]{44, 36, 4, 12});
        rightArmOuter.put(Direction.EAST, new int[]{40, 36, 4, 12});
        rightArmOuter.put(Direction.SOUTH, new int[]{52, 36, 4, 12});
        rightArmOuter.put(Direction.WEST, new int[]{48, 36, 4, 12});
        rightArmOuter.put(Direction.UP, new int[]{44, 32, 4, 4});
        rightArmOuter.put(Direction.DOWN, new int[]{48, 32, 4, 4});
        rightArmMap.put(Layer.INNER, rightArmInner);
        rightArmMap.put(Layer.OUTER, rightArmOuter);
        map.put(BodyPart.RIGHT_ARM, rightArmMap);

        // LEFT_ARM
        Map<Layer, Map<Direction, int[]>> leftArmMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftArmInner = new EnumMap<>(Direction.class);
        leftArmInner.put(Direction.NORTH, new int[]{36, 52, 4, 12});
        leftArmInner.put(Direction.EAST, new int[]{32, 52, 4, 12});
        leftArmInner.put(Direction.SOUTH, new int[]{44, 52, 4, 12});
        leftArmInner.put(Direction.WEST, new int[]{40, 52, 4, 12});
        leftArmInner.put(Direction.UP, new int[]{36, 48, 4, 4});
        leftArmInner.put(Direction.DOWN, new int[]{40, 48, 4, 4});

        Map<Direction, int[]> leftArmOuter = new EnumMap<>(Direction.class);
        leftArmOuter.put(Direction.NORTH, new int[]{52, 52, 4, 12});
        leftArmOuter.put(Direction.EAST, new int[]{48, 52, 4, 12});
        leftArmOuter.put(Direction.SOUTH, new int[]{60, 52, 4, 12});
        leftArmOuter.put(Direction.WEST, new int[]{56, 52, 4, 12});
        leftArmOuter.put(Direction.UP, new int[]{52, 48, 4, 4});
        leftArmOuter.put(Direction.DOWN, new int[]{56, 48, 4, 4});
        leftArmMap.put(Layer.INNER, leftArmInner);
        leftArmMap.put(Layer.OUTER, leftArmOuter);
        map.put(BodyPart.LEFT_ARM, leftArmMap);

        // RIGHT_LEG
        Map<Layer, Map<Direction, int[]>> rightLegMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> rightLegInner = new EnumMap<>(Direction.class);
        rightLegInner.put(Direction.NORTH, new int[]{4, 20, 4, 12});
        rightLegInner.put(Direction.EAST, new int[]{0, 20, 4, 12});
        rightLegInner.put(Direction.SOUTH, new int[]{12, 20, 4, 12});
        rightLegInner.put(Direction.WEST, new int[]{8, 20, 4, 12});
        rightLegInner.put(Direction.UP, new int[]{4, 16, 4, 4});
        rightLegInner.put(Direction.DOWN, new int[]{8, 16, 4, 4});

        Map<Direction, int[]> rightLegOuter = new EnumMap<>(Direction.class);
        rightLegOuter.put(Direction.NORTH, new int[]{4, 36, 4, 12});
        rightLegOuter.put(Direction.EAST, new int[]{0, 36, 4, 12});
        rightLegOuter.put(Direction.SOUTH, new int[]{12, 36, 4, 12});
        rightLegOuter.put(Direction.WEST, new int[]{8, 36, 4, 12});
        rightLegOuter.put(Direction.UP, new int[]{4, 32, 4, 4});
        rightLegOuter.put(Direction.DOWN, new int[]{8, 32, 4, 4});
        rightLegMap.put(Layer.INNER, rightLegInner);
        rightLegMap.put(Layer.OUTER, rightLegOuter);
        map.put(BodyPart.RIGHT_LEG, rightLegMap);

        // LEFT_LEG
        Map<Layer, Map<Direction, int[]>> leftLegMap = new EnumMap<>(Layer.class);
        Map<Direction, int[]> leftLegInner = new EnumMap<>(Direction.class);
        leftLegInner.put(Direction.NORTH, new int[]{20, 52, 4, 12});
        leftLegInner.put(Direction.EAST, new int[]{16, 52, 4, 12});
        leftLegInner.put(Direction.SOUTH, new int[]{28, 52, 4, 12});
        leftLegInner.put(Direction.WEST, new int[]{24, 52, 4, 12});
        leftLegInner.put(Direction.UP, new int[]{20, 48, 4, 4});
        leftLegInner.put(Direction.DOWN, new int[]{24, 48, 4, 4});

        Map<Direction, int[]> leftLegOuter = new EnumMap<>(Direction.class);
        leftLegOuter.put(Direction.NORTH, new int[]{4, 52, 4, 12});
        leftLegOuter.put(Direction.EAST, new int[]{0, 52, 4, 12});
        leftLegOuter.put(Direction.SOUTH, new int[]{12, 52, 4, 12});
        leftLegOuter.put(Direction.WEST, new int[]{8, 52, 4, 12});
        leftLegOuter.put(Direction.UP, new int[]{4, 48, 4, 4});
        leftLegOuter.put(Direction.DOWN, new int[]{8, 48, 4, 4});
        leftLegMap.put(Layer.INNER, leftLegInner);
        leftLegMap.put(Layer.OUTER, leftLegOuter);
        map.put(BodyPart.LEFT_LEG, leftLegMap);
    }

    public static boolean isSlimSkin(BufferedImage image) {
        return image.getWidth() == TEXTURE_WIDTH &&
                image.getHeight() == TEXTURE_HEIGHT &&
                image.getRGB(50, 16) == 0;
    }

    public static void extractTextureRGB(BufferedImage image, Map<BodyPart, Map<Layer, Map<Direction, int[]>>> textureMap, BodyPart part, Layer layer, Direction direction, Consumer<ColorData> consumer) {
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

        // ok so the textures are flipped in the sprite
        // but there is no way in hell i will adjust the item model generator to fix the model.
        // so just flip the textures
        switch (direction) {
            case EAST -> eastTex(image, consumer, width, height, startX, startY);
            case WEST -> westTex(image, consumer, width, height, startX, startY);
            case UP -> upTex(image, consumer, height, width, startX, startY);
            case SOUTH -> southTex(image, consumer, height, width, startX, startY);
            case NORTH, DOWN  -> northTex(image, consumer, height, width, startX, startY);
        }
    }

    public static void extractTextureRGB(BufferedImage image, BodyPart part, Layer layer, Direction direction, Consumer<ColorData> consumer) {
        boolean isSlim = isSlimSkin(image);
        Map<BodyPart, Map<Layer, Map<Direction, int[]>>> textureMap = isSlim ? SLIM_TEXTURE_MAP : image.getHeight() > 32 ? CLASSIC_TEXTURE_MAP : NOTCH_TEXTURE_MAP;
        extractTextureRGB(image, textureMap, part, layer, direction, consumer);
    }

    private static void eastTex(BufferedImage image, Consumer<ColorData> consumer, int width, int height, int startX, int startY) {
        // flip
        for (int dx = 0; dx < Mth.abs(width); dx++) {
            for (int dy = height - 1; dy >= 0; dy--) {
                consumer.accept(sample(image, startX, startY, dx * Mth.sign(width), dy));
            }
        }
    }

    private static void westTex(BufferedImage image, Consumer<ColorData> consumer, int width, int height, int startX, int startY) {
        // flip the other way
        for (int dx = Mth.abs(width) - 1; dx >= 0; dx--) {
            for (int dy = height - 1; dy >= 0; dy--) {
                consumer.accept(sample(image, startX, startY, dx * Mth.sign(width), dy));
            }
        }
    }

    private static void northTex(BufferedImage image, Consumer<ColorData> consumer, int height, int width, int startX, int startY) {
        for (int dy = height - 1; dy >= 0; dy--) {
            for (int dx = 0; dx < Mth.abs(width); dx++) {
                consumer.accept(sample(image, startX, startY, dx * Mth.sign(width), dy));
            }
        }
    }

    private static void upTex(BufferedImage image, Consumer<ColorData> consumer, int height, int width, int startX, int startY) {
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < Mth.abs(width); dx++) {
                consumer.accept(sample(image, startX, startY, dx * Mth.sign(width), dy));
            }
        }
    }

    private static void southTex(BufferedImage image, Consumer<ColorData> consumer, int height, int width, int startX, int startY) {
        for (int dy = height - 1; dy >= 0; dy--) {
            for (int dx = Mth.abs(width) - 1; dx >= 0; dx--) {
                consumer.accept(sample(image, startX, startY, dx * Mth.sign(width), dy));
            }
        }
    }

    private static ColorData sample(BufferedImage image, int startX, int startY, int dx, int dy) {
        int y = startY + dy;
        int x = startX + dx;
        int rgb = image.getRGB(x, y);
        return new ColorData(rgb);
    }

    public enum BodyPart {
        NONE("none", null),
        HEAD("head", EquipmentSlot.HEAD),
        BODY("body", EquipmentSlot.CHEST),
        LEFT_ARM("arm_l", EquipmentSlot.CHEST),
        RIGHT_ARM("arm_r", EquipmentSlot.CHEST),
        LEFT_ARM_SLIM("arm_ls", EquipmentSlot.CHEST),
        RIGHT_ARM_SLIM("arm_rs", EquipmentSlot.CHEST),
        LEFT_LEG("leg_l", EquipmentSlot.LEGS),
        RIGHT_LEG("leg_r", EquipmentSlot.LEGS);

        private final String name;
        private final EquipmentSlot slot;

        BodyPart(String name, EquipmentSlot slot) {
            this.name = name;
            this.slot = slot;
        }

        public String getName() {
            return this.name;
        }

        public EquipmentSlot getSlot() {
            return this.slot;
        }

        public ResourceLocation id(boolean slim) {
            if (slim) {
                return ResourceLocation.fromNamespaceAndPath("danse", getName() + "s");
            } else {
                return ResourceLocation.fromNamespaceAndPath("danse", getName());
            }
        }

        public boolean isArm() {
            return this == RIGHT_ARM || this == LEFT_ARM || this == RIGHT_ARM_SLIM || this == LEFT_ARM_SLIM;
        }

        public boolean isLeg() {
            return this == RIGHT_LEG || this == LEFT_LEG;
        }

        public static BodyPart partFrom(String partName) {
            return switch (partName) {
                case "head" -> HEAD;
                case "body" -> BODY;
                case "arm_r" -> RIGHT_ARM;
                case "arm_l" -> LEFT_ARM;
                case "arm_rs" -> RIGHT_ARM_SLIM;
                case "arm_ls" -> LEFT_ARM_SLIM;
                case "leg_l" -> LEFT_LEG;
                case "leg_r" -> RIGHT_LEG;
                default -> NONE;
            };
        }
    }

    public enum Layer {
        INNER,
        OUTER
    }

    public record ColorData(
            int color
    ) {
        public boolean alpha() {
            return ((color >> 24) & 0xff) >= 0xf0;
        }
    }

    public record PartData(CustomModelData customModelDataInner, CustomModelData customModelDataOuter, boolean slim) {

    }
}
