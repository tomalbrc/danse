package de.tomalbrc.danse.poly;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.*;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Node;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.TextureCache;
import eu.pb4.polymer.virtualentity.api.elements.*;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {
    private static final EntityEquipment EMPTY_EQUIPMENT = new EntityEquipment();

    protected InteractionElement hitboxInteraction;
    private EntityEquipment currentEquipment;

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setupHitbox() {
        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.hitboxInteraction.setSize(0.6f, 1.8f);
        this.addElement(this.hitboxInteraction);
    }

    public void setSkinData(Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        this.setSkinData(data, EMPTY_EQUIPMENT);
    }

    public void setSkinData(Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data, EntityEquipment equipment) {
        for (Bone<?> x : this.bones) {
            if (x instanceof PlayerPartHolder.MultipartModelBone bone) {
                MinecraftSkinParser.BodyPart part = MinecraftSkinParser.BodyPart.partFrom(bone.name());
                if (part != MinecraftSkinParser.BodyPart.NONE) {
                    ItemStack item = bone.element().getItem();
                    MinecraftSkinParser.PartData partData = data.get(part);
                    item.set(DataComponents.ITEM_MODEL, part.id(partData.slim() && part.isArm()));

                    item.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelDataInner());
                    bone.element().getDataTracker().set(DisplayTrackedData.Item.ITEM, item, true);

                    ItemStack outerItem = item.copy();
                    outerItem.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelDataOuter());
                    bone.outer.getDataTracker().set(DisplayTrackedData.Item.ITEM, outerItem, true);

                    boolean isBody = part == MinecraftSkinParser.BodyPart.BODY;
                    boolean isLeg = part.isLeg();

                    ItemStack armorItem = item.copy();
                    armorItem.set(DataComponents.CUSTOM_MODEL_DATA, TextureCache.armorCustomModelData(part, equipment.get(isBody ? EquipmentSlot.LEGS : part.getSlot()), true));
                    bone.armor.getDataTracker().set(DisplayTrackedData.Item.ITEM, armorItem, true);

                    ItemStack armorItemOuter = item.copy();
                    armorItemOuter.set(DataComponents.CUSTOM_MODEL_DATA, TextureCache.armorCustomModelData(part, equipment.get(isLeg ? EquipmentSlot.FEET : part.getSlot()), false));
                    bone.armorOuter.getDataTracker().set(DisplayTrackedData.Item.ITEM, armorItemOuter, true);
                }
            }
        }

        for (VirtualElement element : this.getElements()) {
            if (element instanceof DisplayElement displayElement) {
                displayElement.setViewRange(0.5f);
                displayElement.setInvisible(true);
                displayElement.setDisplaySize(5, 5);
                displayElement.setTeleportDuration(2);
                displayElement.setInterpolationDuration(2);
            }
        }
    }

    private void show(Collection<ItemDisplayElement> elements) {
        for (ItemDisplayElement element : elements) {
            if (element.getHolder() == null) addElement(element);
        }
    }
    private void hide(Collection<ItemDisplayElement> elements) {
        for (ItemDisplayElement element : elements) {
            if (element.getHolder() != null) removeElement(element);
        }
    }

    public void setViewRange(float range) {
        for (Bone<?> bone : this.bones) {
            bone.element().setViewRange(range);
        }
    }


    @Override
    public void updateElement(DisplayWrapper<?> display, @Nullable Pose pose) {
        display.element().setYaw(this.parent.getYRot());
        super.updateElement(display, pose);

        if (pose == null) pose = display.getDefaultPose();

        if (display instanceof PlayerPartHolder.MultipartModelBone multipartModelBone && pose != null) {
            var mat = compose(null, pose.readOnlyLeftRotation(), pose.readOnlyScale(), pose.readOnlyRightRotation());
            if (multipartModelBone.outer != null) {
                var mat1 = new Matrix4f(mat);
                mat1.translate(0.0f, offset(multipartModelBone), 0.0f);
                mat1.scale(1.05f);
                mat1.translateLocal(pose.readOnlyTranslation());
                multipartModelBone.outer.setTransformation(mat1);
            }

            if (multipartModelBone.armor != null) {
                var mat1 = new Matrix4f(mat);
                mat1.translate(0.0f, offset(multipartModelBone), 0.0f);
                mat1.scale(1.1f);
                mat1.translateLocal(pose.readOnlyTranslation());
                multipartModelBone.armor.setTransformation(mat1);
            }

            if (multipartModelBone.armorOuter != null) {
                var mat1 = new Matrix4f(mat);
                mat1.translate(0.0f, offsetArmor(multipartModelBone), 0.0f);
                mat1.scale(1.2f);
                mat1.translateLocal(pose.readOnlyTranslation());
                multipartModelBone.armorOuter.setTransformation(mat1);
            }

            for (ItemDisplayElement part : multipartModelBone.additionalElements()) {
                part.setYaw(this.parent.getYRot());
                part.setInterpolationDuration(multipartModelBone.element().getInterpolationDuration());
                part.setTeleportDuration(multipartModelBone.element().getTeleportDuration());
                part.startInterpolationIfDirty();
            }
        }
        else if (pose != null && display.name().equals("hat")) {
            var mat = compose(null, pose.readOnlyLeftRotation(), pose.readOnlyScale(), pose.readOnlyRightRotation());
            mat.translate(0.0f, -0.06f, 0.0f);
            mat.scale(0.6f);
            mat.translateLocal(pose.readOnlyTranslation());
            display.element().setInterpolationDuration(2);
            display.element().setTeleportDuration(2);
            display.element().setTransformation(mat);
            display.element().startInterpolationIfDirty();
        }
    }

    private float offset(MultipartModelBone bone) {
        var leg = bone.part().isLeg();
        var arm = bone.part().isArm();
        var body = bone.part() == MinecraftSkinParser.BodyPart.BODY;
        return leg ? 0.04f : arm ? 0.015f : body ? -0.015f : 0.0f;
    }

    private float offsetArmor(MultipartModelBone bone) {
        var leg = bone.part().isLeg();
        var arm = bone.part().isArm();
        return leg ? 0.1f : arm ? 0.04f : -0.04f;
    }

    private static Matrix4f compose(@Nullable Vector3fc translation, @Nullable Quaternionfc leftRotation, @Nullable Vector3fc scale, @Nullable Quaternionfc rightRotation) {
        Matrix4f matrix4f = new Matrix4f();
        if (translation != null) {
            matrix4f.translation(translation);
        }

        if (leftRotation != null) {
            matrix4f.rotate(leftRotation);
        }

        if (scale != null) {
            matrix4f.scale(scale);
        }

        if (rightRotation != null) {
            matrix4f.rotate(rightRotation);
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
    protected void setupElements(List<Bone<?>> bones) {
        ObjectIterator<Node> nodes = this.model.nodeMap().values().iterator();

        while (nodes.hasNext()) {
            Node node = nodes.next();
            Pose defaultPose = this.model.defaultPose().get(node.uuid());
            MinecraftSkinParser.BodyPart part = MinecraftSkinParser.BodyPart.partFrom(node.name());
            switch (node.type()) {
                case BONE:
                    ItemDisplayElement boneDisplay = this.createBoneDisplay(node.modelData());
                    if (part != MinecraftSkinParser.BodyPart.NONE) {
                        ItemDisplayElement boneDisplayOuter = this.createBoneDisplay(node.modelData());
                        ItemDisplayElement boneDisplayArmor = this.createBoneDisplay(node.modelData());
                        ItemDisplayElement boneDisplayArmorOuter = this.createBoneDisplay(node.modelData());
                        if (boneDisplay != null) {
                            bones.add(MultipartModelBone.of(boneDisplay, boneDisplayOuter, boneDisplayArmor, boneDisplayArmorOuter, node, defaultPose));
                            this.addElement(boneDisplay);
                            this.addElement(boneDisplayOuter);
                            this.addElement(boneDisplayArmor);
                            this.addElement(boneDisplayArmorOuter);
                        }
                    } else {
                        if (boneDisplay != null) {
                            bones.add(ModelBone.of(boneDisplay, node, defaultPose));
                        }
                    }
                    break;
                case ITEM:
                    if (node.displayDataElement() != null) {
                        ItemDisplayElement bone = new ItemDisplayElement((Item) BuiltInRegistries.ITEM.getValue(node.displayDataElement().getItem()));
                        bone.setInterpolationDuration(3);
                        bone.setTeleportDuration(3);
                        bones.add(ItemBone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                    break;
                case BLOCK:
                    if (node.displayDataElement() != null) {
                        BlockDisplayElement bone = new BlockDisplayElement(((Block)BuiltInRegistries.BLOCK.getValue(node.displayDataElement().getBlock())).defaultBlockState());
                        bone.setInterpolationDuration(3);
                        bone.setTeleportDuration(3);
                        bones.add(BlockBone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                    break;
                case TEXT:
                    if (node.displayDataElement() != null) {
                        TextDisplayElement bone = new TextDisplayElement(Component.literal(node.displayDataElement().getText()));
                        bone.setInterpolationDuration(3);
                        bone.setTeleportDuration(3);
                        bones.add(TextBone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                    break;
                case LOCATOR:
                    this.locatorMap.put(node.name(), Locator.of(node, defaultPose));
            }
        }
    }

    public static class MultipartModelBone extends ModelBone {
        private final ItemDisplayElement outer;
        private final ItemDisplayElement armor;
        private final ItemDisplayElement armorOuter;
        private final MinecraftSkinParser.BodyPart bodyPart;

        protected MultipartModelBone(ItemDisplayElement element, ItemDisplayElement outer, ItemDisplayElement armor, ItemDisplayElement armorOuter, Node node, Pose defaultPose, boolean isHead) {
            super(element, node, defaultPose, isHead);
            this.outer = outer;
            this.armor = armor;
            this.armorOuter = armorOuter;
            this.bodyPart = MinecraftSkinParser.BodyPart.partFrom(node.name());
        }

        public MinecraftSkinParser.BodyPart part() {
            return this.bodyPart;
        }

        public static MultipartModelBone of(ItemDisplayElement inner, ItemDisplayElement outer, ItemDisplayElement armor, ItemDisplayElement armorOuter, @NotNull Node node, Pose defaultPose) {
            Node current = node;

            boolean head;
            for (head = false; current != null; current = current.parent()) {
                if (current.headTag()) {
                    head = true;
                    break;
                }
            }

            return new MultipartModelBone(inner, outer, armor, armorOuter, node, defaultPose, head);
        }

        public ImmutableList<ItemDisplayElement> additionalElements() {
            return ImmutableList.of(this.outer, this.armor, this.armorOuter);
        }

        public ImmutableList<ItemDisplayElement> armorElements() {
            return ImmutableList.of(this.armor, this.armorOuter);
        }
    }
}
