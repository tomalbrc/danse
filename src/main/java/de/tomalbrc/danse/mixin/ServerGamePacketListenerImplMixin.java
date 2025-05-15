package de.tomalbrc.danse.mixin;

import de.tomalbrc.danse.Danse;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow protected abstract void tryPickItem(ItemStack itemStack);

    @Inject(method = "handlePickItemFromEntity", at = @At("HEAD"))
    private void filament$handleEntityPick(ServerboundPickItemFromEntityPacket serverboundPickItemFromEntityPacket, CallbackInfo ci) {
        var v = Danse.VIRTUAL_ENTITY_PICK_MAP.get(serverboundPickItemFromEntityPacket.id());
        if (v != null) {
            this.tryPickItem(v);
        }
    }
}
