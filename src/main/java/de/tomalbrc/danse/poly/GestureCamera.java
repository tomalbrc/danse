package de.tomalbrc.danse.poly;

import com.mojang.math.Axis;
import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GestureCamera extends ElementHolder {
    private final BlockDisplayElement displayElement = new BlockDisplayElement() {
        @Override
        public void notifyMove(Vec3 oldPos, Vec3 newPos, Vec3 delta) {
            var packet = new ClientboundEntityPositionSyncPacket(this.getEntityId(), new PositionMoveRotation(getCurrentPos(), Vec3.ZERO, this.getYaw(), this.getPitch()), true);
            this.getHolder().sendPacket(packet);
        }
    };
    private final ServerPlayer player;
    private final Vec3 origin;
    private final GesturePlayerModelEntity playerModel;

    private float pitch;
    private float yaw;

    public GestureCamera(ServerPlayer player, GesturePlayerModelEntity playerModel) {
        super();

        this.playerModel = playerModel;
        this.origin = player.position();
        this.player = player;
        this.updatePos();
        this.displayElement.setTeleportDuration(2);
        this.addElement(displayElement);
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        if (player.player == this.player) {
            return super.startWatching(player);
        }
        return false;
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        super.startWatchingExtraPackets(player, packetConsumer);
        packetConsumer.accept(VirtualEntityUtils.createRidePacket(displayElement.getEntityId(), IntList.of(player.player.getId())));
        packetConsumer.accept(VirtualEntityUtils.createSetCameraEntityPacket(displayElement.getEntityId()));
        packetConsumer.accept(VirtualEntityUtils.createRidePacket(displayElement.getEntityId(), IntList.of(player.player.getId())));

        GameType.SPECTATOR.updatePlayerAbilities(player.getPlayer().getAbilities());
        packetConsumer.accept(new ClientboundPlayerAbilitiesPacket(player.getPlayer().getAbilities()));
        player.player.setGameMode(player.player.gameMode());

        List<SynchedEntityData.DataValue<?>> data = new ObjectArrayList<>();
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
        packetConsumer.accept(new ClientboundSetEntityDataPacket(player.player.getId(), data));
    }

    @Override
    public void onTick() {
        super.onTick();

        if (this.player == null || this.player.isRemoved() || !GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            this.destroy();
        } else {
            this.updatePos();
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        this.playerModel.discard();
        if (!this.player.hasDisconnected()) {
            this.player.teleportTo(this.origin.x, this.origin.y, this.origin.z);

            List<SynchedEntityData.DataValue<?>> data = new ObjectArrayList<>();
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, player.getEntityData().get(EntityTrackedData.FLAGS)));
            this.player.connection.send(new ClientboundSetEntityDataPacket(this.player.getId(), data));
        }
    }

    private void updatePos() {
        Vector3f rotatedPoint = currentPoint(-4);

        if (this.getAttachment() != null) {
            var level = this.getAttachment().getWorld();

            var eyePos = this.origin.add(0, this.player.getEyeHeight(), 0);
            var blockHitResult = level.clip(new ClipContext(eyePos, new Vec3(rotatedPoint), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            var adjustedPos = currentPoint(-(Math.max(Mth.abs((float) blockHitResult.getLocation().distanceTo(eyePos)) - 0.5f, 0.1f)));
            this.displayElement.setOverridePos(new Vec3(adjustedPos));

            this.displayElement.setPitch(this.pitch);
            this.displayElement.setYaw(this.yaw + 180);
        }
    }

    private Vector3f currentPoint(float dist) {
        Quaternionf xr = Axis.XP.rotationDegrees(this.pitch).normalize();
        Quaternionf yr = Axis.YN.rotationDegrees(this.yaw + 180).normalize();

        Vector3f off = this.origin.toVector3f().add(0, this.player.getEyeHeight(), 0);
        Vector3f rotatedPoint = new Vector3f(0, 0, dist).rotate(yr.mul(xr)).add(off);
        return rotatedPoint;
    }

    public GesturePlayerModelEntity getPlayerModel() {
        return playerModel;
    }
}
