package de.tomalbrc.danse.entities;

import com.mojang.authlib.GameProfile;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setSkin(GameProfile profile) {
        ResolvableProfile profile1 = new ResolvableProfile(profile);
        for (Bone bone : this.bones) {
            var item = bone.element().getItem();
            item.set(DataComponents.PROFILE, profile1);
            bone.element().setItem(item);
        }
    }

    @Override
    public void updateElement(DisplayWrapper<?> display, @Nullable Pose pose) {
        display.element().setYaw(this.parent.getYRot());
        super.updateElement(display, pose);
    }

    @Override
    public void applyPose(Pose pose, DisplayWrapper display) {
        super.applyPose(pose, display);

        // apply offsets that help to determine which texture UV to apply inside the shader
        switch (display.node().name()) {
            case "body" -> display.element().setTranslation(display.element().getTranslation().sub(0, 1024, 0, new Vector3f()));
            case "arm_r" -> display.element().setTranslation(display.element().getTranslation().sub(0, 2 * 1024, 0, new Vector3f()));
            case "arm_l" -> display.element().setTranslation(display.element().getTranslation().sub(0, 3 * 1024, 0, new Vector3f()));
            case "leg_r" -> display.element().setTranslation(display.element().getTranslation().sub(0, 4 * 1024, 0, new Vector3f()));
            case "leg_l" -> display.element().setTranslation(display.element().getTranslation().sub(0, 5 * 1024, 0, new Vector3f()));
            case "hat" -> display.element().setScale(display.element().getScale().mul(0.6f, new Vector3f()));
            default -> {}
        }
    }

    @Nullable
    protected ItemDisplayElement createBoneDisplay(ResourceLocation modelData) {
        var display = super.createBoneDisplay(modelData);

        if (display == null)
            return null;

        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
        itemStack.set(DataComponents.ITEM_MODEL, modelData);
        display.setItem(itemStack);
        return display;
    }
}
