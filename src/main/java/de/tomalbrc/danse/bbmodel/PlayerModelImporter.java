package de.tomalbrc.danse.bbmodel;

import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.bbmodel.BbOutliner;
import de.tomalbrc.bil.file.extra.BbResourcePackGenerator;
import de.tomalbrc.bil.file.extra.ResourcePackItemModel;
import de.tomalbrc.bil.file.importer.AjModelImporter;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;

public class PlayerModelImporter extends AjModelImporter {
    static Vector3f LIMB_SCALE = new Vector3f(0.46875f,1.40625f,0.46875f);

    public PlayerModelImporter(BbModel model) {
        super(model);
    }

    protected PolymerModelData generateModel(BbOutliner outliner) {
        switch (outliner.name) {
            case "head": return generateHeadModel(outliner, new Vector3f(0,5.90f,0), new Vector3f(0.9375f,0.9375f,0.9375f));
            case "body": return generateHeadModel(outliner, new Vector3f(0,8.25f + 0.25f,0), new Vector3f(0.9375f,1.40625f,0.46875f));
            case "arm_r": return generateHeadModel(outliner, new Vector3f(0.35f,0.5f,0), LIMB_SCALE);
            case "arm_l": return generateHeadModel(outliner, new Vector3f(-0.35f,0.5f,0), LIMB_SCALE);
            case "leg_r": return generateHeadModel(outliner, new Vector3f(0.2f,-1.9f + 0.25f,0), LIMB_SCALE);
            case "leg_l": return generateHeadModel(outliner, new Vector3f(-0.2f,-1.9f + 0.25f,0), LIMB_SCALE);
            //case "sarm_r": return generateHeadModel(outliner, new Vector3f(-5.125f,0.f,0.f), new Vector3f(0.3515625f,1.40625f,0.46875f)); // todo: slim support
            //case "sarm_l": return generateHeadModel(outliner, new Vector3f(5.125f,0.f,0.f), new Vector3f(0.3515625f,1.40625f,0.46875f)); // pos might be wrong too
        }

        return super.generateModel(outliner);
    }

    protected PolymerModelData generateHeadModel(BbOutliner outliner, Vector3f pos, Vector3f scale) {
        ResourcePackItemModel.Builder builder = new ResourcePackItemModel.Builder(model.modelIdentifier)
                .withParent("builtin/entity")
                .addDisplayTransform("head", new ResourcePackItemModel.DisplayTransform(null, pos, scale));

        ResourceLocation location = BbResourcePackGenerator.addModelPart(model, outliner.name.toLowerCase(), builder.build());
        return PolymerResourcePackUtils.requestModel(Items.PLAYER_HEAD, location);
    }
}
