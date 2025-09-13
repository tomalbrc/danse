package de.tomalbrc.danse;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.bbmodel.PlayerModelLoader;
import de.tomalbrc.danse.command.GestureCommand;
import de.tomalbrc.danse.emotecraft.EmotecraftAnimationFile;
import de.tomalbrc.danse.emotecraft.EmotecraftLoader;
import de.tomalbrc.danse.registry.EntityRegistry;
import de.tomalbrc.danse.registry.ItemRegistry;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.danse.util.GestureDialog;
import de.tomalbrc.dialogutils.DialogUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Danse implements ModInitializer {
    public static final String MODID = "danse";
    public static Logger LOGGER = LogUtils.getLogger();
    public static BufferedImage STEVE_TEXTURE;

    public static Int2IntArrayMap VIRTUAL_ENTITY_PICK_MAP = new Int2IntArrayMap();

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        DialogUtils.init();
        ModConfig.load();
        PlayerModelRegistry.loadBuiltin();

        loadAnimations();
        GestureDialog.add(ModConfig.getInstance().addGestureDialog);

        EntityRegistry.register();
        ItemRegistry.register();

        ServerPlayerEvents.COPY_FROM.register((serverPlayer, serverPlayer1, b) -> {
            var cameraHolder = GestureController.GESTURE_CAMS.get(serverPlayer.getUUID());
            if (cameraHolder != null) GestureController.onStop(cameraHolder);
        });

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> loadAnimations());

        var imageData = DialogUtils.resourcePackBuilder().getDataOrSource("assets/minecraft/textures/entity/player/wide/steve.png");
        try {
            STEVE_TEXTURE = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    private void loadAnimations() {
        try {
            for (Path path : loadFiles("danse", ".ajblueprint")) {
                Danse.LOGGER.info("Loading player gesture model: {}", path.getFileName());
                PlayerModelRegistry.addFrom(PlayerModelLoader.load(path.toAbsolutePath().toString()));
            }

            var added = false;
            EmotecraftLoader loader = new EmotecraftLoader();
            for (Path path : loadFiles("danse", ".json")) {
                Danse.LOGGER.info("Loading emotecraft emote: {}", path.getFileName());
                var res = new Gson().fromJson(new InputStreamReader(new FileInputStream(path.toAbsolutePath().toString())), EmotecraftAnimationFile.class);
                loader.add(res);
                added = true;
            }

            if (added) {
                Model model = loader.loadResource(ResourceLocation.fromNamespaceAndPath("danse", "tightly-coupled"));
                PlayerModelRegistry.addFrom(model);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
