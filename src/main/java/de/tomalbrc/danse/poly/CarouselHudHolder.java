package de.tomalbrc.danse.poly;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CarouselHudHolder extends HudHolder {
    public CarouselHudHolder(ServerPlayer player) {
        super(player);

        this.selection.setInterpolationDuration(1);
        this.selection.setTeleportDuration(1);
    }

    @Override
    protected void layout(int index) {
        int itemCount = this.textDisplayElementList.size();
        float radius = 1.0f;
        float angleStep = 20f;
        float baseY = -0.12f;
        float baseZOffset = 0.2f;
        float baseScale = 0.35f;
        float selectedScaleMul = 1.15f;
        float falloffPerSlot = 0.12f;
        int half = itemCount / 2;

        for (int i = 0; i < itemCount; i++) {
            int delta = i - index;
            if (delta > half) delta -= itemCount;
            if (delta < -half) delta += itemCount;

            float absDelta = Math.abs(delta);

            float angleDeg = delta * angleStep;
            float angleRad = angleDeg * Mth.DEG_TO_RAD;

            float x = (float) Math.sin(angleRad) * radius;
            float z = -((float) Math.cos(angleRad) * radius + baseZOffset);

            float y = baseY - absDelta * 0.03f;

            float scaleMul = Math.max(0.20f, 1.0f - falloffPerSlot * absDelta);
            float scale = baseScale * (i == index ? (scaleMul * selectedScaleMul) : scaleMul);

            Matrix4f matrix = new Matrix4f()
                    .translate(new Vector3f(x, y, z))
                    .rotateLocal(new Quaternionf().rotateY(-angleRad))
                    .rotateLocal(new Quaternionf().rotateX(Mth.DEG_TO_RAD * (5f * absDelta)))
                    .scale(scale);

            if (i == index) {
                Matrix4f sel = new Matrix4f()
                        .translate(new Vector3f(x, y + 0.02f, z - 0.01f))
                        .rotateLocal(new Quaternionf().rotateY(-angleRad))
                        .rotateLocal(new Quaternionf().rotateX(Mth.DEG_TO_RAD * (6f * absDelta))) // maybe push up y
                        .scale(scale * 1.02f);

                this.selection.setTransformation(sel);
                this.selection.startInterpolationIfDirty();
            }

            this.textDisplayElementList.get(i).setTransformation(matrix);
            this.textDisplayElementList.get(i).startInterpolationIfDirty();
        }
    }
}
