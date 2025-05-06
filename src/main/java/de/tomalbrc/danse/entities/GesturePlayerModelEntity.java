package de.tomalbrc.danse.entities;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.registries.EntityRegistry;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.CustomModelData;

import java.util.Map;

public class GesturePlayerModelEntity extends PlayerModelEntity {
    private final ServerPlayer player;
    private boolean checkDistance = true;

    public GesturePlayerModelEntity(ServerPlayer player, Model model, Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        super(EntityRegistry.PLAYER_MODEL, player.level());
        this.player = player;
        this.setPos(player.position());
        this.setYRot(player.getYRot());

        this.setModel(model);
        this.setPlayer(player, data); // important to set this *after* model was set
    }

    public void setCheckDistance(boolean checkDistance) {
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
