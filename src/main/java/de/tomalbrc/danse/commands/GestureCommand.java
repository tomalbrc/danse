package de.tomalbrc.danse.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class GestureCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> gestureNode = Commands
                .literal("gesture")
                .build();

        dispatcher.getRoot().addChild(gestureNode);

        for (String animation : PlayerModelRegistry.getAnimations()) {
            gestureNode.addChild(literal(animation).executes(ctx -> execute(ctx.getSource().getPlayerOrException(), animation)).build());
        }
    }

    private static int execute(ServerPlayer player, String animationName) {
        if (!player.onGround() && !player.isCreative()) {
            return 0;
        }

        GestureController.onStart(player, animationName);


        return Command.SINGLE_SUCCESS;
    }
}
