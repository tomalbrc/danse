package de.tomalbrc.danse;

import de.tomalbrc.danse.commands.GestureCommand;
import de.tomalbrc.danse.registries.EntityRegistry;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Danse implements ModInitializer {
    public static final String MODID = "danse";

    @Override
    public void onInitialize() {
        forceAutohost();
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        PlayerModelRegistry.load();
        EntityRegistry.registerMobs();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> GestureCommand.register(dispatcher));
    }

    void forceAutohost() {
        try {
            Path path = FabricLoader.getInstance().getGameDir().resolve("polymer/.force_autohost");
            Files.createDirectories(path.getParent()); // Create parent directories if they don't exist
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
