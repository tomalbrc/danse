package de.tomalbrc.danse.registry;

import com.mojang.authlib.properties.PropertyMap;
import de.tomalbrc.danse.item.StatuePlayerModelItem;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;
import java.util.function.Function;

public class ItemRegistry {
    public static final Object2ObjectLinkedOpenHashMap<ResourceLocation, Item> CUSTOM_ITEMS = new Object2ObjectLinkedOpenHashMap<>();

    public static final Item PLAYER_STATUE = register(Util.id("player_statue"), StatuePlayerModelItem::new, Items.ARMOR_STAND.components().get(DataComponents.ITEM_MODEL));

    public static void register() {
        CreativeModeTab ITEM_GROUP = new CreativeModeTab.Builder(null, -1)
                .title(Component.literal("Danse Items").withStyle(ChatFormatting.DARK_PURPLE))
                .icon(Items.ARMOR_STAND::getDefaultInstance)
                .displayItems((parameters, output) -> CUSTOM_ITEMS.forEach((key, value) -> output.accept(value)))
                .build();

        PolymerItemGroupUtils.registerPolymerItemGroup(Util.id("items"), ITEM_GROUP);
    }

    static public <T extends Item> T register(ResourceLocation identifier, Function<Item.Properties, T> function, ResourceLocation model) {
        var x = function.apply(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON).setId(ResourceKey.create(Registries.ITEM, identifier)).modelId(model).component(DataComponents.PROFILE, new ResolvableProfile(Optional.of("Steve"), Optional.empty(), new PropertyMap())));
        Registry.register(BuiltInRegistries.ITEM, identifier, x);
        CUSTOM_ITEMS.putIfAbsent(identifier, x);
        return x;
    }
}
