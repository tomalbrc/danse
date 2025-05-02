package de.tomalbrc.danse.bbmodel;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.bil.file.extra.ResourcePackModel;
import de.tomalbrc.bil.json.JSON;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PerPixelModelGenerator {

    static class Face {
        List<Integer> uv = Arrays.asList(0, 0, 16, 16);
        String texture = "#p";
        int tintindex;

        Face(int tintindex) {
            this.tintindex = 0;
        }
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

    private static void createModelForFace(int ix, int iy, int iz, double sizeX, double sizeY, double sizeZ, int tintIndex, String faceDirection, List<Map<String, Object>> pixelModels, Map<String, byte[]> res, Map<String, ResourcePackModel.DisplayTransform> transformMap, String partName, Gson gson) throws IOException {
        double fromX = ix * sizeX + (8 - 4);
        double toX = fromX + sizeX;
        double fromY = iy * sizeY + (0);
        double toY = fromY + sizeY;
        double fromZ = iz * sizeZ + (8 - 4);
        double toZ = fromZ + sizeZ;

        Map<String, Face> faces = new HashMap<>();
        switch (faceDirection) {
            case "north":
                faces.put("north", new Face(tintIndex));
                break;
            case "south":
                faces.put("south", new Face(tintIndex));
                break;
            case "east":
                faces.put("east", new Face(tintIndex));
                break;
            case "west":
                faces.put("west", new Face(tintIndex));
                break;
            case "up":
                faces.put("up", new Face(tintIndex));
                break;
            case "down":
                faces.put("down", new Face(tintIndex));
                break;
        }

        Element element = new Element(
                Arrays.asList(fromX, fromY, fromZ),
                Arrays.asList(toX, toY, toZ),
                faces
        );

        Model model = new Model(Collections.singletonList(element), transformMap);

        String filename = String.format("f%d_%d_%d%s.json", ix, iy, iz, faceDirection);
        String filenameWithoutExt = String.format("f%d_%d_%d%s", ix, iy, iz, faceDirection);
        byte[] bytes = gson.toJson(model).getBytes(StandardCharsets.UTF_8);
        res.put("assets/danse/models/item/" + partName + "/" + filename, bytes);

        pixelModels.add(ImmutableMap.of(
                "index", tintIndex,
                "model_path", String.format("danse:item/" + partName + "/" + filenameWithoutExt)
        ));
    }

    public static Map<String, byte[]> generatePerPixelModels(int px, int py, int pz, String partName, Map<String, ResourcePackModel.DisplayTransform> transformMap) throws IOException {
        Map<String, byte[]> res = new Object2ObjectArrayMap<>();

        double sizeX = 8.0 / px;
        double sizeY = 8.0 / py;
        double sizeZ = 8.0 / pz;

        Gson gson = JSON.GENERIC_BUILDER.create();
        int tintIndex = 0;
        List<Map<String, Object>> pixelModels = new ArrayList<>();

        for (int ix = 0; ix < px; ix++) {
            for (int iy = 0; iy < py; iy++) {
                for (int iz = 0; iz < pz; iz++) {
                    // north
                    if (iz == 0) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "north", pixelModels, res, transformMap, partName, gson);
                        tintIndex++;
                    }
                    // south
                    if (iz == pz - 1) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "south", pixelModels, res, transformMap, partName, gson);
                        tintIndex++;
                    }
                    // east
                    if (ix == px - 1) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "east", pixelModels, res, transformMap, partName, gson);
                        tintIndex++;
                    }
                    // west
                    if (ix == 0) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "west", pixelModels, res, transformMap, partName, gson);
                        tintIndex++;
                    }
                    // up
                    if (iy == py - 1) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "up", pixelModels, res, transformMap, partName, gson);
                        tintIndex++;
                    }
                    // down
                    if (iy == 0) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "down", pixelModels, res, transformMap, partName, gson);
                        tintIndex++;
                    }
                }
            }
        }

        CompositeModel composite = new CompositeModel();
        for (Map<String, Object> pixel : pixelModels) {
            int index = (Integer) pixel.get("index");
            String modelPath = (String) pixel.get("model_path");
            ConditionModel condition = new ConditionModel(index, modelPath);
            composite.models.add(condition);
        }

        var bytes = gson.toJson(ImmutableMap.of("model", composite)).getBytes(StandardCharsets.UTF_8);
        res.put("assets/danse/items/composite_" + partName + ".json", bytes);

        return res;
    }
}