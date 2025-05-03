package de.tomalbrc.danse.util;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.Direction;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CustomModelDataCache {
    private static final Map<UUID, Map<MinecraftSkinParser.BodyPart, CustomModelData>> CACHE = new ConcurrentHashMap<>();
    private static final List<Direction> DIRECTIONS = ImmutableList.of(Direction.SOUTH, Direction.NORTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN);

    public static void fetch(GameProfile profile, Consumer<Map<MinecraftSkinParser.BodyPart, CustomModelData>> onFinish) {
        var val = CACHE.get(profile.getId());
        if (val != null) {
            onFinish.accept(val);
            return;
        }

        new ResolvableProfile(profile).resolve().thenAccept(x -> {
            var textureBase64 = x.gameProfile().getProperties().get("textures").iterator().next().value();
            MinecraftSkinFetcher.fetchSkin(textureBase64, image -> {
                Map<MinecraftSkinParser.BodyPart, CustomModelData> data = new Object2ObjectArrayMap<>();
                for (MinecraftSkinParser.BodyPart part : MinecraftSkinParser.BodyPart.values()) {
                    List<Integer> colors = new ArrayList<>();
                    List<Boolean> alphas = new ArrayList<>();

                    for (Direction direction : DIRECTIONS) {
                        MinecraftSkinParser.extractTextureRGB(image, part, MinecraftSkinParser.Layer.INNER, direction, colorData -> {
                            colors.add(colorData.color());
                            alphas.add(colorData.alpha());
                        });
                    }

                    for (Direction direction : DIRECTIONS) {
                        MinecraftSkinParser.extractTextureRGB(image, part, MinecraftSkinParser.Layer.OUTER, direction, colorData -> {
                            colors.add(colorData.color());
                            alphas.add(colorData.alpha());
                        });
                    }

                    data.put(part, new CustomModelData(List.of(), alphas, List.of(), colors));
                }

                onFinish.accept(data);
            });
        });

    }
}
