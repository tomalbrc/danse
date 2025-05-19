package de.tomalbrc.danse.mixin;

import de.tomalbrc.danse.entity.StatuePlayerModelEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStand.class)
public class ArmorStandMixin {
    @Inject(method = "brokenByPlayer", at = @At("HEAD"), cancellable = true)
    private void danse$replaceDrop(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
        if ((Object)this instanceof StatuePlayerModelEntity playerPartHolder) {
            playerPartHolder.customBrokenByPlayer(serverLevel, damageSource);
            ci.cancel();
        }
    }
}
