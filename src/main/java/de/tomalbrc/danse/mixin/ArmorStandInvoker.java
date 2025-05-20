package de.tomalbrc.danse.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStand.class)
public interface ArmorStandInvoker {
    @Invoker
    void invokeBrokenByAnything(ServerLevel serverLevel, DamageSource damageSource);
}
