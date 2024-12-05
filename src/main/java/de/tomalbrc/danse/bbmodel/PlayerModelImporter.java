package de.tomalbrc.danse.bbmodel;

import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.bbmodel.BbOutliner;
import de.tomalbrc.bil.file.extra.BbResourcePackGenerator;
import de.tomalbrc.bil.file.extra.ResourcePackModel;
import de.tomalbrc.bil.file.importer.AjModelImporter;
import de.tomalbrc.bil.util.ResourcePackUtil;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.nio.charset.StandardCharsets;

public class PlayerModelImporter extends AjModelImporter {
    static Vector3f LIMB_SCALE = new Vector3f(0.46875f,1.40625f,0.46875f);

    public PlayerModelImporter(BbModel model) {
        super(model);
    }

    @Override
    protected ResourceLocation generateModel(BbOutliner outliner) {
        return switch (outliner.name) {
            case "head" -> generateHeadModel(outliner, new Vector3f(0, 5.90f, 0), new Vector3f(0.9375f, 0.9375f, 0.9375f));
            case "body" -> generateHeadModel(outliner, new Vector3f(0, 8.25f + 0.25f, 0), new Vector3f(0.9375f, 1.40625f, 0.46875f));
            case "arm_r" -> generateHeadModel(outliner, new Vector3f(0.35f, 0.5f, 0), LIMB_SCALE);
            case "arm_l" -> generateHeadModel(outliner, new Vector3f(-0.35f, 0.5f, 0), LIMB_SCALE);
            case "leg_r" -> generateHeadModel(outliner, new Vector3f(0.2f, -1.9f + 0.25f, 0), LIMB_SCALE);
            case "leg_l" -> generateHeadModel(outliner, new Vector3f(-0.2f, -1.9f + 0.25f, 0), LIMB_SCALE);
            //case "sarm_r": return generateHeadModel(outliner, new Vector3f(-5.125f,0.f,0.f), new Vector3f(0.3515625f,1.40625f,0.46875f)); // todo: slim support
            //case "sarm_l": return generateHeadModel(outliner, new Vector3f(5.125f,0.f,0.f), new Vector3f(0.3515625f,1.40625f,0.46875f)); // pos might be wrong too
            default -> super.generateModel(outliner);
        };
    }

    protected ResourceLocation generateHeadModel(BbOutliner outliner, Vector3f pos, Vector3f scale) {
        ResourcePackModel.Builder builder = new ResourcePackModel.Builder(model.modelIdentifier)
                .withParent("builtin/entity")
                .addDisplayTransform("head", new ResourcePackModel.DisplayTransform(null, pos, scale));

        return addItemModel(model, outliner.name.toLowerCase(), builder.build());
    }

    public static ResourceLocation addItemModel(BbModel model, String partName, ResourcePackModel resourcePackModel) {
        var modelPath = BbResourcePackGenerator.addModelPart(model, partName, resourcePackModel);

        var str = """
                {
                  "model": {
                    "type": "minecraft:special",
                    "base": "%s",
                    "model": {
                      "type": "minecraft:head",
                      "kind": "player"
                    }
                  }
                }
                """;

        var bytes = str.formatted(modelPath.toString()).getBytes(StandardCharsets.UTF_8);

        var id = ResourceLocation.fromNamespaceAndPath("bil", partName);
        ResourcePackUtil.add(ResourceLocation.parse(":" + AssetPaths.itemAsset(id)), bytes);

        return id;
    }
}
