package de.tomalbrc.danse.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import de.tomalbrc.danse.Danse;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TextureCache {
    private static final Map<CacheKey, CustomModelData> ARMOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<CacheKey, CustomModelData> TRIM_CACHE = new ConcurrentHashMap<>();
    private static List<Integer> PALETTE_KEY;

    public static Map<UUID, BufferedImage> SKINS = new ConcurrentHashMap<>();

    public static void fetch(GameProfile profile, Consumer<BufferedImage> onFinish) {
        if (SKINS.containsKey(profile.id())) {
            onFinish.accept(SKINS.get(profile.id()));
        } else if (profile.properties().containsKey("textures")) {
            var textureBase64 = profile.properties().get("textures").iterator().next().value();
            MinecraftSkinFetcher.fetchSkin(textureBase64, onFinish);
        } else {
            onFinish.accept(Danse.STEVE_TEXTURE);
        }
    }

    public static void fetch(String url, Consumer<BufferedImage> onFinish) {
        MinecraftSkinFetcher.fetchSkinFromURL(url, onFinish);
    }

    public static CustomModelData armorCustomModelData(MinecraftSkinParser.BodyPart part, ItemStack itemStack, boolean inner) {
        if (!itemStack.has(DataComponents.EQUIPPABLE))
            return CustomModelData.EMPTY;

        Equippable eq = itemStack.get(DataComponents.EQUIPPABLE);
        if (eq == null || eq.assetId().isEmpty()) {
            return CustomModelData.EMPTY;
        }

        ResourceLocation loc = eq.assetId().get().location();
        CacheKey key = new CacheKey(loc, part, inner);
        CustomModelData cachedData = ARMOR_CACHE.get(key);
        if (cachedData != null) {
            CustomModelData trimCmd = trim(part, itemStack, inner);

            if (cachedData != CustomModelData.EMPTY && trimCmd != CustomModelData.EMPTY) {
                return merged(new CustomModelData(cachedData.floats(), new BooleanArrayList(cachedData.flags()), cachedData.strings(), new IntArrayList(cachedData.colors())), trimCmd);
            }

            return cachedData;
        }

        EquipmentAsset asset = loadEquipmentAsset(loc);
        if (asset == null)
            return CustomModelData.EMPTY;

        String folder;
        EquipmentAsset.TextureEntry[] entries;
        if (!inner && asset.layers.humanoid != null && asset.layers.humanoid.length > 0) {
            entries = asset.layers.humanoid;
            folder = "humanoid";
        } else {
            entries = asset.layers.humanoidLeggings;
            folder = "humanoid_leggings";
        }

        if (entries == null || entries.length == 0 || entries[0].texture == null) {
            return CustomModelData.EMPTY;
        }

        CustomModelData trimCmd = trim(part, itemStack, inner);

        CustomModelData result = null;
        DyedItemColor dyedColor = itemStack.get(DataComponents.DYED_COLOR);
        for (EquipmentAsset.TextureEntry entry : entries) {
            if (result == null) {
                result = loadArmorTextureData(part, entry, dyedColor, folder);
            } else {
                merged(result, loadArmorTextureData(part, entry, dyedColor, folder));
            }
        }

        if (!itemStack.is(ItemTags.DYEABLE)) ARMOR_CACHE.put(key, result);

        if (result != CustomModelData.EMPTY && trimCmd != CustomModelData.EMPTY) {
            return merged(new CustomModelData(result.floats(), new BooleanArrayList(result.flags()), result.strings(), new IntArrayList(result.colors())), trimCmd);
        }

        return result;
    }

    private static EquipmentAsset loadEquipmentAsset(ResourceLocation loc) {
        String path = String.format("assets/%s/equipment/%s.json", loc.getNamespace(), loc.getPath());
        byte[] jsonBytes = Danse.RESOURCEPACK_BUILDER.getDataOrSource(path);

        if (jsonBytes == null) return null;

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(jsonBytes), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, EquipmentAsset.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse equipment JSON for " + loc, e);
        }
    }

    private static List<Integer> loadPaletteData(ResourceLocation id) {
        String pngPath = String.format("assets/%s/textures/trims/color_palettes/%s.png", id.getNamespace(), id.getPath());
        byte[] pngBytes = Danse.RESOURCEPACK_BUILDER.getDataOrSource(pngPath);
        if (pngBytes == null) {
            return null;
        }

        try (InputStream in = new ByteArrayInputStream(pngBytes)) {
            List<Integer> colors = new IntArrayList();
            BufferedImage img = ImageIO.read(in);
            Raster raster = img.getRaster();
            boolean grayscale = raster.getNumBands() == 1;
            for (int i = 0; i < img.getWidth(); i++) {
                if (grayscale) {
                    int r = raster.getSample(i, 0, 0);
                    int rgba = (0xFF << 24) | (r << 16) | (r << 8) | r;
                    colors.add(rgba);
                } else {
                    int r = raster.getSample(i, 0, 0);
                    int g = raster.getSample(i, 0, 1);
                    int b = raster.getSample(i, 0, 2);
                    int rgba = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    colors.add(rgba);
                }
            }

            return colors;
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading PNG: " + pngPath, e);
        }
    }

    private static CustomModelData loadArmorTextureData(MinecraftSkinParser.BodyPart part, EquipmentAsset.TextureEntry textureEntry, DyedItemColor itemColor, String folder) {
        var id = ResourceLocation.parse(textureEntry.texture);
        String pngPath = String.format("assets/%s/textures/entity/equipment/%s/%s.png", id.getNamespace(), folder, id.getPath());
        byte[] pngBytes = Danse.RESOURCEPACK_BUILDER.getDataOrSource(pngPath);
        if (pngBytes == null) {
            return CustomModelData.EMPTY;
        }

        try (InputStream in = new ByteArrayInputStream(pngBytes)) {
            BufferedImage img = ImageIO.read(in);
            List<Integer> colors = new ArrayList<>();
            List<Boolean> alphas = new ArrayList<>();

            for (Direction dir : MinecraftSkinParser.DIRECTIONS) {
                MinecraftSkinParser.extractTextureRGB(img, MinecraftSkinParser.NOTCH_TEXTURE_MAP, part, MinecraftSkinParser.Layer.INNER, dir, data -> {
                    if (textureEntry.dyeable != null) {
                        int tint = textureEntry.dyeable.colorWhenUndyed;
                        if (itemColor != null) {
                            tint = itemColor.rgb();
                        }

                        colors.add(multiplyRGB(data.color(), tint));
                        alphas.add(data.alpha());
                    } else {
                        colors.add(data.color());
                        alphas.add(data.alpha());
                    }
                });
            }

            return new CustomModelData(ImmutableList.of(), alphas, ImmutableList.of(), colors);
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading PNG: " + pngPath, e);
        }
    }

    private static CustomModelData loadTrimTextureData(MinecraftSkinParser.BodyPart part, ResourceLocation id, String folder) {
        String pngPath = String.format("assets/%s/textures/trims/entity/%s/%s.png", id.getNamespace(), folder, id.getPath());
        byte[] pngBytes = Danse.RESOURCEPACK_BUILDER.getDataOrSource(pngPath);
        if (pngBytes == null) {
            return CustomModelData.EMPTY;
        }

        try (InputStream in = new ByteArrayInputStream(pngBytes)) {
            BufferedImage img = ImageIO.read(in);
            List<Integer> colors = new IntArrayList();
            List<Boolean> alphas = new BooleanArrayList();

            for (Direction dir : MinecraftSkinParser.DIRECTIONS) {
                MinecraftSkinParser.extractTextureRGB(img, MinecraftSkinParser.NOTCH_TEXTURE_MAP, part, MinecraftSkinParser.Layer.INNER, dir, data -> {
                    colors.add(data.color());
                    alphas.add(data.alpha());
                });
            }

            return new CustomModelData(ImmutableList.of(), alphas, ImmutableList.of(), colors);
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading PNG: " + pngPath, e);
        }
    }

    public static CustomModelData trim(MinecraftSkinParser.BodyPart part, ItemStack itemStack, boolean inner) {
        if (!itemStack.has(DataComponents.TRIM))
            return CustomModelData.EMPTY;

        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        assert trim != null;

        var materialId = ResourceLocation.parse(trim.material().getRegisteredName());
        var patternId = trim.pattern().value().assetId();

        CacheKey key = new CacheKey(patternId, part, inner);
        CustomModelData cachedData = TRIM_CACHE.get(key);
        if (cachedData != null) {
            return tinted(new CustomModelData(cachedData.floats(), new BooleanArrayList(cachedData.flags()), cachedData.strings(), new IntArrayList(cachedData.colors())), materialId);
        }

        if (PALETTE_KEY == null) PALETTE_KEY = loadPaletteData(ResourceLocation.withDefaultNamespace("trim_palette"));

        String folder;
        if (inner) {
            folder = "humanoid_leggings";
        } else {
            folder = "humanoid";
        }

        CustomModelData data = loadTrimTextureData(part, patternId, folder);
        TRIM_CACHE.put(key, data);

        return tinted(new CustomModelData(data.floats(), new BooleanArrayList(data.flags()), data.strings(), new IntArrayList(data.colors())), materialId);
    }

    private static CustomModelData tinted(CustomModelData data, ResourceLocation materialId) {
        List<Integer> coloredPalette = loadPaletteData(materialId);
        if (coloredPalette != null) {
            for (int i = 0; i < data.colors().size(); i++) {
                if (data.getBoolean(i) == Boolean.TRUE) {
                    var color = data.getColor(i);
                    var paletteIndex = PALETTE_KEY.indexOf(color);
                    if (paletteIndex != -1) data.colors().set(i, coloredPalette.get(paletteIndex));
                }
            }
        }

        return data;
    }

    private static CustomModelData merged(CustomModelData texture, CustomModelData overlay) {
        if (overlay != CustomModelData.EMPTY) {
            for (int i = 0; i < texture.colors().size(); i++) {
                var armorColor = texture.getColor(i);
                var trimColor = overlay.getColor(i);

                var armorVisible = texture.getBoolean(i);
                var trimVisible = overlay.getBoolean(i);

                if (trimColor != null && trimVisible == Boolean.TRUE)
                    texture.colors().set(i, trimColor);
                else {
                    texture.colors().set(i, armorColor);
                }

                var visible = armorVisible == Boolean.TRUE || trimVisible == Boolean.TRUE;
                if (visible)
                    texture.flags().set(i, true);
            }
        }

        return texture;
    }

    public static int multiplyRGB(int color1, int color2) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (r << 16) | (g << 8) | b;
    }
}
