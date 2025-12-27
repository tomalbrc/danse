package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.core.holder.base.SimpleAnimatedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.Danse;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

class HudPlayerModelHolder extends SimpleAnimatedHolder {
    private final ServerPlayer player;
    private int age = 0;

    public Matrix4fc mat = new Matrix4f();

    public boolean active = false;

    public HudPlayerModelHolder(ServerPlayer player, Model model) {
        super(model);
        this.player = player;
    }

    @Override
    protected void onAsyncTick() {
        super.onAsyncTick();
        this.age++;
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl p) {
        return p.player == player && super.startWatching(p);
    }

    @Override
    protected void updateElement(ServerPlayer serverPlayer, DisplayWrapper<?> display) {
        var queryResult = this.animationComponent.findPose(serverPlayer, display);
        if (queryResult != null) {
            if (queryResult.owner() != serverPlayer && display.element().getDataTracker().isDirty()) {
                this.updateElement(queryResult.owner(), display, display.getDefaultPose());
            } else {
                this.updateElement(queryResult.owner(), display, queryResult.pose());
            }
        }
    }

    @Override
    protected void applyPose(ServerPlayer serverPlayer, Pose pose, DisplayWrapper<?> display) {
        display.element().setBillboardMode(Display.BillboardConstraints.CENTER);
        display.element().setBrightness(Brightness.FULL_BRIGHT);
        display.element().setGlowing(true);

        var mmm = new Matrix4f();
        mmm.translate(mat.getTranslation(new Vector3f()));
        mmm.rotateLocal(mat.getNormalizedRotation(new Quaternionf()));

        var ma = mmm.get(new Matrix4f()).translate(0, 0, 0).translateLocal(0, 0.05f, 0).mul(pose.matrix().scaleLocal(scale, new Matrix4f()).rotateLocalY(Mth.DEG_TO_RAD * ((age * 2) % 360)));
        display.element().setTransformation(null, ma.rotateY(Mth.PI));
        display.element().startInterpolationIfDirty(null);
    }

    public void playAnimationLoop(String name) {
        this.getAnimator().playAnimation(name, (p) -> {
            if (active) Danse.SERVER.execute(() -> {
                playAnimationLoop(name);
            });
        });
    }

    public void setActive(boolean b) {
        this.active = b;

        if (active) {
            asyncTick();
            tick();
            for (Bone<?> bone : this.getBones()) {
                bone.element().setInterpolationDuration(null, 2);
                bone.element().setTeleportDuration(null, 2);
                bone.setInvisible(false);
            }
        } else {
            for (Bone<?> bone : this.getBones()) {
                bone.element().setInterpolationDuration(null, 0);
                bone.element().setTeleportDuration(null, 0);
                bone.setInvisible(true);
            }
            asyncTick();
            tick();
        }
    }
}
