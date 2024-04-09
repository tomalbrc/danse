package de.tomalbrc.danse.entities;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.extra.DisplayElementUpdateListener;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.holder.wrapper.Locator;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.Util;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
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

public class PlayerModelEntity extends Entity implements AnimatedEntity {
    public static final ResourceLocation ID = Util.id("player_model");
    private static final String ANIMATION = "Animation";
    private static final String PLAYER = "Player";
    private PlayerPartHolder<?> holder;

    @NotNull
    private String animation = "walk";
    @Nullable
    private String playerName;

    @Override
    public PlayerPartHolder<?> getHolder() {
        return this.holder;
    }

    public PlayerModelEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    public void setModel(Model model) {
        this.holder = new PlayerPartHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(ANIMATION)) {
            if (this.animation != tag.getString(ANIMATION) && this.holder != null) {
                this.holder.getAttachment().destroy();
                this.holder.destroy();
            }

            this.animation = tag.getString(ANIMATION);

            this.setModel(PlayerModelRegistry.getModel(this.animation));

            this.holder.getAnimator().playAnimation(this.animation);
        }

        if (this.holder == null) {
            // get any/first animation
            this.setModel(PlayerModelRegistry.getModel(PlayerModelRegistry.getAnimations().get(0)));
        }

        if (tag.contains(PLAYER)) {
            this.playerName = tag.getString(PLAYER);
            this.holder.setSkin(this.playerName);
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
            this.addElement("offhand", offHand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
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

    public void playAnimation(String animation, Runnable onFinish) {
        this.animation = animation;
        this.holder.getAnimator().playAnimation(animation, onFinish);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public float getShadowRadius() {
        return this.getBbWidth() * 0.5f;
    }
}
