package de.tomalbrc.danse;

import de.tomalbrc.danse.commands.GestureCommand;
import de.tomalbrc.danse.registries.EntityRegistry;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Danse implements ModInitializer {
    public static final String MODID = "danse";

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        PlayerModelRegistry.load();
        EntityRegistry.registerMobs();

        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> {
            GestureController.onConnect(serverGamePacketListener.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> {
            GestureController.onDisconnect(serverGamePacketListener.player);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> GestureCommand.register(dispatcher));
    }
}
