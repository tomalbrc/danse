package de.tomalbrc.danse.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.poly.PlayerPartHolder;
import de.tomalbrc.danse.poly.StatuePlayerPartHolder;
import de.tomalbrc.danse.registry.ItemRegistry;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.danse.util.TextureCache;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.Rotations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

// ArmorStand-like base
public class StatuePlayerModelEntity extends ArmorStand implements AnimatedEntity {
    public static final ResourceLocation ID = Util.id("player_statue");
    private static final String PLAYER = "Player";
    private static final String PLAYER_UUID = "PlayerUUID";
    protected PlayerPartHolder<?> holder;

    private boolean poseDirty = true;

    @Nullable
    protected String playerName;
    @Nullable
    protected UUID playerUuid;

    public StatuePlayerModelEntity(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public PlayerPartHolder<?> getHolder() {
        return this.holder;
    }

    public void setModel(Model model) {
        if (holder != null)
            holder.destroy();

        this.holder = new StatuePlayerPartHolder<>(this, model);
        this.holder.setupHitbox();
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public int getTeleportDuration() {
        return 2;
    }

    @Override
    public float getShadowRadius() {
        return this.getBbWidth() * 0.5f;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (this.holder == null) {
            // get any/first animation
            setAnyModel();
        }

        if (tag.contains(PLAYER_UUID)) {
            this.playerUuid = tag.read(PLAYER_UUID, UUIDUtil.LENIENT_CODEC).orElseThrow();
        }
        else if (tag.contains(PLAYER) && !tag.getString(PLAYER).orElseThrow().isBlank()) {
            this.playerName = tag.getString(PLAYER).orElseThrow();
        }

        fetchGameProfile(this::setProfile);
        this.holder.setEquipment(this.equipment);
    }

    public void setAnyModel() {
        this.setModel(PlayerModelRegistry.getModel(PlayerModelRegistry.getAnimations().getFirst()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (this.playerName != null && !this.playerName.isBlank()) {
            tag.putString(PLAYER, this.playerName);
        }

        if (this.playerUuid != null) {
            tag.store(PLAYER_UUID, UUIDUtil.LENIENT_CODEC, this.playerUuid);
        }
    }

    public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        super.onEquipItem(equipmentSlot, itemStack, itemStack2);
        if (!this.level().isClientSide() && !this.isSpectator()) {
            this.holder.setEquipment(this.equipment);
        }
    }

    public void setProfile(Optional<GameProfile> profile) {
        profile.ifPresent(gameProfile -> {
            this.playerName = gameProfile.getName();
            this.playerUuid = gameProfile.getId();
        });

        profile.ifPresent(gameProfile -> TextureCache.fetch(gameProfile, dataMap -> {
            this.holder.setSkinData(dataMap);
        }));
    }

    public void fetchGameProfile(Consumer<Optional<GameProfile>> cb) {
        if (this.playerUuid != null) {
            SkullBlockEntity.fetchGameProfile(this.playerUuid).thenAccept(cb);
        }

        if (this.playerName != null && !this.playerName.isBlank()) {
            SkullBlockEntity.fetchGameProfile(this.playerName).thenAccept(cb);
        }
    }

    @Override
    @NotNull
    public ItemStack getPickResult() {
        var stack = ItemRegistry.PLAYER_STATUE.getDefaultInstance();
        stack.set(DataComponents.PROFILE, new ResolvableProfile(
                Optional.ofNullable(this.playerName),
                Optional.ofNullable(this.playerUuid),
                new PropertyMap())
        );
        return stack;
    }

    public void customBrokenByPlayer(ServerLevel serverLevel, DamageSource damageSource) {
        ItemStack itemStack = new ItemStack(ItemRegistry.PLAYER_STATUE);
        itemStack.set(DataComponents.PROFILE, new ResolvableProfile(
                Optional.ofNullable(this.playerName),
                Optional.ofNullable(this.playerUuid),
                new PropertyMap())
        );
        itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        Block.popResource(this.level(), this.blockPosition(), itemStack);
        this.brokenByAnything(serverLevel, damageSource);
    }

    public boolean isPoseDirty() {
        return this.poseDirty || level().getGameTime() - this.lastHit < 5;
    }

    public void setPoseDirty(boolean dirty) {
        this.poseDirty = dirty;
    }

    public void setHeadPose(Rotations rotations) {
        super.setHeadPose(rotations);
        this.poseDirty = true;
    }

    public void setBodyPose(Rotations rotations) {
        super.setBodyPose(rotations);
        this.poseDirty = true;
    }

    public void setLeftArmPose(Rotations rotations) {
        super.setLeftArmPose(rotations);
        this.poseDirty = true;
    }

    public void setRightArmPose(Rotations rotations) {
        super.setRightArmPose(rotations);
        this.poseDirty = true;
    }

    public void setLeftLegPose(Rotations rotations) {
        super.setLeftLegPose(rotations);
        this.poseDirty = true;
    }

    public void setRightLegPose(Rotations rotations) {
        super.setRightLegPose(rotations);
        this.poseDirty = true;
    }
}
