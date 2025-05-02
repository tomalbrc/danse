package de.tomalbrc.danse.util;

import com.mojang.datafixers.util.Pair;
import de.tomalbrc.danse.Danse;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Util {
    public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Danse.MODID, path);
    }

    public static List<Pair<EquipmentSlot, ItemStack>> getEquipment(LivingEntity entity, boolean empty) {
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ObjectArrayList<>();

        for (EquipmentSlot slot : SLOTS) {
            if (empty) {
                equipment.add(Pair.of(slot, ItemStack.EMPTY));
            } else {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    equipment.add(Pair.of(slot, stack.copy()));
                }
            }
        }

        return equipment;
    }


}
