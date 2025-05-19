package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.entity.StatuePlayerModelEntity;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import net.minecraft.core.Rotations;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class StatuePlayerPartHolder<T extends StatuePlayerModelEntity & AnimatedEntity> extends PlayerPartHolder<StatuePlayerModelEntity> {
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

        var wiggle = (float) (getAttachment().getWorld().getGameTime() - parent.lastHit) + 0.5f;
        var wiggleDegree = Mth.sin(wiggle / 1.5F * Mth.PI) * 6.0F;

        return new Quaternionf().rotateXYZ(
                (float) Math.toRadians(-pose.x()),
                (float) Math.toRadians(pose.y() + wiggleDegree),
                (float) Math.toRadians(pose.z())
        );
    }

    @Override
    protected boolean isDirty() {
        return super.isDirty() || parent.isPoseDirty();
    }

    @Override
    public void onAsyncTick() {
        super.onAsyncTick();

        this.parent.setPoseDirty(false);
    }
}
