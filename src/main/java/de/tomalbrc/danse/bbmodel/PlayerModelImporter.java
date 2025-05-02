package de.tomalbrc.danse.bbmodel;

import de.tomalbrc.bil.file.bbmodel.BbElement;
import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.bbmodel.BbOutliner;
import de.tomalbrc.bil.file.extra.BbModelUtils;
import de.tomalbrc.bil.file.extra.BbResourcePackGenerator;
import de.tomalbrc.bil.file.extra.ResourcePackModel;
import de.tomalbrc.bil.file.importer.AjModelImporter;
import de.tomalbrc.bil.util.ResourcePackUtil;
import de.tomalbrc.danse.TextureUtil;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
        var elements = BbModelUtils.elementsForOutliner(model, outliner, BbElement.ElementType.CUBE);
        var element = elements.getFirst();

        return addItemModel(model, outliner.name.toLowerCase(), Map.of("head", new ResourcePackModel.DisplayTransform(null, pos, scale)));
    }

    public static ResourceLocation addItemModel(BbModel model, String partName, Map<String, ResourcePackModel.DisplayTransform> transformMap) {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            try {
                var size = TextureUtil.sizeFor(partName);
                var dataMap = PerPixelModelGenerator.generatePerPixelModels(size.getX(), size.getY(), size.getZ(), partName, transformMap);
                for (Map.Entry<String, byte[]> entry : dataMap.entrySet()) {
                    resourcePackBuilder.addData(entry.getKey(), entry.getValue());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return ResourceLocation.fromNamespaceAndPath("danse", "composite_"+partName);
    }
}
