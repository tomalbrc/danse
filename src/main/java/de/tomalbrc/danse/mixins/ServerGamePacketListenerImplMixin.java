package de.tomalbrc.danse.mixins;

import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.poly.GestureCamera;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Shadow public abstract void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket);

    @Inject(method = "handlePlayerInput", at = @At(value = "HEAD"), cancellable = true)
    private void danse$handleInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket, CallbackInfo ci) {
        GestureCamera camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            if (serverboundPlayerInputPacket.input().shift()) {
                GestureController.onStop(camera);
            }

            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At(value = "HEAD"), cancellable = true)
    private void danse$handleInteract(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        if (GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void danse$handleMove(ServerboundMovePlayerPacket serverboundMovePlayerPacket, CallbackInfo ci) {
        GestureCamera camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            if (serverboundMovePlayerPacket instanceof ServerboundMovePlayerPacket.Rot rot) {
                camera.setYaw(rot.getYRot(this.player.getYRot()));
                camera.setPitch(rot.getXRot(this.player.getXRot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "HEAD"), cancellable = true)
    private void danse$handleAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket, CallbackInfo ci) {
        if (GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            ci.cancel();
        }
    }
}