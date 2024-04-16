package de.tomalbrc.danse.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.danse.Util;
import de.tomalbrc.danse.entities.GestureCamera;
import de.tomalbrc.danse.entities.GesturePlayerModelEntity;
import de.tomalbrc.danse.registries.EntityRegistry;
import de.tomalbrc.danse.registries.PlayerModelRegistry;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

public class GestureCommand {
    public static final Object2IntOpenHashMap<UUID> GESTURES = new Object2IntOpenHashMap<>();

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
        if (!player.onGround()) {
            return 0;
        }

        int id = GESTURES.getInt(player.getUUID());
        Entity entity = id == 0 ? null : player.level().getEntity(id);
        if (entity instanceof GesturePlayerModelEntity playerModel) {
            playerModel.discard();
        }

        GesturePlayerModelEntity playerModel = new GesturePlayerModelEntity(player, PlayerModelRegistry.getModel(animationName), (model) -> {});
        playerModel.setCheckDistance(false);



        GestureCamera gestureCamera = EntityRegistry.GESTURE_CAMERA.create(player.level());
        gestureCamera.setPos(player.position());
        player.level().addFreshEntity(gestureCamera);

        // todo: move to the part where we restore effects etc
        Vec3 pos = player.position();
        var y = player.getYHeadRot();
        var x = player.getXRot();

        gestureCamera.setPlayer(player, () -> {
            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode.getGameModeForPlayer().getId()));

            // Refresh equipment and inventory
            playerModel.getHolder().sendPacket(new ClientboundSetEquipmentPacket(player.getId(), Util.getEquipment(player, false)));
            if (!player.hasDisconnected()) {
                PolymerUtils.reloadInventory(player);

                player.setInvulnerable(false);
                player.setInvisible(false);
                GESTURES.removeInt(player.getUUID());

                player.connection.send(new ClientboundSetCameraPacket(player));
                player.setPos(pos);
                player.setXRot(x);
                player.setYHeadRot(y);
            }
            playerModel.discard();
        });

        playerModel.playAnimation(animationName, () -> {
            player.stopRiding(); // removes model etc when animation finishes
        });

        player.level().addFreshEntity(playerModel);

        // make spayer spectate the camera, on the server they are riding the gesture seat where they left off
        gestureCamera.spectate();

        GESTURES.put(player.getUUID(), playerModel.getId());
        return Command.SINGLE_SUCCESS;
    }
}
