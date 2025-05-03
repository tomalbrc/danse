package de.tomalbrc.danse.entities;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {
    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setSkinData(Map<MinecraftSkinParser.BodyPart, CustomModelData> data) {
        for (Bone bone : this.bones) {
            var item = bone.element().getItem();
            item.set(DataComponents.CUSTOM_MODEL_DATA, data.get(MinecraftSkinParser.BodyPart.partFrom(bone.name())));
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
}
