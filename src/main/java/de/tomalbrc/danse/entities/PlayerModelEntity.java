package de.tomalbrc.danse.entities;

import de.tomalbrc.danse.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.data.AjLoader;
import org.provim.nylon.extra.DisplayElementUpdateListener;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.holders.wrappers.Locator;
import org.provim.nylon.model.AjModel;

public class PlayerModelEntity extends Entity implements AjEntity {
    public static final ResourceLocation ID = Util.id("player_model");
    public static final AjModel MODEL = AjLoader.require(ID);
    private static final String ANIMATION = "Animation";
    private static final String PLAYER = "Player";
    private final AjPlayerPartHolder<?> holder;

    @NotNull
    private String animation = "walk";
    @Nullable
    private String playerName;

    @Override
    public AjPlayerPartHolder<?> getHolder() {
        return this.holder;
    }

    public PlayerModelEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);

        this.holder = new AjPlayerPartHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(PLAYER)) {
            this.playerName = tag.getString(PLAYER);
            this.holder.setSkin(this.playerName);
        }

        if (tag.contains(ANIMATION)) {
            this.animation = tag.getString(ANIMATION);
            this.holder.getAnimator().playAnimation(this.animation);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.playerName != null && !this.playerName.isEmpty()) {
            tag.putString(PLAYER, this.playerName);
        }

        if (!this.animation.isEmpty()) {
            tag.putString(ANIMATION, this.animation);
        }
    }

    public void setPlayer(ServerPlayer player) {
        this.playerName = player.getScoreboardName();

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            this.addElement("mainhand", mainHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        }

        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty()) {
            this.addElement("offhand", offHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        }

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty()) {
            this.addElement("hat", head, ItemDisplayContext.HEAD);
        }

        this.holder.setSkin(this.playerName);
    }

    private void addElement(String name, ItemStack stack, ItemDisplayContext context) {
        Locator locator = this.holder.getLocator(name);
        if (locator != null) {
            ItemDisplayElement element = this.makeItemDisplay(stack, context);
            DisplayWrapper<?> display = new DisplayWrapper<>(element, locator, false);
            locator.addListener(new DisplayElementUpdateListener(display));

            this.holder.initializeDisplay(display);
            this.holder.addAdditionalDisplay(element);
        }
    }

    private ItemDisplayElement makeItemDisplay(ItemStack stack, ItemDisplayContext context) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setItem(stack.copy());
        element.setModelTransformation(context);
        element.setInterpolationDuration(2);
        element.getDataTracker().set(DisplayTrackedData.TELEPORTATION_DURATION, 2);
        return element;
    }

    public void setAnimation(String animation) {
        this.animation = animation;
        this.holder.getAnimator().playAnimation(animation);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public float getShadowRadius() {
        return this.getBbWidth() * 0.5f;
    }
}
