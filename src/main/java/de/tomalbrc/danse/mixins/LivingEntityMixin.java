package de.tomalbrc.danse.mixins;

import de.tomalbrc.danse.GestureController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "equipmentHasChanged", at = @At("RETURN"), cancellable = true)
    private void danse$onEquipmentChanged(ItemStack itemStack, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer serverPlayer && GestureController.GESTURE_CAMS.containsKey(serverPlayer.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}
