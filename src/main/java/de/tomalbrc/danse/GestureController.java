package de.tomalbrc.danse;

import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.entities.PlayerModelEntity;
import de.tomalbrc.danse.poly.GestureCamera;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import de.tomalbrc.danse.util.CustomModelDataCache;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.UUID;

public class GestureController {
    public static final Reference2ObjectOpenHashMap<UUID, GestureCamera> GESTURE_CAMS = new Reference2ObjectOpenHashMap<>();

    public static void onConnect(ServerPlayer serverPlayer) {
        new ResolvableProfile(serverPlayer.getGameProfile()).resolve().thenAccept(x -> {
            CustomModelDataCache.fetch(x.gameProfile(), (dataMap) -> {
            });
        });
    }

    public static void onDisconnect(ServerPlayer serverPlayer) {
        GESTURE_CAMS.remove(serverPlayer.getUUID());
    }

    public static void onStop(ServerPlayer player, PlayerModelEntity playerModel) {
        if (!player.hasDisconnected()) {
            PolymerUtils.reloadInventory(player);

            player.setInvulnerable(false);
            player.setInvisible(false);
            GestureController.GESTURE_CAMS.get(player.getUUID()).destroy();
            GestureController.GESTURE_CAMS.remove(player.getUUID());

            player.connection.send(new ClientboundSetCameraPacket(player));
        }

        playerModel.discard();
    }

    public static void onStart(ServerPlayer player, String animationName) {
        CustomModelDataCache.fetch(player.getGameProfile(), dataMap -> {
            GestureCamera camera = GestureController.GESTURE_CAMS.get(player.getUUID());
            if (camera != null) {
                camera.destroy();
            }

            GesturePlayerModelEntity playerModel = new GesturePlayerModelEntity(player, PlayerModelRegistry.getModel(animationName), dataMap);
            playerModel.setCheckDistance(false);

            GestureCamera gestureCamera = new GestureCamera(player, playerModel);
            GestureController.GESTURE_CAMS.put(player.getUUID(), gestureCamera);

            ChunkAttachment.ofTicking(gestureCamera, player.serverLevel(), player.position());

            // removes model etc when animation finishes
            playerModel.playAnimation(animationName, () -> {
                GestureController.onStop(player, playerModel);
            });

            player.serverLevel().addFreshEntity(playerModel);
        });
    }
}
