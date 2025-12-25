package de.tomalbrc.danse.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.bil.util.Permissions;
import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.ModConfig;
import de.tomalbrc.danse.poly.HudHolder;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class GestureHudCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> gestureNode = Commands
                .literal("emote").requires(Permissions.require("danse.emote", 1).or((s) -> !ModConfig.getInstance().permissionCheck))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    if (player != null) {
                        openHud(player);
                    }
                    return Command.SINGLE_SUCCESS;
                })
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

    public static void openHud(ServerPlayer player) {
        if (!GestureController.GESTURE_CAMS.containsKey(player.getUUID()) && !player.hasContainerOpen()) {
            var hud = new HudHolder(player);
            EntityAttachment.ofTicking(hud, player);
            hud.startWatching(player);
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
