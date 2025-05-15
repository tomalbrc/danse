package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.entity.PlayerModelArmorStand;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import net.minecraft.core.Rotations;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class StatuePlayerPartHolder<T extends PlayerModelArmorStand & AnimatedEntity> extends PlayerPartHolder<T> {
    public StatuePlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    @Override
    protected Quaternionfc rotation(MinecraftSkinParser.BodyPart part) {
        Rotations pose = switch (part) {
            case LEFT_ARM, LEFT_ARM_SLIM -> parent.getLeftArmPose();
            case RIGHT_ARM, RIGHT_ARM_SLIM -> parent.getRightArmPose();
            case LEFT_LEG -> parent.getLeftLegPose();
            case RIGHT_LEG -> parent.getRightLegPose();
            case BODY -> parent.getBodyPose();
            case NONE -> null;
            case HEAD -> parent.getHeadPose();
        };

        if (pose == null)
            return null;

        return new Quaternionf().rotateXYZ(
                (float) Math.toRadians(-pose.x()),
                (float) Math.toRadians(pose.y()),
                (float) Math.toRadians(pose.z())
        );
    }
}
