package de.tomalbrc.danse.command;

import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.ModConfig;
import de.tomalbrc.danse.poly.FlatHudHolder;
import de.tomalbrc.danse.poly.HudHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.server.level.ServerPlayer;

public class GestureHudCommand {
    public static void openHud(ServerPlayer player) {
        if (!GestureController.GESTURE_CAMS.containsKey(player.getUUID()) && !player.hasContainerOpen()) {
            var hud = ModConfig.getInstance().flatHud ? new FlatHudHolder(player) : new HudHolder(player);
            EntityAttachment.ofTicking(hud, player);
            hud.startWatching(player);
        }
    }
}
