package de.tomalbrc.danse.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import de.tomalbrc.danse.commands.GestureCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "handleSetCreativeModeSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"
            )
    )
    private boolean am_cancelSetCreativeModeSlot(ServerPlayerGameMode gameMode) {
        return gameMode.isCreative() && !GestureCommand.GESTURES.containsKey(this.player.getUUID());
    }
}
