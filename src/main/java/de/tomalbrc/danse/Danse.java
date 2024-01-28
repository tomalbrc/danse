package de.tomalbrc.danse;

import de.tomalbrc.danse.registries.EntityRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import de.tomalbrc.danse.commands.GestureCommand;

public class Danse implements ModInitializer {
    public static final String MODID = "danse";

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        EntityRegistry.registerMobs();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            GestureCommand.register(dispatcher);
        });
    }
}
