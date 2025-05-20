package de.tomalbrc.danse.mixin;

import de.tomalbrc.danse.Danse;
import de.tomalbrc.danse.GestureController;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Shadow protected abstract void tryPickItem(ItemStack itemStack);

    @Inject(method = "handlePlayerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setLastClientInput(Lnet/minecraft/world/entity/player/Input;)V"), cancellable = true)
    private void danse$handleInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket, CallbackInfo ci) {
        var camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            if (serverboundPlayerInputPacket.input().shift()) {
                GestureController.onStop(camera);
            }

            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasClientLoaded()Z"), cancellable = true)
    private void danse$handleInteract(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        if (GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            ci.cancel();
        }
    }

    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;containsInvalidValues(DDDFF)Z"), cancellable = true)
    private void danse$handleMove(ServerboundMovePlayerPacket serverboundMovePlayerPacket, CallbackInfo ci) {
        var camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            if (serverboundMovePlayerPacket instanceof ServerboundMovePlayerPacket.Rot rot) {
                camera.setYaw(rot.getYRot(this.player.getYRot()));
                camera.setPitch(rot.getXRot(this.player.getXRot()));
            }
            ci.cancel();
        }
    }

    @ModifyArg(method = "handlePickItemFromEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getEntityOrPart(I)Lnet/minecraft/world/entity/Entity;"))
    private int danse$handleEntityPick(int i) {
        if (Danse.VIRTUAL_ENTITY_PICK_MAP.containsKey(i)) {
            return Danse.VIRTUAL_ENTITY_PICK_MAP.get(i);
        }

        return i;
    }
}