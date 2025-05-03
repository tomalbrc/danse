package de.tomalbrc.danse.bbmodel;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.bil.file.extra.ResourcePackModel;
import de.tomalbrc.bil.json.JSON;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PerPixelModelGenerator {
    private static final String MODEL_PREFIX = "danse:item/";
    private static final List<String> DIRECTIONS = List.of(
            "north", "south", "east", "west", "up", "down"
    );

    static class Face {
        List<Integer> uv = Arrays.asList(0, 0, 16, 16);
        String texture = "#p";
        int tintindex = 0;

        public static Face ZERO = new Face();
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
        Map<String, String> textures = Collections.singletonMap("p", "danse:item/model");
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

    private record GenerationContext(Map<String, byte[]> resources, List<Map<String, Object>> pixelModels,
                                     Map<String, ResourcePackModel.DisplayTransform> transformMap, String partName,
                                     Gson gson, GridConfig grid, boolean inflated) {
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
                    px * py,  // North/South
                    px * py,   // North/South
                    py * pz,  // East/West
                    py * pz,   // East/West
                    px * pz,  // Up/Down
                    px * pz    // Up/Down
            };
        }
    }

    private record FaceData(int x, int y, int z, String direction, int directionIndex) {
    }

    public static Map<String, byte[]> generatePerPixelModels(int px, int py, int pz,
                                                             String partName,
                                                             Map<String, ResourcePackModel.DisplayTransform> transformMap)
            throws IOException {
        GridConfig grid = new GridConfig(px, py, pz);
        Map<String, byte[]> resources = new Object2ObjectArrayMap<>();
        List<Map<String, Object>> pixelModels = new ArrayList<>();
        Gson gson = JSON.GENERIC_BUILDER.create();

        calculateBases(grid.directionBases, grid.directionCounts, 0);

        generateAllModels(new GenerationContext(
                resources, pixelModels, transformMap, partName, gson, grid, false
        ));

        int totalOriginal = Arrays.stream(grid.directionCounts).sum();
        calculateBases(grid.directionBases, grid.directionCounts, totalOriginal);

        var map = Map.of("head", new ResourcePackModel.DisplayTransform(null, transformMap.get("head").translation().add(0, 0.025f, 0, new Vector3f()), transformMap.get("head").scale().add(0.05f,0.05f,0.05f, new Vector3f())));
        generateAllModels(new GenerationContext(
                resources, pixelModels, map, partName, gson, grid, true
        ));

        createCompositeModel(resources, partName, gson, pixelModels);
        return resources;
    }

    private static void generateAllModels(GenerationContext ctx) throws IOException {
        for (int x = 0; x < ctx.grid.px; x++) {
            for (int y = 0; y < ctx.grid.py; y++) {
                for (int z = 0; z < ctx.grid.pz; z++) {
                    processCubeFaces(x, y, z, ctx);
                }
            }
        }
    }

    private static void processCubeFaces(int x, int y, int z, GenerationContext ctx) throws IOException {
        for (int i = 0; i < DIRECTIONS.size(); i++) {
            String dir = DIRECTIONS.get(i);
            if (isFacePresent(x, y, z, dir, ctx.grid)) {
                createFaceModel(new FaceData(x, y, z, dir, i), ctx);
            }
        }
    }

    private static boolean isFacePresent(int x, int y, int z, String direction, GridConfig grid) {
        return switch (direction) {
            case "north" -> z == 0;
            case "south" -> z == grid.pz - 1;
            case "east" -> x == grid.px - 1;
            case "west" -> x == 0;
            case "up" -> y == grid.py - 1;
            case "down" -> y == 0;
            default -> false;
        };
    }

    private static void createFaceModel(FaceData face, GenerationContext ctx) throws IOException {
        int perFaceIndex = calculatePerFaceIndex(face, ctx.grid);
        int tintIndex = ctx.grid.directionBases[face.directionIndex] + perFaceIndex;

        Element element = createElement(face, ctx.grid, ctx.inflated);
        Model model = new Model(List.of(element), ctx.transformMap);

        String filename = createFilename(face, ctx);
        storeModel(ctx, model, filename, tintIndex);
    }

    private static int calculatePerFaceIndex(FaceData face, GridConfig grid) {
        return switch (face.direction) {
            case "north", "south" -> face.x + face.y * grid.px;
            case "east", "west" -> face.y + face.z * grid.py;
            default -> face.x + face.z * grid.px; // up/down
        };
    }

    private static Element createElement(FaceData face, GridConfig grid, boolean inflated) {
        BoundingBox bb = new BoundingBox(face.x, face.y, face.z, grid, inflated);
        return new Element(bb.from(), bb.to(), Map.of(face.direction, Face.ZERO));
    }

    private static String createFilename(FaceData face, GenerationContext ctx) {
        String suffix = ctx.inflated ? "_inflated" : "";
        return String.format("f%d_%d_%d%s%s", face.x, face.y, face.z, face.direction, suffix);
    }

    private static void storeModel(GenerationContext ctx, Model model, String baseName, int tintIndex) {
        String filename = baseName + ".json";
        String path = "assets/danse/models/item/" + ctx.partName + "/" + filename;
        ctx.resources.put(path, ctx.gson.toJson(model).getBytes(StandardCharsets.UTF_8));

        ctx.pixelModels.add(Map.of(
                "index", tintIndex,
                "path", MODEL_PREFIX + ctx.partName + "/" + baseName
        ));
    }

    private static void calculateBases(int[] bases, int[] counts, int start) {
        int acc = start;
        for (int i = 0; i < bases.length; i++) {
            bases[i] = acc;
            acc += counts[i];
        }
    }

    private static void createCompositeModel(Map<String, byte[]> resources, String partName,
                                             Gson gson, List<Map<String, Object>> pixelModels) {
        CompositeModel composite = new CompositeModel();
        pixelModels.forEach(p -> composite.models.add(
                new ConditionModel((Integer) p.get("index"), (String) p.get("path"))
        ));

        byte[] bytes = gson.toJson(Map.of("model", composite)).getBytes(StandardCharsets.UTF_8);
        resources.put("assets/danse/items/composite_" + partName + ".json", bytes);
    }

    private static class BoundingBox {
        final double fromX, fromY, fromZ;
        final double toX, toY, toZ;

        BoundingBox(int x, int y, int z, GridConfig grid, boolean inflated) {
            this.fromX = calculateFrom(x, grid.sizeX, 8 - 4, inflated);
            this.toX = fromX + grid.sizeX;

            this.fromY = calculateFrom(y, grid.sizeY, 0, inflated);
            this.toY = fromY + grid.sizeY;

            this.fromZ = calculateFrom(z, grid.sizeZ, 8 - 4, inflated);
            this.toZ = fromZ + grid.sizeZ;
        }

        private double calculateFrom(int coord, double size, double offset, boolean inflated) {
            return coord * size + offset;
        }

        List<Double> from() {
            return List.of(fromX, fromY, fromZ);
        }

        List<Double> to() {
            return List.of(toX, toY, toZ);
        }
    }
}