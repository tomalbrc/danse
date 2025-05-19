package de.tomalbrc.danse.entity;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.mixin.LivingEntityAccessor;
import de.tomalbrc.danse.poly.PlayerPartHolder;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Map;

// non-persistent gesture entity
public class GesturePlayerModelEntity extends AnimatedPlayerModelEntity {
    public static final ResourceLocation ID = Util.id("gesture_player_model");

    private ServerPlayer player;
    private boolean checkDistance = true;

    public void setup(ServerPlayer player, Model model, Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        this.player = player;
        this.playerName = player.getScoreboardName();
        this.setPos(player.position());
        this.setYRot(player.getYRot());

        this.setModel(model);
        this.holder.setSkinData(data);
        this.holder.setEquipment(((LivingEntityAccessor)player).getEquipment());
    }

    public GesturePlayerModelEntity(EntityType<? extends AnimatedPlayerModelEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void setModel(Model model) {
        if (this.holder != null)
            holder.destroy();

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
}
