package de.tomalbrc.danse.registry;

import de.tomalbrc.danse.entity.AnimatedPlayerModelEntity;
import de.tomalbrc.danse.entity.PlayerModelArmorStand;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;

public class EntityRegistry {
    public static final EntityType<AnimatedPlayerModelEntity> PLAYER_MODEL = register(
            AnimatedPlayerModelEntity.ID,
            EntityType.Builder.of(AnimatedPlayerModelEntity::new, MobCategory.MISC)
                    .sized(1.f, 2.f)
    );

    public static final EntityType<PlayerModelArmorStand> PLAYER_STATUE = register(
            PlayerModelArmorStand.ID,
            EntityType.Builder.of(PlayerModelArmorStand::new, MobCategory.MISC).sized(0.5F, 1.975f).eyeHeight(1.7775f).clientTrackingRange(10)
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(PLAYER_MODEL, ArmorStand.createLivingAttributes());
        FabricDefaultAttributeRegistry.register(PLAYER_STATUE, ArmorStand.createLivingAttributes());
    }

    private static <T extends Entity> EntityType<T> register(ResourceLocation id, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        EntityType<T> type = builder.build(key);
        PolymerEntityUtils.registerType(type);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }
}
