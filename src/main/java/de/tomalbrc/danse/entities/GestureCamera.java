package de.tomalbrc.danse.entities;

import com.mojang.math.Axis;
import de.tomalbrc.danse.Util;
import de.tomalbrc.danse.registries.EntityRegistry;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class GestureCamera extends ArmorStand implements PolymerEntity {
    public static final ResourceLocation ID = Util.id("gesture_camerax");

    private ServerPlayer player;

    private Vec3 origin;

    @NotNull
    public static AttributeSupplier.Builder createAttributes() {
        return ArmorStand.createLivingAttributes();
    }


    public GestureCamera(EntityType<? extends ArmorStand> type, Level level) {
        super(type, level);
    }


    GestureSeat seat;

    public void setPlayer(ServerPlayer player, Runnable runnable) {
        if (seat != null)
            seat.discard();

        this.origin = player.position();
        this.player = player;
        this.seat = EntityRegistry.GESTURE_SEAT.create(player.level(), EntitySpawnReason.TRIGGERED);
        assert this.seat != null;
        this.seat.setOnRemoved(runnable);

        player.level().addFreshEntity(seat);

        this.seat.setPlayer(player); // makes the player start ride the seat
    }

    public void spectate() {
        this.player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, GameType.SPECTATOR.getId()));
        this.player.connection.send(new ClientboundSetCameraPacket(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void tick() {
        super.tick();

        setNoGravity(true);
        setInvulnerable(true);

        if (this.player == null || this.player.isRemoved() || !this.player.isPassenger()) {
            this.discard();
        } else {
            Quaternionf rot = Axis.XP.rotationDegrees(this.player.getXRot());
            Quaternionf rot2 = Axis.YN.rotationDegrees(this.player.getYRot() + 180.f).normalize();

            Vector3f rotatedPoint = new Vector3f(0, 0, -4).rotate(rot2.mul(rot)).add(this.origin.toVector3f());

            this.move(MoverType.PLAYER, new Vec3(rotatedPoint.add(0, 0.5f, 0)).subtract(this.position()));
            this.setXRot(this.player.getXRot());
            this.setYHeadRot(this.player.getYRot() - 180.f);
        }
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return false;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.player;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_MARKER | ArmorStand.CLIENT_FLAG_SMALL)));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.POSE, Pose.SLEEPING));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.NO_GRAVITY, true));
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {

    }
}
