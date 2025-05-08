package de.tomalbrc.danse.util;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.Direction;
import net.minecraft.world.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TextureCache {
    private static final Map<UUID, Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData>> CACHE = new ConcurrentHashMap<>();
    private static final ImmutableList<Direction> DIRECTIONS = ImmutableList.of(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN);

    public static void fetch(GameProfile profile, Consumer<Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData>> onFinish) {
        var val = CACHE.get(profile.getId());
        if (val != null) {
            onFinish.accept(val);
            return;
        }

        var textureBase64 = profile.getProperties().get("textures").iterator().next().value();
        MinecraftSkinFetcher.fetchSkin(textureBase64, image -> {
            Map<MinecraftSkinParser.BodyPart, MinecraftSkinParser.PartData> data = new Object2ObjectArrayMap<>();
            for (MinecraftSkinParser.BodyPart part : MinecraftSkinParser.BodyPart.values()) {
                List<Integer> colors = new ArrayList<>();
                List<Boolean> alphas = new ArrayList<>();

                for (Direction direction : DIRECTIONS) {
                    MinecraftSkinParser.extractTextureRGB(image, part, MinecraftSkinParser.Layer.INNER, direction, colorData -> {
                        colors.add(colorData.color());
                        alphas.add(colorData.alpha());
                    });
                }

                List<Integer> colorsOuter = new ArrayList<>();
                List<Boolean> alphasOuter = new ArrayList<>();
                if (image.getHeight() > 32) {
                    for (final Direction direction : DIRECTIONS) {
                        MinecraftSkinParser.extractTextureRGB(image, part, MinecraftSkinParser.Layer.OUTER, direction, colorData -> {
                            colorsOuter.add(colorData.color());
                            alphasOuter.add(colorData.alpha());
                        });
                    }
                }

                CustomModelData innerCmd = new CustomModelData(ImmutableList.of(), alphas, ImmutableList.of(), colors);
                CustomModelData outerCmd = new CustomModelData(ImmutableList.of(), alphasOuter, ImmutableList.of(), colorsOuter);
                data.put(part, new MinecraftSkinParser.PartData(innerCmd, outerCmd, MinecraftSkinParser.isSlimSkin(image)));
            }

            CACHE.put(profile.getId(), data);
            onFinish.accept(data);
        });
    }
}
