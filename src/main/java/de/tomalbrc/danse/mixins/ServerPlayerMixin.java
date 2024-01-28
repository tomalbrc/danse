package de.tomalbrc.danse.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import de.tomalbrc.danse.commands.GestureCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public boolean equipmentHasChanged(ItemStack itemStack, ItemStack itemStack2) {
        return super.equipmentHasChanged(itemStack, itemStack2) && !GestureCommand.GESTURES.containsKey(this.uuid);
    }

    @Inject(method = "disconnect", at = @At("TAIL"))
    private void am_onDisconnect(CallbackInfo ci) {
        GestureCommand.GESTURES.removeInt(this.uuid);
    }
}
