package de.tomalbrc.danse.bbmodel;

import com.google.common.collect.ImmutableMap;
import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.bbmodel.BbOutliner;
import de.tomalbrc.bil.file.extra.ResourcePackModel;
import de.tomalbrc.bil.file.importer.AjModelImporter;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.Map;

public class PlayerModelImporter extends AjModelImporter {
    static Vector3f LIMB_SCALE = new Vector3f(0.46875f, 1.40625f, 0.46875f);
    static Vector3f LIMB_SCALE_SLIM = new Vector3f(0.3225f,1.40625f,0.46875f);

    public PlayerModelImporter(BbModel model) {
        super(model);
    }

    public static ResourceLocation addItemModel(String partName, Map<String, ResourcePackModel.DisplayTransform> transformMap) {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> {
            addPart(partName, transformMap, resourcePackBuilder);
            if (Util.isArm(partName)) {
                var displayMap = ImmutableMap.of("head", new ResourcePackModel.DisplayTransform(null, new Vector3f(0.9575f * (partName.equals("arm_r") ? 1.f : -1.f), 0.5f,0.f), LIMB_SCALE_SLIM));
                addPart(partName + "s", displayMap, resourcePackBuilder);
            }
        });

        return ResourceLocation.fromNamespaceAndPath("danse", partName);
    }

    @Override
    protected ResourceLocation generateModel(BbOutliner outliner) {
        return switch (outliner.name) {
            case "head" ->
                    generateModelPart(outliner, new Vector3f(0, 5.90f, 0), new Vector3f(0.9375f, 0.9375f, 0.9375f));
            case "body" ->
                    generateModelPart(outliner, new Vector3f(0, 8.25f + 0.25f, 0), new Vector3f(0.9375f, 1.40625f, 0.46875f));
            case "arm_r" -> generateModelPart(outliner, new Vector3f(0.35f, 0.5f, 0), LIMB_SCALE);
            case "arm_l" -> generateModelPart(outliner, new Vector3f(-0.35f, 0.5f, 0), LIMB_SCALE);
            case "leg_r" -> generateModelPart(outliner, new Vector3f(0.125f, -1.9f + 0.16f, 0), LIMB_SCALE);
            case "leg_l" -> generateModelPart(outliner, new Vector3f(-0.125f, -1.9f + 0.16f, 0), LIMB_SCALE);
            default -> super.generateModel(outliner);
        };
    }

    protected ResourceLocation generateModelPart(BbOutliner outliner, Vector3f pos, Vector3f scale) {
        return addItemModel(outliner.name.toLowerCase(), ImmutableMap.of("head", new ResourcePackModel.DisplayTransform(null, pos, scale)));
    }

    private static void addPart(String partName, Map<String, ResourcePackModel.DisplayTransform> transformMap, ResourcePackBuilder resourcePackBuilder) {
        var size = Util.sizeFor(partName);
        var dataMap = PerPixelModelGenerator.generatePerPixelModels(size.getX(), size.getY(), size.getZ(), partName, transformMap);
        for (Map.Entry<String, byte[]> entry : dataMap.entrySet()) {
            resourcePackBuilder.addData(entry.getKey(), entry.getValue());
        }
    }
}
