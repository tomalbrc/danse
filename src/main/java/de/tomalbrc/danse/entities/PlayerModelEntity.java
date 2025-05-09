package de.tomalbrc.danse.entities;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.extra.DisplayElementUpdateListener;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.holder.wrapper.Locator;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.mixins.LivingEntityAccessor;
import de.tomalbrc.danse.poly.PlayerPartHolder;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.TextureCache;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class PlayerModelEntity extends Entity implements AnimatedEntity {
    public static final ResourceLocation ID = Util.id("player_model");
    private static final String ANIMATION = "Animation";
    private static final String PLAYER = "Player";
    private static final String PLAYER_UUID = "PlayerUUID";
    protected PlayerPartHolder<?> holder;

    @Nullable
    private String animation = null;
    @Nullable
    private String playerName;
    @Nullable
    private UUID playerUuid;

    public PlayerModelEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public PlayerPartHolder<?> getHolder() {
        return this.holder;
    }

    public void setModel(Model model) {
        this.holder = new PlayerPartHolder<>(this, model);
        this.holder.setupHitbox();
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(ANIMATION)) {
            if (this.holder != null) {
                this.holder.destroy();
            }

            if (this.animation != null && !this.animation.isBlank() && this.holder != null) {
                this.holder.getAnimator().stopAnimation(this.animation);
            }

            this.animation = tag.getString(ANIMATION).orElseThrow();

            if (!this.animation.isBlank()) {
                var model = PlayerModelRegistry.getModel(this.animation);
                if (model != null && model.animations().containsKey(this.animation)) {
                    this.setModel(model);
                    this.holder.getAnimator().playAnimation(this.animation, () -> this.animation = null);
                }
            }
        }

        if (this.holder == null) {
            // get any/first animation
            this.setModel(PlayerModelRegistry.getModel(PlayerModelRegistry.getAnimations().getFirst()));
        }

        if (tag.contains(PLAYER_UUID)) {
            this.playerUuid = tag.read(PLAYER_UUID, UUIDUtil.LENIENT_CODEC).orElseThrow();

            if (this.getServer() != null) {
                SkullBlockEntity.fetchGameProfile(this.playerUuid).thenAccept(gameProfile -> gameProfile.ifPresent(profile -> TextureCache.fetch(profile, dataMap -> this.holder.setSkinData(dataMap))));
            } else {
                this.discard();
            }
        }
        else if (tag.contains(PLAYER) && !tag.getString(PLAYER).orElseThrow().isBlank()) {
            this.playerName = tag.getString(PLAYER).orElseThrow();

            if (this.getServer() != null && StringUtil.isValidPlayerName(this.playerName)) {
                SkullBlockEntity.fetchGameProfile(this.playerName).thenAccept(gameProfile -> gameProfile.ifPresent(profile -> TextureCache.fetch(profile, dataMap -> this.holder.setSkinData(dataMap))));
            } else {
                this.discard();
            }
        }

        tag.getFloat("Yaw").ifPresent(this::setYRot);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.playerName != null && !this.playerName.isBlank()) {
            tag.putString(PLAYER, this.playerName);
        }

        if (this.playerUuid != null) {
            tag.store(PLAYER_UUID, UUIDUtil.LENIENT_CODEC, this.playerUuid);
        }

        if (this.animation != null && !this.animation.isBlank()) {
            tag.putString(ANIMATION, this.animation);
        }

        tag.putFloat("Yaw", this.getYRot());
    }

    public void setPlayer(ServerPlayer player, Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        this.playerName = player.getScoreboardName();

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            this.addElement("mainhand", mainHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        }

        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty()) {
            this.addElement("offhand", offHand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        }

//        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
//        if (!head.isEmpty() && !(head.has(DataComponents.EQUIPPABLE) && head.get(DataComponents.EQUIPPABLE).assetId().isPresent() && !(head.getItem() instanceof BlockItem))) {
//
//        }

        this.holder.setSkinData(data, ((LivingEntityAccessor)player).getEquipment());
        this.holder.setViewRange(0.5f);
    }

    private ItemDisplayElement addElement(String name, ItemStack stack, ItemDisplayContext context) {
        Locator locator = this.holder.getLocator(name);
        if (locator != null) {
            ItemDisplayElement element = this.makeItemDisplay(stack, context);
            DisplayWrapper<?> display = new DisplayWrapper<>(element, locator, false);
            locator.addListener(new DisplayElementUpdateListener(display));

            this.holder.initializeDisplay(display);
            this.holder.addAdditionalDisplay(element);

            return element;
        }

        return null;
    }

    private ItemDisplayElement makeItemDisplay(ItemStack stack, ItemDisplayContext context) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setItem(stack.copy());
        element.setItemDisplayContext(context);
        element.setInterpolationDuration(2);
        element.setTeleportDuration(2);
        return element;
    }

    @Override
    public int getTeleportDuration() {
        return 2;
    }

    public void playAnimation(String animation, Runnable onFinish) {
        this.animation = animation;
        this.holder.getAnimator().playAnimation(animation, onFinish);
    }

    @Override
    public float getShadowRadius() {
        return this.getBbWidth() * 0.5f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (damageSource.isCreativePlayer())
            this.kill(serverLevel);
        return damageSource.isCreativePlayer();
    }
}

