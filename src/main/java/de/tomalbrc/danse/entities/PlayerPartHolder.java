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
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    }

    @Nullable
    protected ItemDisplayElement createBoneDisplay(ResourceLocation modelData) {
        var display = super.createBoneDisplay(modelData);

        if (display == null)
            return null;

        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), generateRandomBooleanList(8 * 8 * 8), List.of(), generateRandomIntegerList(8 * 8 * 8, 0x111111, 0xffffff)));
        itemStack.set(DataComponents.ITEM_MODEL, modelData);
        display.setItem(itemStack);
        return display;
    }


    private static final Random random = new Random();

    public static List<Integer> generateRandomIntegerList(int size, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        if (size <= 0) {
            return new ArrayList<>();
        }
        List<Integer> randomList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int randomNumber = random.nextInt(max - min + 1) + min;
            randomList.add(randomNumber);
        }
        return randomList;
    }

    public static List<Boolean> generateRandomBooleanList(int size) {
        if (size <= 0) {
            return new ArrayList<>();
        }
        List<Boolean> randomList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            randomList.add(random.nextBoolean());
        }
        return randomList;
    }
}
