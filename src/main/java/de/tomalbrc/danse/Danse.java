package de.tomalbrc.danse;

import de.tomalbrc.danse.commands.GestureCommand;
import de.tomalbrc.danse.registries.EntityRegistry;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Danse implements ModInitializer {
    public static final String MODID = "danse";

    public static ResourcePackBuilder RPBUILDER;

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        ModConfig.load();
        PlayerModelRegistry.load();
        EntityRegistry.registerMobs();

        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(resourcePackBuilder -> RPBUILDER = resourcePackBuilder);
        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> GestureController.onConnect(serverGamePacketListener.player));
        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> GestureController.onDisconnect(serverGamePacketListener.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> GestureCommand.register(dispatcher));
    }
}
