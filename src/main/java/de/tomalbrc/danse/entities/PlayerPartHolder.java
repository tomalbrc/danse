package de.tomalbrc.danse.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.simple.SimpleEntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.danse.util.MinecraftSkinFetcher;
import de.tomalbrc.danse.util.MinecraftSkinParser;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.Direction;
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
import java.util.Map;

public class PlayerPartHolder<T extends Entity & AnimatedEntity> extends SimpleEntityHolder<T> {
    private final Map<MinecraftSkinParser.BodyPart, CustomModelData> data = new Object2ObjectArrayMap<>();

    public PlayerPartHolder(T parent, Model model) {
        super(parent, model);
    }

    public void setSkin(GameProfile profile) {
        ResolvableProfile profile1 = new ResolvableProfile(profile);
        var textureBase64 = profile1.gameProfile().getProperties().get("textures").iterator().next().value();
        MinecraftSkinFetcher.fetchSkin(textureBase64, image -> {

            List<Direction> directions = ImmutableList.of(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN);
            for (MinecraftSkinParser.BodyPart part : MinecraftSkinParser.BodyPart.values()) {
                List<Integer> colors = new ArrayList<>();
                List<Boolean> alphas = new ArrayList<>();

                for (Direction direction : directions) {
                    MinecraftSkinParser.extractTextureRGB(image, part, MinecraftSkinParser.Layer.INNER, direction, colorData -> {
                        colors.add(colorData.color());
                        alphas.add(colorData.alpha());
                    });
                }

                for (Direction direction : directions) {
                    MinecraftSkinParser.extractTextureRGB(image, part, MinecraftSkinParser.Layer.OUTER, direction, colorData -> {
                        colors.add(colorData.color());
                        alphas.add(colorData.alpha());
                    });
                }

                this.data.put(part, new CustomModelData(List.of(), alphas, List.of(), colors));
            }
        });


        for (Bone bone : this.bones) {
            var item = bone.element().getItem();
            item.set(DataComponents.PROFILE, profile1);
            item.set(DataComponents.CUSTOM_MODEL_DATA, this.data.get(MinecraftSkinParser.BodyPart.partFrom(bone.name())));
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
