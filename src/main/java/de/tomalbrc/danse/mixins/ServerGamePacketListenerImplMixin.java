package de.tomalbrc.danse.mixins;

import de.tomalbrc.danse.commands.GestureCommand;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "handleSetCreativeModeSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;hasInfiniteMaterials()Z"
            )
    )
    private boolean danse$cancelSetCreativeModeSlot(ServerPlayer instance) {
        return instance.hasInfiniteMaterials() && !GestureCommand.GESTURES.containsKey(this.player.getUUID());
    }

    @Inject(method = "handleInteract", at = @At(value = "HEAD"), cancellable = true)
    private void danse$handleInteract(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        if (GestureCommand.GESTURES.containsKey(player.getUUID())) {
            ci.cancel();
        }
    }
}