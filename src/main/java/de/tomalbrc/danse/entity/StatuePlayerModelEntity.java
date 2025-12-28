package de.tomalbrc.danse.entity;

import com.mojang.authlib.GameProfile;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.Danse;
import de.tomalbrc.danse.mixin.ArmorStandInvoker;
import de.tomalbrc.danse.poly.PlayerPartHolder;
import de.tomalbrc.danse.poly.StatuePlayerPartHolder;
import de.tomalbrc.danse.registry.ItemRegistry;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.TextureCache;
import de.tomalbrc.danse.util.Util;
import de.tomalbrc.dialogutils.DialogUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.Rotations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.function.Consumer;

// ArmorStand-like base
public class StatuePlayerModelEntity extends ArmorStand implements AnimatedEntity {
    public static final Identifier ID = Util.id("player_statue");
    private static final String PLAYER = "Player";
    private static final String URL = "URL";
    private static final String PLAYER_UUID = "PlayerUUID";
    protected PlayerPartHolder<?> holder;

    private boolean poseDirty = true;

    @Nullable
    protected String playerName;
    @Nullable
    protected UUID playerUuid;
    @Nullable
    protected String url;

    public StatuePlayerModelEntity(EntityType<? extends @NotNull ArmorStand> entityType, Level level) {
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
    public void readAdditionalSaveData(@NotNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);

        if (this.holder == null) {
            // get any/first animation
            setAnyModel();
        }

        this.playerUuid = valueInput.read(PLAYER_UUID, UUIDUtil.LENIENT_CODEC).orElse(null);
        this.playerName = valueInput.getStringOr(PLAYER, null);
        this.url = valueInput.getStringOr(URL, null);

        if (this.url != null && !this.url.isBlank()) {
            TextureCache.fetch(this.url, this::setTexture);
        } else if (this.playerName != null || this.playerUuid != null) {
            fetchGameProfile(this::setProfile);
            this.holder.setEquipment(this.equipment);
        } else {
            this.setTexture(Danse.STEVE_TEXTURE);
        }
    }

    public void setAnyModel() {
        this.setModel(PlayerModelRegistry.getModel(PlayerModelRegistry.getAnimations().getFirst()));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);

        if (this.playerName != null && !this.playerName.isBlank()) {
            valueOutput.putString(PLAYER, this.playerName);
        }

        if (this.playerUuid != null) {
            valueOutput.store(PLAYER_UUID, UUIDUtil.LENIENT_CODEC, this.playerUuid);
        }
    }

    public void onEquipItem(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        super.onEquipItem(equipmentSlot, itemStack, itemStack2);
        if (!this.level().isClientSide() && !this.isSpectator()) {
            this.holder.setEquipment(this.equipment);
        }
    }

    public void setProfile(GameProfile profile) {
        this.playerName = profile.name();
        this.playerUuid = profile.id();

        TextureCache.fetch(profile, this::setTexture);
    }

    public void setTexture(BufferedImage image) {
        MinecraftSkinParser.calculate(playerUuid, image, data -> this.holder.setSkinData(data));
    }

    public void fetchGameProfile(Consumer<GameProfile> cb) {
        if (this.playerUuid != null) {
            ResolvableProfile.createUnresolved(playerUuid).resolveProfile(DialogUtils.SERVER.services().profileResolver()).thenAccept(cb);
        }

        if (this.playerName != null && !this.playerName.isBlank()) {
            ResolvableProfile.createUnresolved(playerName).resolveProfile(DialogUtils.SERVER.services().profileResolver()).thenAccept(cb);
        }
    }

    @Override
    @NotNull
    public ItemStack getPickResult() {
        var stack = ItemRegistry.PLAYER_STATUE.getDefaultInstance();
        stack.set(DataComponents.PROFILE, this.playerUuid != null ? ResolvableProfile.createUnresolved(this.playerUuid) : ResolvableProfile.createUnresolved(this.playerName));
        return stack;
    }

    public void customBrokenByPlayer(ServerLevel serverLevel, DamageSource damageSource) {
        ItemStack itemStack = new ItemStack(ItemRegistry.PLAYER_STATUE);
        itemStack.set(DataComponents.PROFILE, this.playerUuid != null ? ResolvableProfile.createUnresolved(this.playerUuid) : ResolvableProfile.createUnresolved(this.playerName));

        itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        Block.popResource(this.level(), this.blockPosition(), itemStack);
        ((ArmorStandInvoker)this).invokeBrokenByAnything(serverLevel, damageSource);
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
