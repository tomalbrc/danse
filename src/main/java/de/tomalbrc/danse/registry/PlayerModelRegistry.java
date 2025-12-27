package de.tomalbrc.danse.registry;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.danse.Danse;
import de.tomalbrc.danse.bbmodel.PlayerModelLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public class PlayerModelRegistry {
    static Map<String, Model> animationModelMap = new Object2ObjectArrayMap<>();
    static Map<String, Model> texturedModelMap = new Object2ObjectArrayMap<>();

    public static void addFrom(Model model) {
        for (String animationName : model.animations().keySet()) {
            Danse.LOGGER.info("Loaded animation {}", animationName);
            animationModelMap.put(animationName, model);
        }
    }

    public static void loadBuiltin() {
        var sourceModels = ImmutableList.of(
                "loosely-coupled",
                "tightly-coupled",
                "default"
                // add more if needed
        );

        for (String filename : sourceModels) {
            Model model = new PlayerModelLoader().loadResource(Identifier.fromNamespaceAndPath("danse", filename));
            Model normalModel = new AjBlueprintLoader().loadResource(Identifier.fromNamespaceAndPath("danse", filename));
            for (String animationName : model.animations().keySet()) {
                animationModelMap.put(animationName, model);
                texturedModelMap.put(animationName, normalModel);
            }
        }
    }


    public static Model getTexturedModel(String animationName) {
        return texturedModelMap.get(animationName);
    }

    public static Model getModel(String animationName) {
        return animationModelMap.get(animationName);
    }

    public static List<String> getAnimations() {
        return animationModelMap.keySet().stream().toList();
    }
}
