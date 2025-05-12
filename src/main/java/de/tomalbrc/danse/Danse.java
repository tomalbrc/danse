package de.tomalbrc.danse;

import com.mojang.logging.LogUtils;
import de.tomalbrc.danse.bbmodel.PlayerModelLoader;
import de.tomalbrc.danse.commands.GestureCommand;
import de.tomalbrc.danse.registries.EntityRegistry;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Danse implements ModInitializer {
    public static final String MODID = "danse";
    public static Logger LOGGER = LogUtils.getLogger();
    public static ResourcePackBuilder RPBUILDER;

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        ModConfig.load();
        PlayerModelRegistry.loadBuiltin();
        try {
            //copyResource("danse", "default.ajblueprint", "loosely-coupled.ajblueprint", "tightly-coupled.ajblueprint");
            for (Path path : loadFiles("danse", ".ajblueprint")) {
                Danse.LOGGER.info("Loading player gesture model: {}", path.getFileName());
                PlayerModelRegistry.load(PlayerModelLoader.load(path.toAbsolutePath().toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EntityRegistry.registerMobs();

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> RPBUILDER = resourcePackBuilder);
        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> GestureController.onConnect(serverGamePacketListener.player));
        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> GestureController.onDisconnect(serverGamePacketListener.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> GestureCommand.register(dispatcher));
    }

    private static List<Path> loadFiles(String subDir, String suffix) throws IOException {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve(subDir);
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(p -> p.toString().endsWith(suffix)).toList();
        }
    }

    private static void copyResource(String subDir, String... fileNames) throws IOException {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve(subDir);
        if (!dir.toFile().exists()) {
            Files.createDirectories(dir);
            for (String fileName : fileNames) {
                Path target = dir.resolve(fileName);
                if (Files.notExists(target)) {
                    String resourcePath = "/model/danse/" + fileName;
                    try (InputStream in = Danse.class.getResourceAsStream(resourcePath)) {
                        if (in == null) throw new FileNotFoundException("Resource not found: " + fileName);
                        Files.copy(in, target);
                    }
                }
            }
        }
    }
}
