package de.tomalbrc.danse.entities;

import com.mojang.authlib.properties.PropertyMap;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {
    public static final String SKULL_OWNER = "SkullOwner";

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setSkin(String playerName) {
        for (Bone bone : this.bones) {
            bone.element().getItem().set(DataComponents.PROFILE, new ResolvableProfile(Optional.of(playerName), Optional.empty(), new PropertyMap()));
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
}
