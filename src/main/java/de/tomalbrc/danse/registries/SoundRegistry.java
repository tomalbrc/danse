package de.tomalbrc.danse.registries;

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SoundRegistry {
    public static final SoundEvent PENGUIN_AMBIENT = register("penguin.ambient", SoundEvents.TURTLE_AMBIENT_LAND);
    public static final SoundEvent PENGUIN_HURT = register("penguin.hurt", SoundEvents.TURTLE_HURT);
    public static final SoundEvent PENGUIN_DEATH = register("penguin.death", SoundEvents.TURTLE_DEATH);

    public static final SoundEvent GOLEM_AMBIENT = register("golem.ambient", SoundEvents.EMPTY);
    public static final SoundEvent GOLEM_HURT = register("golem.hurt", SoundEvents.IRON_GOLEM_HURT);
    public static final SoundEvent GOLEM_DEATH = register("golem.death", SoundEvents.IRON_GOLEM_DEATH);


    private static SoundEvent register(String name, SoundEvent soundEvent) {
        ResourceLocation id = new ResourceLocation("animated_java", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, PolymerSoundEvent.of(id, soundEvent));
    }

    public static void registerSounds() {
    }
}

