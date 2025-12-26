package de.tomalbrc.danse.poly;

import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.dialogutils.util.TextUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FlatHudHolder extends ElementHolder {
    ServerPlayer player;
    HotbarGui gui;
    int slot = -1;
    int age = 0;

    List<TextDisplayElement> textDisplayElementList = new ArrayList<>();

    public FlatHudHolder(ServerPlayer player) {
        super();

        this.player = player;

        this.gui = new HotbarGui(player) {
            @Override
            public boolean onSelectedSlotChange(int s) {
                slotChanged(s);
                return super.onSelectedSlotChange(s);
            }

            @Override
            public boolean onHandSwing() {
                select();
                return super.onHandSwing();
            }

            @Override
            public void onClose() {
                super.onClose();
                if (getAttachment() != null) destroy();
            }
        };


        for (int i = 0; i < 9; i++) {
            this.gui.setSlot(i, GuiElementBuilder.from(Items.PAPER.getDefaultInstance()).model(Items.AIR.getDefaultInstance().get(DataComponents.ITEM_MODEL)).setItemName(Component.literal(
                    StringUtil.capitalize(PlayerModelRegistry.getAnimations().get(i))
            )));

            TextDisplayElement element = new TextDisplayElement(TextUtil.parse(text(i)));
            element.setOffset(new Vec3(0, player.getEyeHeight(), 0));
            element.setBillboardMode(Display.BillboardConstraints.CENTER);
            element.setTextOpacity((byte) 255);
            element.setTeleportDuration(2);
            element.setInterpolationDuration(2);
            this.addElement(element);

            textDisplayElementList.add(element);
        }
        this.gui.open();
        layout(0);
        slotChanged(0);
    }

    @Override
    protected void onTick() {
        super.onTick();

        if (age > 20 && this.player.isShiftKeyDown() || this.player.getLastClientInput().left() || this.player.getLastClientInput().right() || this.player.getLastClientInput().forward() || this.player.getLastClientInput().backward() || this.player.getLastClientInput().jump()) {
            destroy();
        } else if (age < 20) {
            for (int i = 0; i < this.textDisplayElementList.size(); i++) {
                this.textDisplayElementList.get(i).setOffset(new Vec3(0, player.getEyeHeight(), 0));
            }
        }

        age++;
    }

    protected void layout(int index) {
        final float spacingX = 0.35f;
        final float spacingY = 0.4f;
        final float baseZ = -1.0f;
        final float selectedZ = -0.8f;
        final float scale = 0.35f;

        for (int i = 0; i < this.textDisplayElementList.size(); i++) {
            int col = i % 3;
            int row = i / 3;

            float x = (col - 1) * spacingX;
            float y = (1 - row) * spacingY - 0.12f;
            float z = (i == index) ? selectedZ : baseZ;

            Matrix4f matrix4f = new Matrix4f().translate(new Vector3f(x*Math.abs(z), y*Math.abs(z) - (i==index?0.0345f:0), z));
            matrix4f.scale(scale);

            this.textDisplayElementList.get(i).setTransformation(matrix4f);
            this.textDisplayElementList.get(i).startInterpolationIfDirty();
        }
    }

    protected String text(int index) {
        var entry = String.valueOf((char) (0xE001 + index));
        return "\n<font:danse:gesture>" + entry + "</font>\n\n";
    }

    protected void select() {
        GestureController.onStart(player, PlayerModelRegistry.getAnimations().get(this.slot));
        destroy();
    }

    protected void slotChanged(int s) {
        if (this.slot == s)
            return;

        this.slot = s;
        for (int i = 0; i < this.textDisplayElementList.size(); i++) {
            if (slot == i) {
                this.textDisplayElementList.get(i).setText(TextUtil.parse("<white>" + text(i)));
                this.textDisplayElementList.get(i).setBackground(0xFF_a0efa0);
            } else {
                this.textDisplayElementList.get(i).setText(TextUtil.parse("<gray>" + text(i)));
                this.textDisplayElementList.get(i).setBackground(0xFF_101010);
            }
        }

        layout(slot);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.gui.close();
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        var r = player.player == this.player && super.startWatching(player);
        return r;
    }

    @Override
    public boolean stopWatching(ServerGamePacketListenerImpl player) {
        var r = super.stopWatching(player);
        if (player.player == this.player) {
            destroy();
        }
        return r;
    }
}
