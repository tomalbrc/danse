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
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setSkin(GameProfile profile) {
        ResolvableProfile profile1 = new ResolvableProfile(profile);

        // todo: parse cmd for each

        for (Bone bone : this.bones) {
            var item = bone.element().getItem();
            item.set(DataComponents.PROFILE, profile1);
            item.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), generateAlphaBools(64*64), List.of(), generateColors(64*64)));
            bone.element().setItem(item);
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


    private static final Random random = new Random();

    public static List<Integer> generateColors(int size) {
        List<Integer> randomList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            randomList.add(getRandomRGBColor());
        }
        return randomList;
    }

    public static List<Boolean> generateAlphaBools(int size) {
        List<Boolean> randomList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            //randomList.add(random.nextBoolean());
            randomList.add(true);
        }
        return randomList;
    }

    public static int getRandomRGBColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return (r << 16) | (g << 8) | b;
    }
}
