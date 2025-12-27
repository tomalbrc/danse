package de.tomalbrc.danse.poly;

import de.tomalbrc.danse.registry.PlayerModelRegistry;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FlatHudHolder extends HudHolder {
    public FlatHudHolder(ServerPlayer player) {
        super(player);

        this.selection.setInterpolationDuration(2);
        this.selection.setTeleportDuration(2);
    }

    protected void layout(int index) {
        final float spacingX = 0.3f;
        final float spacingY = 0.3525f;
        final float baseZ = -1.0f;
        final float selectedZ = -0.9f;
        final float scale = 0.35f;

        for (int i = 0; i < this.textDisplayElementList.size(); i++) {
            int col = i % 3;
            int row = i / 3;

            float x = (col - 1) * spacingX;
            float y = (1 - row) * spacingY - 0.12f;
            float z = (i == index) ? selectedZ : baseZ;

            Matrix4f matrix4f = new Matrix4f().translate(new Vector3f(x * Math.abs(z), y * Math.abs(z) - (i == index ? 0.017f : 0), z));
            matrix4f.scale(scale);

            if (index == i) {
                Matrix4f matrix4f2 = new Matrix4f().translate(new Vector3f(x * Math.abs(z), y * Math.abs(z) - 0.0345f, z-0.01f));
                matrix4f2.scale(scale);

                this.selection.setTransformation(matrix4f2);
                this.selection.startInterpolationIfDirty();
            }

            HudPlayerModelHolder playerModelHolder = this.modelAttachments.get(i);
            playerModelHolder.mat = matrix4f;
            if (index == i) {
                playerModelHolder.playAnimationLoop(PlayerModelRegistry.getAnimations().get(i));
                playerModelHolder.setActive(true);
            } else {
                playerModelHolder.getModel().animations().keySet().forEach(playerModelHolder.getAnimator()::stopAnimation);
                playerModelHolder.setActive(false);
            }

            this.labels.get(i).setTransformation(matrix4f.translateLocal(0.0027f, 0.000f, 0, new Matrix4f()).translate(0,0,0.001f).scale(0.395f));
            this.labels.get(i).startInterpolationIfDirty();

            this.textDisplayElementList.get(i).setTransformation(matrix4f);
            this.textDisplayElementList.get(i).startInterpolationIfDirty();
        }
    }
}
