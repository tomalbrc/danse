package de.tomalbrc.danse.entity;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.mixin.LivingEntityAccessor;
import de.tomalbrc.danse.poly.PlayerPartHolder;
import de.tomalbrc.danse.registry.EntityRegistry;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

// non-persistent gesture entity
public class GesturePlayerModelEntity extends AnimatedPlayerModelEntity {
    private final ServerPlayer player;
    private boolean checkDistance = true;

    public GesturePlayerModelEntity(ServerPlayer player, Model model, Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        super(EntityRegistry.PLAYER_MODEL, player.level());
        this.player = player;
        this.setPos(player.position());
        this.setYRot(player.getYRot());

        this.setModel(model);
        this.setPlayer(player, data);
    }

    @Override
    public void setModel(Model model) {
        assert this.holder == null;
        this.holder = new PlayerPartHolder<>(this, model);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public void shouldCheckDistance(boolean checkDistance) {
        this.checkDistance = checkDistance;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer viewer) {
        super.startSeenByPlayer(viewer);
        viewer.connection.send(new ClientboundSetEquipmentPacket(this.player.getId(), Util.getEquipment(this.player, true)));
    }

    @Override
    public boolean saveAsPassenger(CompoundTag compoundTag) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.player.isRemoved() || this.player.isSpectator() || (this.checkDistance && this.player.distanceToSqr(this) > 1)) {
            this.discard();
        }
    }

    public ServerPlayer getPlayer() {
        return player;
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
}
