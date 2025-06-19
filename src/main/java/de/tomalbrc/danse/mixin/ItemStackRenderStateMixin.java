package de.tomalbrc.danse.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.class)
public abstract class ItemStackRenderStateMixin {

    @Shadow public abstract void clearModelIdentity();

    @Inject(method = "clear", at = @At(value = "HEAD"))
    private void billionDollarCompanyIsToDumbToMakeWorkingGames(CallbackInfo ci) {
        clearModelIdentity();
    }
}
