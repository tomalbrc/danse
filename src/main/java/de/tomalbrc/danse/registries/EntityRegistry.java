package de.tomalbrc.danse.registries;

import de.tomalbrc.danse.entities.GestureCamera;
import de.tomalbrc.danse.entities.GestureSeat;
import de.tomalbrc.danse.entities.PlayerModelEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityRegistry {
    public static final EntityType<PlayerModelEntity> PLAYER_MODEL = register(
            PlayerModelEntity.ID,
            EntityType.Builder.of(PlayerModelEntity::new, MobCategory.MISC)
                    .sized(1.f, 2.f)
    );

    public static final EntityType<GestureCamera> GESTURE_CAMERA = register(
            GestureCamera.ID,
            FabricEntityType.Builder.createLiving(
                    GestureCamera::new,
                    MobCategory.MISC,
                    (living) -> living.defaultAttributes(GestureCamera::createAttributes))
                    .noSave()
                    .noSummon()
                    .sized(.25f, .25f)
    );

    public static final EntityType<GestureSeat> GESTURE_SEAT = register(
            GestureSeat.ID,
            FabricEntityType.Builder.createLiving(GestureSeat::new, MobCategory.MISC, living -> living.defaultAttributes(GestureSeat::createAttributes))
                    .sized(.25f, .25f)
                    .noSummon()
    );

    public static void registerMobs() {
    }

    private static <T extends Entity> EntityType<T> register(ResourceLocation id, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        EntityType<T> type = builder.build(key);
        PolymerEntityUtils.registerType(type);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }
}
