package de.tomalbrc.danse.bbmodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.bil.file.extra.ResourcePackModel;
import de.tomalbrc.bil.json.JSON;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.Direction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class PerPixelModelGenerator {
    private static final String MODEL_PREFIX = "danse:item/";
    private static final List<Direction> DIRECTIONS = ImmutableList.of(
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN
    );

    public static Map<String, byte[]> generatePerPixelModels(int px, int py, int pz, String partName, Map<String, ResourcePackModel.DisplayTransform> transformMap) {
        GridConfig grid = new GridConfig(px, py, pz);
        Map<String, byte[]> resources = new Object2ObjectArrayMap<>();
        List<ConditionModel> pixelModels = new ArrayList<>();
        Gson gson = JSON.GENERIC_BUILDER.create();

        calculateBases(grid.directionBases, grid.directionCounts, 0);

        generateAllModels(new GenerationContext(
                resources, pixelModels, transformMap, partName, gson, grid
        ));

        createCompositeModel(resources, partName, gson, pixelModels);
        return resources;
    }

    private static void generateAllModels(GenerationContext ctx) {
        for (int x = 0; x < ctx.grid.px; x++) {
            for (int y = 0; y < ctx.grid.py; y++) {
                for (int z = 0; z < ctx.grid.pz; z++) {
                    processCubeFaces(x, y, z, ctx);
                }
            }
        }
    }

    private static void processCubeFaces(int x, int y, int z, GenerationContext ctx) {
        for (int i = 0; i < DIRECTIONS.size(); i++) {
            Direction dir = DIRECTIONS.get(i);
            if (isFacePresent(x, y, z, dir, ctx.grid)) {
                createFaceModel(new FaceData(x, y, z, dir, i), ctx);
            }
        }
    }

    private static boolean isFacePresent(int x, int y, int z, Direction direction, GridConfig grid) {
        return switch (direction) {
            case Direction.NORTH -> z == 0;
            case Direction.SOUTH -> z == grid.pz - 1;
            case Direction.EAST -> x == grid.px - 1;
            case Direction.WEST -> x == 0;
            case Direction.UP -> y == grid.py - 1;
            case Direction.DOWN -> y == 0;
        };
    }

    private static void createFaceModel(FaceData face, GenerationContext ctx) {
        int perFaceIndex = calculatePerFaceIndex(face, ctx.grid);
        int tintIndex = ctx.grid.directionBases[face.directionIndex] + perFaceIndex;

        Element element = createElement(face, ctx.grid);
        Model model = new Model(List.of(element), ctx.transformMap);

        String filename = createFilename(face, ctx);
        storeModel(ctx, model, filename, tintIndex);
    }

    private static int calculatePerFaceIndex(FaceData face, GridConfig grid) {
        return switch (face.direction) {
            case Direction.NORTH, Direction.SOUTH -> face.x + face.y * grid.px;
            case Direction.EAST, Direction.WEST -> face.y + face.z * grid.py;
            default -> face.x + face.z * grid.px; // up/down
        };
    }

    private static Element createElement(FaceData face, GridConfig grid) {
        BoundingBox bb = new BoundingBox(face.x, face.y, face.z, grid);
        return new Element(bb.from(), bb.to(), ImmutableMap.of(face.direction.getName(), Face.ZERO));
    }

    private static String createFilename(FaceData face, GenerationContext ctx) {
        return String.format("%d%d%d%s", face.x, face.y, face.z, face.direction.getName().charAt(0));
    }

    private static void storeModel(GenerationContext ctx, Model model, String baseName, int tintIndex) {
        String filename = baseName + ".json";
        String path = "assets/danse/models/item/" + ctx.partName + "/" + filename;
        ctx.resources.put(path, ctx.gson.toJson(model).getBytes(StandardCharsets.UTF_8));

        ctx.pixelModels.add(new ConditionModel(tintIndex, MODEL_PREFIX + ctx.partName + "/" + baseName));
    }

    private static void calculateBases(int[] bases, int[] counts, int start) {
        int acc = start;
        for (int i = 0; i < bases.length; i++) {
            bases[i] = acc;
            acc += counts[i];
        }
    }

    private static void createCompositeModel(Map<String, byte[]> resources, String partName,
                                             Gson gson, List<ConditionModel> pixelModels) {
        CompositeModel composite = new CompositeModel();
        composite.models.addAll(pixelModels);

        byte[] bytes = gson.toJson(ImmutableMap.of("model", composite)).getBytes(StandardCharsets.UTF_8);
        resources.put("assets/danse/items/" + partName + ".json", bytes);
    }

    static class Face {
        public static Face ZERO = new Face();
        List<Integer> uv = Arrays.asList(0, 0, 16, 16);
        String texture = "#p";
        int tintindex = 0;
    }

    static class Element {
        List<Double> from;
        List<Double> to;
        Map<String, Face> faces;

        Element(List<Double> from, List<Double> to, Map<String, Face> faces) {
            this.from = from;
            this.to = to;
            this.faces = faces;
        }
    }

    static class Model {
        ImmutableMap<String, String> textures = ImmutableMap.of("p", "danse:item/model", "particle", "#p");
        List<Element> elements;
        Map<String, ResourcePackModel.DisplayTransform> display;

        Model(List<Element> elements, Map<String, ResourcePackModel.DisplayTransform> display) {
            this.elements = elements;
            this.display = display;
        }
    }

    static class ConditionModel {
        String type = "minecraft:condition";
        String property = "minecraft:custom_model_data";
        int index;
        Map<String, Object> on_true;
        Map<String, Object> on_false = ImmutableMap.of("type", "minecraft:empty");

        ConditionModel(int tintIndex, String modelPath) {
            this.index = tintIndex;
            this.on_true = ImmutableMap.of("type", "minecraft:model", "model", modelPath, "tints", List.of(new Tint(tintIndex)));
        }
    }

    static class CompositeModel {
        String type = "minecraft:composite";
        List<ConditionModel> models = new ArrayList<>();
    }

    static class Tint {
        String type = "minecraft:custom_model_data";
        int index;
        @SerializedName("default")
        int def = 0xFF_FF_FF;

        public Tint(int index) {
            this.index = index;
        }
    }

    private record GenerationContext(Map<String, byte[]> resources, List<ConditionModel> pixelModels,
                                     Map<String, ResourcePackModel.DisplayTransform> transformMap, String partName,
                                     Gson gson, GridConfig grid) {
    }

    private static class GridConfig {
        final int px, py, pz;
        final double sizeX;
        final double sizeY;
        final double sizeZ;
        final int[] directionCounts;
        final int[] directionBases;

        GridConfig(int px, int py, int pz) {
            this.px = px;
            this.py = py;
            this.pz = pz;
            this.sizeX = 8.0 / px;
            this.sizeY = 8.0 / py;
            this.sizeZ = 8.0 / pz;
            this.directionCounts = calculateDirectionCounts();
            this.directionBases = new int[DIRECTIONS.size()];
        }

        private int[] calculateDirectionCounts() {
            return new int[]{
                    px * py,  // North
                    px * py,   // South
                    py * pz,  // East
                    py * pz,   // West
                    px * pz,  // Up
                    px * pz    // Down
            };
        }
    }

    private record FaceData(int x, int y, int z, Direction direction, int directionIndex) {
    }

    private static class BoundingBox {
        final double fromX, fromY, fromZ;
        final double toX, toY, toZ;

        BoundingBox(int x, int y, int z, GridConfig grid) {
            this.fromX = calculateFrom(x, grid.sizeX, 8 - 4);
            this.toX = fromX + grid.sizeX;

            this.fromY = calculateFrom(y, grid.sizeY, 0);
            this.toY = fromY + grid.sizeY;

            this.fromZ = calculateFrom(z, grid.sizeZ, 8 - 4);
            this.toZ = fromZ + grid.sizeZ;
        }

        private double calculateFrom(int coord, double size, double offset) {
            return coord * size + offset;
        }

        List<Double> from() {
            return ImmutableList.of(fromX, fromY, fromZ);
        }

        List<Double> to() {
            return ImmutableList.of(toX, toY, toZ);
        }
    }
}