package de.tomalbrc.danse;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.poly.GestureCamera;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import de.tomalbrc.danse.util.CustomModelDataCache;
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
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GestureController {
    public static final Reference2ObjectOpenHashMap<UUID, GestureCamera> GESTURE_CAMS = new Reference2ObjectOpenHashMap<>();

    public static void onConnect(ServerPlayer serverPlayer) {
        // to have a cached "model" of connected players
        new ResolvableProfile(serverPlayer.getGameProfile()).resolve().thenAccept(x -> CustomModelDataCache.fetch(x.gameProfile(), (dataMap) -> {}));
    }

    public static void onDisconnect(ServerPlayer serverPlayer) {
        GESTURE_CAMS.remove(serverPlayer.getUUID());
    }

    public static void onStop(GestureCamera camera) {
        var player = camera.getPlayer();
        if (!player.hasDisconnected()) {
            PolymerUtils.reloadInventory(player);

            List<SynchedEntityData.DataValue<?>> data = new ObjectArrayList<>();
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, player.getEntityData().get(EntityTrackedData.FLAGS)));
            camera.getPlayerModel().getHolder().sendPacket(new ClientboundSetEntityDataPacket(player.getId(), data));

            var packet = new ClientboundPlayerPositionPacket(player.getId(), new PositionMoveRotation(camera.getOrigin().add(0, 0, 0), Vec3.ZERO, player.getYRot() + player.getEyeHeight(), player.getXRot()), Set.of(Relative.X, Relative.Y, Relative.Z));
            player.connection.send(
                    new ClientboundBundlePacket(ImmutableList.of(
                            new ClientboundPlayerAbilitiesPacket(player.getAbilities()),
                            new ClientboundSetCameraPacket(player),
                            VirtualEntityUtils.createRidePacket(camera.getCameraId(), IntList.of()),
                            packet
                    ))
            );
            player.teleportTo(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z);
        }

        camera.destroy();
        GestureController.GESTURE_CAMS.remove(player.getUUID());
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
            playerModel.playAnimation(animationName, () -> GestureController.onStop(gestureCamera));

            player.serverLevel().addFreshEntity(playerModel);
        });
    }
}
