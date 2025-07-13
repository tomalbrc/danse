package de.tomalbrc.danse.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.bil.util.Permissions;
import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.ModConfig;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class GestureCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> gestureNode = Commands
                .literal("gesture").requires(Permissions.require("danse.animation", 1).or((s) -> !ModConfig.getInstance().permissionCheck))
                .build();

        dispatcher.getRoot().addChild(gestureNode);

        for (String animation : PlayerModelRegistry.getAnimations()) {
            var name = animation
                    .replace(" ", "-")
                    .replace("(", "")
                    .replace(")", "");
            gestureNode.addChild(literal(name).requires(Permissions.require("danse.animation." + name, 1).or((s) -> !ModConfig.getInstance().permissionCheck)).executes(ctx -> execute(ctx.getSource().getPlayerOrException(), animation)).build());
        }
    }

    private static int execute(ServerPlayer player, String animationName) {
        if ((!player.onGround() && !player.isCreative()) || GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            return 0;
        }

        GestureController.onStart(player, animationName);

        return Command.SINGLE_SUCCESS;
    }
}
