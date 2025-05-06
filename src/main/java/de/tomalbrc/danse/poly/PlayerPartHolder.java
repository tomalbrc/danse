package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {
    protected InteractionElement hitboxInteraction;

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setupHitbox() {
        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.hitboxInteraction.setSize(0.6f, 1.8f);
        this.addElement(this.hitboxInteraction);
    }

    public void setSkinData(Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        for (Bone bone : this.bones) {
            var part = MinecraftSkinParser.BodyPart.partFrom(bone.name());
            if (part != MinecraftSkinParser.BodyPart.NONE) {
                var item = bone.element().getItem();
                var partData = data.get(part);
                if (partData.slim() && part.isArm()) {
                    item.set(DataComponents.ITEM_MODEL, part.id(true));
                }
                item.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelData());
                bone.element().setViewRange(0.5f);
                bone.element().setInvisible(true);
                bone.element().setDisplaySize(5, 5);
                bone.element().getDataTracker().set(DisplayTrackedData.Item.ITEM, item, true);
                bone.element().setTeleportDuration(2);
                bone.element().setInterpolationDuration(2);
            }
        }
    }

    public void setViewRange(float range) {
        for (Bone bone : this.bones) {
            bone.element().setViewRange(range);
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
