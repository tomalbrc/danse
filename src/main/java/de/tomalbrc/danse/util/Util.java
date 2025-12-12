package de.tomalbrc.danse.util;

import com.mojang.datafixers.util.Pair;
import de.tomalbrc.danse.Danse;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.List;

public class Util {
    public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Danse.MODID, path);
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

    public static Matrix4f compose(@Nullable Vector3fc translation, @Nullable Quaternionfc leftRotation, @Nullable Vector3fc scale, @Nullable Quaternionfc rightRotation) {
        Matrix4f matrix4f = new Matrix4f();
        if (translation != null) {
            matrix4f.translation(translation);
        }

        if (leftRotation != null) {
            matrix4f.rotate(leftRotation);
        }

        if (scale != null) {
            matrix4f.scale(scale);
        }

        if (rightRotation != null) {
            matrix4f.rotate(rightRotation);
        }

        return matrix4f;
    }
}
