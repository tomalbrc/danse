package de.tomalbrc.danse.entities;

import com.mojang.math.Axis;
import de.tomalbrc.danse.Util;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public class GestureCamera extends ArmorStand implements PolymerEntity {
    public static final ResourceLocation ID = Util.id("gesture_camerax");

    private Vec3 origin;
    private ServerPlayer player;
    private Runnable onRemoved;

    private boolean didGlow = false;
    private Collection<MobEffectInstance> effects;

    public static AttributeSupplier.Builder createAttributes() {
        return ArmorStand.createLivingAttributes();
    }

    public void setOnRemoved(Runnable onRemoved) {
        this.onRemoved = onRemoved;
    }

    public GestureCamera(EntityType<? extends ArmorStand> type, Level level) {
        super(type, level);
    }

    public void setPlayer(ServerPlayer player) {
        this.setPos(player.position());
        this.origin = player.position();
        this.player = player;
        player.startRiding(this);
        player.setInvisible(true);
        this.didGlow = player.hasGlowingTag();
        player.setGlowingTag(false);

        this.effects = player.getActiveEffects();
        player.removeAllEffects();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void tick() {
        super.tick();

        setNoGravity(true);
        setInvulnerable(true);

        if (this.player == null || player.isRemoved() || !hasControllingPassenger() || getPassengers().isEmpty()) {
            this.ejectPassengers();

            if (didGlow) player.setGlowingTag(true);
            if (this.effects != null && !this.effects.isEmpty()) this.effects.forEach(e -> player.addEffect(e));

            if (this.onRemoved != null) this.onRemoved.run();
            this.discard();
        } else {
            Quaternionf rot = Axis.XP.rotationDegrees(this.player.getXRot());
            Quaternionf rot2 = Axis.YN.rotationDegrees(this.player.getYRot() + 180.f).normalize();

            Vector3f rotatedPoint = new Vector3f(0, 0, -3).rotate(rot2.mul(rot)).add(origin.toVector3f());

            this.move(MoverType.PLAYER, new Vec3(rotatedPoint.add(0, 0.5f, 0)).subtract(this.position()));
            this.setXRot(player.getXRot());
            this.setYHeadRot(player.getYRot() - 180.f);
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
    public EntityType<?> getPolymerEntityType(ServerPlayer player) {
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
    public boolean hurt(DamageSource damageSource, float f) {
        return false;
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {

    }
}
