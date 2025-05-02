package de.tomalbrc.danse.bbmodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class PerPixelModelGenerator {

    static class Face {
        List<Integer> uv = Arrays.asList(0, 0, 16, 16);
        String texture = "#p";
        int tintindex;

        Face(int tintindex) {
            this.tintindex = tintindex;
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

        Model(List<Element> elements) {
            this.elements = elements;
        }
    }

    static class ConditionModel {
        String type = "minecraft:condition";
        String property = "minecraft:custom_model_data";
        int index;
        Map<String, Object> on_true;
        Map<String, Object> on_false = Collections.singletonMap("type", "minecraft:empty");

        ConditionModel(int index, String modelPath) {
            this.index = index;
            this.on_true = Map.of("type", "minecraft:model", "model", modelPath);
        }
    }

    static class CompositeModel {
        String type = "minecraft:composite";
        List<ConditionModel> models = new ArrayList<>();
    }

    private static void createModelForFace(int ix, int iy, int iz, double sizeX, double sizeY, double sizeZ, int tintIndex, String faceDirection, List<Map<String, Object>> pixelModels, Map<String, byte[]> res, Gson gson, Path dir) throws IOException {
        // Calculate the correct bounds for the face
        double fromX = ix * sizeX;
        double toX = fromX + sizeX;
        double fromY = iy * sizeY;
        double toY = fromY + sizeY;
        double fromZ = iz * sizeZ;
        double toZ = fromZ + sizeZ;

        // Adjust for the specific face direction (north, south, east, west, up, down)
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

        // Create the element for the face
        Element element = new Element(
                Arrays.asList(fromX, fromY, fromZ),
                Arrays.asList(toX, toY, toZ),
                faces
        );

        // Create the model for that face
        Model model = new Model(Collections.singletonList(element));

        // Save the model to a file
        String filename = String.format("face_%d_%d_%d_%s.json", ix, iy, iz, faceDirection);
        var bytes = gson.toJson(model).getBytes(StandardCharsets.UTF_8);
        res.put("assets/danse/models/item/" + filename, bytes);

        System.out.println("Saved " + filename);

        pixelModels.add(Map.of(
                "index", pixelModels.size(),
                "model_path", String.format("danse:item/face_%d_%d_%d_%s", ix, iy, iz, faceDirection)
        ));
    }

    public static Map<String, byte[]> generatePerPixelModels(int px, int py, int pz, Path dir) throws IOException {
        Map<String, byte[]> res = new Object2ObjectArrayMap<>();

        double sizeX = 8.0 / px;
        double sizeY = 8.0 / py;
        double sizeZ = 8.0 / pz;

        var file = dir.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }

        Gson gson = new GsonBuilder().create();
        int tintIndex = 0;
        List<Map<String, Object>> pixelModels = new ArrayList<>();

        // Generate models for the faces of the structure, not the internal pixels
        for (int ix = 0; ix < px; ix++) {
            for (int iy = 0; iy < py; iy++) {
                for (int iz = 0; iz < pz; iz++) {

                    // Only create models for faces that are on the boundary of the structure
                    // North face (for iz == 0)
                    if (iz == 0) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "north", pixelModels, res, gson, dir);
                    }
                    // South face (for iz == pz - 1)
                    if (iz == pz - 1) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "south", pixelModels, res, gson, dir);
                    }
                    // East face (for ix == px - 1)
                    if (ix == px - 1) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "east", pixelModels, res, gson, dir);
                    }
                    // West face (for ix == 0)
                    if (ix == 0) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "west", pixelModels, res, gson, dir);
                    }
                    // Up face (for iy == py - 1)
                    if (iy == py - 1) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "up", pixelModels, res, gson, dir);
                    }
                    // Down face (for iy == 0)
                    if (iy == 0) {
                        createModelForFace(ix, iy, iz, sizeX, sizeY, sizeZ, tintIndex, "down", pixelModels, res, gson, dir);
                    }

                    tintIndex++;
                }
            }
        }

        // Generate the composite model
        CompositeModel composite = new CompositeModel();
        for (Map<String, Object> pixel : pixelModels) {
            int index = (Integer) pixel.get("index");
            String modelPath = (String) pixel.get("model_path");
            ConditionModel condition = new ConditionModel(index, modelPath);
            composite.models.add(condition);
        }

        var bytes = gson.toJson(Map.of("model", composite)).getBytes(StandardCharsets.UTF_8);
        res.put("assets/danse/items/composite.json", bytes);

        System.out.println("Saved composite.json");

        return res;
    }
}