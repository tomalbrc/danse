package de.tomalbrc.danse.registries;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.Danse;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

public class PlayerModelRegistry {
    static Map<String, Model> animationModelMap = new Object2ObjectOpenHashMap<>();

    public static void load(Model model) {
        for (String animationName : model.animations().keySet()) {
            Danse.LOGGER.info("Loaded animation {}", animationName);
            animationModelMap.put(animationName, model);
        }
    }

    public static Model getModel(String animationName) {
        return animationModelMap.get(animationName);
    }

    public static List<String> getAnimations() {
        return animationModelMap.keySet().stream().toList();
    }
}
