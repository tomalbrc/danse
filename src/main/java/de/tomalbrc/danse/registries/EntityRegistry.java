package de.tomalbrc.danse.registries;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import de.tomalbrc.danse.entities.GestureCamera;
import de.tomalbrc.danse.entities.PlayerModelEntity;

public class EntityRegistry {
    public static final EntityType<PlayerModelEntity> PLAYER_MODEL = register(
            PlayerModelEntity.ID,
            FabricEntityTypeBuilder.create()
                    .entityFactory(PlayerModelEntity::new)
                    .spawnGroup(MobCategory.MISC)
                    .dimensions(EntityDimensions.scalable(1.f, 2.f))
    );

    public static final EntityType<GestureCamera> GESTURE_CAMERA = register(
            GestureCamera.ID,
            FabricEntityTypeBuilder.createLiving()
                    .entityFactory(GestureCamera::new)
                    .spawnGroup(MobCategory.MISC)
                    .dimensions(EntityDimensions.fixed(.25f, .25f))
                    .defaultAttributes(GestureCamera::createAttributes)
                    .disableSaving()
                    .disableSummon()
    );

    public static void registerMobs() {
    }

    private static <T extends Entity> EntityType<T> register(ResourceLocation id, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        PolymerEntityUtils.registerType(type);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
    }
}
