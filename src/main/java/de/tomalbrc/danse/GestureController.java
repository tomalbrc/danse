package de.tomalbrc.danse;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.danse.entity.GesturePlayerModelEntity;
import de.tomalbrc.danse.poly.GestureCameraHolder;
import de.tomalbrc.danse.registry.EntityRegistry;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.TextureCache;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GestureController {
    public static final Reference2ObjectOpenHashMap<UUID, GestureCameraHolder> GESTURE_CAMS = new Reference2ObjectOpenHashMap<>();

    public static void onConnect(ServerPlayer serverPlayer) {
        // to have a cached model/texture of players
        TextureCache.fetch(serverPlayer.getGameProfile(), image -> {
            MinecraftSkinParser.calculate(image, x -> {});
        });
    }

    public static void onDisconnect(ServerPlayer serverPlayer) {
        var cam = GESTURE_CAMS.get(serverPlayer.getUUID());
        if (cam != null) {
            onStop(cam);
        }
    }

    public static void onStop(ServerPlayer player) {
        var cam = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (cam != null) {
            GestureController.onStop(cam);
        }
    }

    public static void onStop(GestureCameraHolder camera) {
        var player = camera.getPlayer();
        if (!player.hasDisconnected()) {
            PolymerUtils.reloadInventory(player);
            for (ServerGamePacketListenerImpl watchingPlayer : camera.getPlayerModel().getHolder().getWatchingPlayers()) {
                watchingPlayer.send(new ClientboundSetEquipmentPacket(player.getId(), Util.getEquipment(player, false)));
            }

            List<SynchedEntityData.DataValue<?>> data = new ObjectArrayList<>();
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, player.getEntityData().get(EntityTrackedData.FLAGS)));
            camera.getPlayerModel().getHolder().getWatchingPlayers().forEach(p -> camera.getPlayerModel().getHolder().sendPacketDirect(p, new ClientboundSetEntityDataPacket(player.getId(), data)));

            var pmr = new PositionMoveRotation(camera.getOrigin(), Vec3.ZERO, player.getYRot(), player.getXRot());
            var packet = new ClientboundPlayerPositionPacket(player.getId(), pmr, Set.of());
            var p2 = new ClientboundEntityPositionSyncPacket(player.getId(), pmr, true);
            player.connection.send(
                    new ClientboundBundlePacket(ImmutableList.of(
                            new ClientboundSetCameraPacket(player),
                            VirtualEntityUtils.createRidePacket(camera.getCameraId(), IntList.of()),
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode().getId()),
                            packet,
                            p2
                    ))
            );
        }

        camera.destroy();
        GestureController.GESTURE_CAMS.remove(player.getUUID());
    }

    public static void onStart(ServerPlayer player, String animationName) {
        TextureCache.fetch(player.getGameProfile(), image -> {
            MinecraftSkinParser.calculate(image, dataMap -> {
                // destroy any previous gestures
                GestureCameraHolder camera = GestureController.GESTURE_CAMS.get(player.getUUID());
                if (camera != null) {
                    camera.destroy();
                }

                GesturePlayerModelEntity playerModel = EntityRegistry.GESTURE_PLAYER_MODEL.create(player.level(), EntitySpawnReason.EVENT);
                assert playerModel != null;
                playerModel.setup(player, PlayerModelRegistry.getModel(animationName), dataMap);
                playerModel.shouldCheckDistance(false);

                GestureCameraHolder gestureCameraHolder = new GestureCameraHolder(player, playerModel);
                GestureController.GESTURE_CAMS.put(player.getUUID(), gestureCameraHolder);

                ChunkAttachment.ofTicking(gestureCameraHolder, player.level(), player.position());

                // removes model etc when animation finishes
                playerModel.playAnimation(animationName, (unused) -> GestureController.onStop(gestureCameraHolder));

                player.level().addFreshEntity(playerModel);
            });
        });
    }
}
