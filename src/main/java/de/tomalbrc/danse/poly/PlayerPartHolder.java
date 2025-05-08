package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.holder.wrapper.Locator;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Node;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

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
        for (Bone x : this.bones) {
            if (x instanceof MultipartBone bone) {
                var part = MinecraftSkinParser.BodyPart.partFrom(bone.name());
                if (part != MinecraftSkinParser.BodyPart.NONE) {
                    var item = bone.element().getItem();
                    var partData = data.get(part);
                    if (partData.slim() && part.isArm()) {
                        item.set(DataComponents.ITEM_MODEL, part.id(true));
                    }
                    item.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelDataInner());
                    bone.element().setViewRange(0.5f);
                    bone.element().setInvisible(true);
                    bone.element().setDisplaySize(5, 5);
                    bone.element().getDataTracker().set(DisplayTrackedData.Item.ITEM, item, true);
                    bone.element().setTeleportDuration(2);
                    bone.element().setInterpolationDuration(2);

                    var outerItem = item.copy();
                    outerItem.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelDataOuter());
                    bone.outer.setViewRange(0.5f);
                    bone.outer.setInvisible(true);
                    bone.outer.setDisplaySize(5, 5);
                    bone.outer.getDataTracker().set(DisplayTrackedData.Item.ITEM, outerItem, true);
                    bone.outer.setTeleportDuration(2);
                    bone.outer.setInterpolationDuration(2);
                }
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

        if (display instanceof MultipartBone b && pose != null) {
            var mat = compose(null, pose.readOnlyLeftRotation(), pose.readOnlyScale(), pose.readOnlyRightRotation());
            var leg = display.name().equals("leg_r") || display.name().equals("leg_l") || display.name().equals("body");
            mat.translate(0.0f, leg ? 0.065f : 0.0f, 0.0f);
            mat.scale(1.1f);
            mat.translateLocal(pose.readOnlyTranslation());
            b.outer.setYaw(this.parent.getYRot());

            b.outer.setTransformation(mat);
            b.outer.setInterpolationDuration(b.element().getInterpolationDuration());
            b.outer.setTeleportDuration(b.element().getTeleportDuration());
            b.outer.startInterpolationIfDirty();
        }
    }

    private static Matrix4f compose(@Nullable Vector3fc vector3f, @Nullable Quaternionfc quaternionf, @Nullable Vector3fc vector3f2, @Nullable Quaternionfc quaternionf2) {
        Matrix4f matrix4f = new Matrix4f();
        if (vector3f != null) {
            matrix4f.translation(vector3f);
        }

        if (quaternionf != null) {
            matrix4f.rotate(quaternionf);
        }

        if (vector3f2 != null) {
            matrix4f.scale(vector3f2);
        }

        if (quaternionf2 != null) {
            matrix4f.rotate(quaternionf2);
        }

        return matrix4f;
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

    @Override
    protected void setupElements(List<Bone> bones) {
        ObjectIterator<Node> nodes = this.model.nodeMap().values().iterator();

        while (nodes.hasNext()) {
            Node node = nodes.next();
            Pose defaultPose = this.model.defaultPose().get(node.uuid());
            switch (node.type()) {
                case BONE:
                    ItemDisplayElement boneDisplay = this.createBoneDisplay(node.modelData());
                    ItemDisplayElement boneDisplayOuter = this.createBoneDisplay(node.modelData());
                    if (boneDisplay != null) {
                        bones.add(MultipartBone.of(boneDisplay, boneDisplayOuter, node, defaultPose));
                        this.addElement(boneDisplay);
                        this.addElement(boneDisplayOuter);
                    }
                    break;
                case LOCATOR:
                    this.locatorMap.put(node.name(), Locator.of(node, defaultPose));
            }
        }
    }

    public static class MultipartBone extends Bone {
        private final ItemDisplayElement outer;

        protected MultipartBone(ItemDisplayElement element, ItemDisplayElement outer, Node node, Pose defaultPose, boolean isHead) {
            super(element, node, defaultPose, isHead);
            this.outer = outer;
        }

        public static Bone of(ItemDisplayElement element, ItemDisplayElement outer, @NotNull Node node, Pose defaultPose) {
            Node current = node;

            boolean head;
            for (head = false; current != null; current = current.parent()) {
                if (current.headTag()) {
                    head = true;
                    break;
                }
            }

            return new MultipartBone(element, outer, node, defaultPose, head);
        }
    }
}
