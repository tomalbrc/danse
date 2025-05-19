package de.tomalbrc.danse.poly;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.extra.DisplayElementUpdateListener;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.*;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Node;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.Danse;
import de.tomalbrc.danse.entity.GesturePlayerModelEntity;
import de.tomalbrc.danse.entity.StatuePlayerModelEntity;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import de.tomalbrc.danse.util.TextureCache;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.*;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerPartHolder<T extends StatuePlayerModelEntity & AnimatedEntity> extends SimpleEntityHolder<T> {
    private boolean didFirstRun = false;

    protected InteractionElement hitboxInteraction;

    protected Map<MinecraftSkinParser.BodyPart, DisplayWrapper<ItemDisplayElement>> locatorPartMap = new Object2ObjectArrayMap<>();
    private boolean didSetup;

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setupHitbox() {
        this.hitboxInteraction = InteractionElement.redirect(parent);
        this.hitboxInteraction.setSize(this.dimensions.width(), this.dimensions.height());
        this.hitboxInteraction.ignorePositionUpdates();
        Danse.VIRTUAL_ENTITY_PICK_MAP.put(this.hitboxInteraction.getEntityId(), this.parent::getPickResult);
        this.addElement(this.hitboxInteraction);
    }

    public void setLocatorItem(MinecraftSkinParser.BodyPart part, String name, ItemStack itemStack) {
        if (!locatorPartMap.containsKey(part)) {
            var e = this.addLocatorElement(name, itemStack, part.getContext());
            if (e != null) {
                e.element().setItem(itemStack);
                locatorPartMap.put(part, e);
            }
        } else locatorPartMap.get(part).element().setItem(itemStack);
    }

    public DisplayWrapper<ItemDisplayElement> addLocatorElement(String name, ItemStack stack, ItemDisplayContext context) {
        Locator locator = this.getLocator(name);
        if (this.didSetup && locator != null) {
            ItemDisplayElement element = this.createLocatorItemDisplay(stack, context);
            DisplayWrapper<ItemDisplayElement> display = new DisplayWrapper<>(element, locator, false);
            locator.addListener(new DisplayElementUpdateListener(display));

            this.initializeDisplay(display);
            this.addAdditionalDisplay(element);

            return display;
        }

        return null;
    }

    private ItemDisplayElement createLocatorItemDisplay(ItemStack stack, ItemDisplayContext context) {
        ItemDisplayElement element = new ItemDisplayElement();
        element.setItem(stack.copy());
        element.setItemDisplayContext(context);
        element.setInterpolationDuration(2);
        element.setTeleportDuration(2);
        return element;
    }

    @Override
    protected void updateLocator(Locator locator) {
        if (!isDirty() && this.getAnimator().hasRunningAnimations()) {
            Pose pose = this.animationComponent.findPose(locator);
            if (pose == null) {
                locator.updateListeners(this, locator.getDefaultPose());
            } else {
                locator.updateListeners(this, pose);
            }
        }
    }

    @Override
    protected void onAttachmentRemoved(HolderAttachment oldAttachment) {
        super.onAttachmentRemoved(oldAttachment);
        if (this.hitboxInteraction != null) {
            Danse.VIRTUAL_ENTITY_PICK_MAP.remove(this.hitboxInteraction.getEntityId());
        }
    }

    @Override
    public int[] getDisplayIds() {
        int[] displays = new int[this.bones.length + this.additionalDisplays.size() + (hitboxInteraction == null ? 0 : 1)];

        int index = 0;
        for (Bone<?> bone : this.bones) {
            displays[index++] = bone.element().getEntityId();
        }

        for (DisplayElement element : this.additionalDisplays) {
            displays[index++] = element.getEntityId();
        }

        if (hitboxInteraction != null)
            displays[displays.length - 1] = this.hitboxInteraction.getEntityId();

        return displays;
    }

    public void setSkinData(Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data) {
        for (Bone<?> x : this.bones) {
            if (x instanceof PlayerPartHolder.MultipartModelBone bone) {
                MinecraftSkinParser.BodyPart part = MinecraftSkinParser.BodyPart.partFrom(bone.name());
                if (part != MinecraftSkinParser.BodyPart.NONE) {
                    ItemStack item = bone.element().getItem();
                    MinecraftSkinParser.PartData partData = data.get(part);
                    item.set(DataComponents.ITEM_MODEL, part.modelId(partData.slim() && part.isArm()));

                    item.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelDataInner());
                    bone.element().getDataTracker().set(DisplayTrackedData.Item.ITEM, item, true);

                    ItemStack outerItem = item.copy();
                    outerItem.set(DataComponents.CUSTOM_MODEL_DATA, partData.customModelDataOuter());
                    bone.outer.getDataTracker().set(DisplayTrackedData.Item.ITEM, outerItem, true);
                }
            }
        }

        setDisplayProps();
    }

    public void setEquipment(EntityEquipment equipment) {
        for (Bone<?> x : this.bones) {
            if (x instanceof PlayerPartHolder.MultipartModelBone bone) {
                MinecraftSkinParser.BodyPart part = MinecraftSkinParser.BodyPart.partFrom(bone.name());
                if (part != MinecraftSkinParser.BodyPart.NONE) {
                    ItemStack item = bone.element().getItem().copy();
                    item.set(DataComponents.ITEM_MODEL, part.modelId(false));

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

        setLocatorItem(MinecraftSkinParser.BodyPart.LEFT_ARM, "offhand", equipment.get(EquipmentSlot.OFFHAND));
        setLocatorItem(MinecraftSkinParser.BodyPart.RIGHT_ARM, "mainhand", equipment.get(EquipmentSlot.MAINHAND));

        setDisplayProps();
    }

    private void setDisplayProps() {
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

    protected boolean isDirty() {
        return !didFirstRun;
    }

    @Override
    public void updateElement(DisplayWrapper<?> display, @Nullable Pose pose) {
        if (display instanceof MultipartModelBone multipartModelBone)
            multipartModelBone.setYaw(this.parent.getYRot());
        else
            display.element().setYaw(this.parent.getYRot());

        if (this.hitboxInteraction != null) this.hitboxInteraction.setYaw(this.parent.getYRot());

        super.updateElement(display, pose);

        if (pose == null) {
            if (isDirty()) {
                pose = display.getDefaultPose();
            } else return;
        }

        if (display instanceof PlayerPartHolder.MultipartModelBone multipartModelBone && pose != null) {
            var mat = Util.compose(null, pose.readOnlyLeftRotation(), pose.readOnlyScale(), pose.readOnlyRightRotation());
            var rotation = rotation(multipartModelBone.bodyPart);
            if (rotation != null) {
                var mat1 = new Matrix4f(mat);
                mat1.mul(new Matrix4f().rotate(rotation));
                mat1.translateLocal(pose.readOnlyTranslation());
                display.element().setTransformation(mat1);

                var l = locatorPartMap.get(multipartModelBone.bodyPart);
                if (l != null) {
                    Matrix4f locatorMatrix = new Matrix4f(mat1); // the matrix to rotate
                    locatorMatrix.translate(l.node().transform().origin());
                    locatorMatrix.rotate(l.getDefaultPose().matrix().getNormalizedRotation(new Quaternionf()));
                    locatorMatrix.rotateX(Mth.PI);
                    locatorMatrix.rotateY(Mth.PI);
                    l.element().setTransformation(locatorMatrix);
                    l.element().startInterpolationIfDirty();
                    l.element().setYaw(this.parent.getYRot());
                }
            }

            if (multipartModelBone.outer != null) {
                var mat1 = new Matrix4f(mat);
                if (rotation != null) mat1.mul(new Matrix4f().rotate(rotation));
                mat1.translate(0.0f, offset(multipartModelBone), 0.0f);
                mat1.scale(1.05f);
                mat1.translateLocal(pose.readOnlyTranslation());
                multipartModelBone.outer.setTransformation(mat1);
            }

            if (multipartModelBone.armor != null) {
                var mat1 = new Matrix4f(mat);
                if (rotation != null) mat1.mul(new Matrix4f().rotate(rotation));
                mat1.translate(0.0f, offset(multipartModelBone), 0.0f);
                mat1.scale(1.1f);
                mat1.translateLocal(pose.readOnlyTranslation());
                multipartModelBone.armor.setTransformation(mat1);
            }

            if (multipartModelBone.armorOuter != null) {
                var mat1 = new Matrix4f(mat);
                if (rotation != null) mat1.mul(new Matrix4f().rotate(rotation));
                mat1.translate(0.0f, offsetArmor(multipartModelBone), 0.0f);
                mat1.scale(1.2f);
                mat1.translateLocal(pose.readOnlyTranslation());
                multipartModelBone.armorOuter.setTransformation(mat1);
            }

            for (ItemDisplayElement part : multipartModelBone.additionalElements()) {
                part.setYaw(this.parent.getYRot());
                part.setInterpolationDuration(2);
                part.setTeleportDuration(2);
                part.startInterpolationIfDirty();
            }
        }
    }

    protected Quaternionfc rotation(MinecraftSkinParser.BodyPart part) {
        return null;
    }

    protected float offset(MultipartModelBone bone) {
        var leg = bone.part().isLeg();
        var arm = bone.part().isArm();
        var body = bone.part() == MinecraftSkinParser.BodyPart.BODY;
        return leg ? 0.04f : arm ? 0.015f : body ? -0.015f : 0.0f;
    }

    protected float offsetArmor(MultipartModelBone bone) {
        var leg = bone.part().isLeg();
        var arm = bone.part().isArm();
        return leg ? 0.1f : arm ? 0.04f : -0.04f;
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
    public void setAttachment(@Nullable HolderAttachment attachment) {
        super.setAttachment(attachment);

        if (attachment != null) {
            for (Bone<?> bone : this.bones) {
                if (bone instanceof MultipartModelBone multipartModelBone) {
                    for (ItemDisplayElement element : multipartModelBone.additionalElements()) {
                        addAdditionalDisplay(element);
                    }
                }
            }
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
                        }
                    } else {
                        if (boneDisplay != null) {
                            bones.add(ModelBone.of(boneDisplay, node, defaultPose));
                            this.addElement(boneDisplay);
                        }
                    }
                    break;
                case ITEM:
                    if (node.displayDataElement() != null) {
                        ItemDisplayElement bone = new ItemDisplayElement(BuiltInRegistries.ITEM.getValue(node.displayDataElement().getItem()));
                        bone.setInterpolationDuration(2);
                        bone.setTeleportDuration(2);
                        bones.add(ItemBone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                    break;
                case BLOCK:
                    if (node.displayDataElement() != null) {
                        BlockDisplayElement bone = new BlockDisplayElement(BuiltInRegistries.BLOCK.getValue(node.displayDataElement().getBlock()).defaultBlockState());
                        bone.setInterpolationDuration(2);
                        bone.setTeleportDuration(2);
                        bones.add(BlockBone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                    break;
                case TEXT:
                    if (node.displayDataElement() != null) {
                        TextDisplayElement bone = new TextDisplayElement(Component.literal(node.displayDataElement().getText()));
                        bone.setInterpolationDuration(2);
                        bone.setTeleportDuration(2);
                        bones.add(TextBone.of(bone, node, defaultPose));
                        this.addElement(bone);
                    }
                    break;
                case LOCATOR:
                    this.locatorMap.put(node.name(), Locator.of(node, defaultPose));
            }
        }

        this.didSetup = true;
    }

    @Override
    protected void onAsyncTick() {
        super.onAsyncTick();
        this.didFirstRun = true;
    }

    public static class MultipartModelBone extends ModelBone {
        public final ItemDisplayElement outer;
        public final ItemDisplayElement armor;
        public final ItemDisplayElement armorOuter;
        public final MinecraftSkinParser.BodyPart bodyPart;

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
            return new MultipartModelBone(inner, outer, armor, armorOuter, node, defaultPose, false);
        }

        public ImmutableList<ItemDisplayElement> additionalElements() {
            return ImmutableList.of(element(), this.outer, this.armor, this.armorOuter);
        }

        public void setYaw(float yRot) {
            for (ItemDisplayElement element : this.additionalElements()) {
                element.setYaw(yRot);
            }
        }
    }
}
