package de.tomalbrc.danse.entities;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.holders.entity.simple.SimpleEntityHolder;
import org.provim.nylon.holders.wrappers.Bone;
import org.provim.nylon.holders.wrappers.DisplayWrapper;
import org.provim.nylon.model.AjModel;
import org.provim.nylon.model.AjNode;
import org.provim.nylon.model.AjPose;

import java.util.List;

public class AjPlayerPartHolder<T extends Entity & AjEntity> extends SimpleEntityHolder<T> {
    public static final String SKULL_OWNER = "SkullOwner";
    public static final String CUSTOM_MODEL_DATA = "CustomModelData";
    public static final String[] SPAWN_ORDER = {
            "head", "body", "arm_r", "arm_l", "leg_r", "leg_l"
    };

    public AjPlayerPartHolder(T parent, AjModel model) {
        super(parent, model);
    }

    public void setSkin(String playerName) {
        for (Bone bone : this.bones) {
            bone.element().getItem().getOrCreateTag().putString(SKULL_OWNER, playerName);
        }
    }

    @Override
    public void updateElement(DisplayWrapper<?> display, @Nullable AjPose pose) {
        display.element().setYaw(this.parent.getYRot());
        super.updateElement(display, pose);
    }

    @Override
    public void applyPose(AjPose pose, DisplayWrapper display) {
        super.applyPose(pose, display);

        // fix for player head blocks & the player entities' head
        // also helps to determine which texture UV to apply
        switch (display.node().name()) {
            case "body" -> display.setTranslation(display.getTranslation().sub(0, 1024, 0, new Vector3f()));
            case "arm_r" -> display.setTranslation(display.getTranslation().sub(0, 2 * 1024, 0, new Vector3f()));
            case "arm_l" -> display.setTranslation(display.getTranslation().sub(0, 3 * 1024, 0, new Vector3f()));
            case "leg_r" -> display.setTranslation(display.getTranslation().sub(0, 4 * 1024, 0, new Vector3f()));
            case "leg_l" -> display.setTranslation(display.getTranslation().sub(0, 5 * 1024, 0, new Vector3f()));
            case "hat" -> display.setScale(display.getScale().mul(0.6f, new Vector3f()));
            default -> {
                // do nothing
            }
        }
    }

    @Override
    protected void setupElements(List<Bone> bones) {
        Item rigItem = this.model.projectSettings().rigItem();

        // Order dependent spawning
        for (String name : SPAWN_ORDER) {
            this.addElementNamed(name, rigItem, bones);
        }

        super.setupElements(bones);
    }

    @Override
    protected @Nullable ItemDisplayElement createBone(AjNode node, Item rigItem) {
        // Don't create bones here, we spawn them above in setupElements.
        return null;
    }

    private void addElementNamed(String name, Item rigItem, List<Bone> bones) {
        for (AjNode node : this.model.rig().nodeMap().values()) {
            if (node.name().equals(name)) {
                ItemDisplayElement element = new ItemDisplayElement();
                element.setInterpolationDuration(2);
                element.getDataTracker().set(DisplayTrackedData.TELEPORTATION_DURATION, this.parent.getTeleportDuration());
                element.setModelTransformation(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
                element.setInvisible(true);
                this.addElement(element);

                AjPose defaultPose = this.model.rig().defaultPose().get(node.uuid());
                Bone bone = Bone.of(element, node, defaultPose, false);

                ItemStack itemStack = new ItemStack(rigItem);
                CompoundTag tag = itemStack.getOrCreateTag();

                tag.putInt(CUSTOM_MODEL_DATA, node.customModelData());

                element.setItem(itemStack);
                bones.add(bone);
            }
        }
    }
}
