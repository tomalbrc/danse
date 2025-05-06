package de.tomalbrc.danse.util;

import com.mojang.datafixers.util.Pair;
import de.tomalbrc.danse.Danse;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Vec3i;
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

    public static Vec3i sizeFor(String name) {
        return switch (name) {
            case "body" -> new Vec3i(8, 12, 4);
            case "leg_r", "leg_l", "arm_r", "arm_l" -> new Vec3i(4, 12, 4);
            case "arm_rs", "arm_ls" -> new Vec3i(3, 12, 4);
            default -> new Vec3i(8, 8, 8);
        };
    }

    public static boolean isArm(String name) {
        return name.equals("arm_r") || name.equals("arm_l");
    }
}
