package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {
    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setSkinData(Map<MinecraftSkinParser.BodyPart, CustomModelData> data) {
        for (Bone bone : this.bones) {
            var item = bone.element().getItem();
            item.set(DataComponents.CUSTOM_MODEL_DATA, data.get(MinecraftSkinParser.BodyPart.partFrom(bone.name())));
            bone.element().setItem(item);
            bone.element().setTeleportDuration(2);
            bone.element().setInterpolationDuration(2);
        }
    }


    @Override
    public void updateElement(DisplayWrapper<?> display, @Nullable Pose pose) {
        display.element().setYaw(this.parent.getYRot());
        super.updateElement(display, pose);
    }

    @Nullable
    protected ItemDisplayElement createBoneDisplay(ResourceLocation modelData) {
        var display = super.createBoneDisplay(modelData);

        if (display == null)
            return null;

        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.set(DataComponents.ITEM_MODEL, modelData);
        display.setItem(itemStack);
        return display;
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> consumer) {
        super.startWatchingExtraPackets(player, consumer);

        if (this.parent instanceof GesturePlayerModelEntity gesturePlayerModel) {
            List<SynchedEntityData.DataValue<?>> data = new ObjectArrayList<>();
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
            consumer.accept(new ClientboundSetEntityDataPacket(gesturePlayerModel.getPlayer().getId(), data));
        }
    }
}
