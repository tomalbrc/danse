package de.tomalbrc.danse.poly;

import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.danse.GestureController;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HudHolder extends ElementHolder {
    ServerPlayer player;
    HotbarGui gui;
    int slot = -1;
    int age = 0;

    List<HudPlayerModelHolder> modelAttachments = new ArrayList<>();
    List<TextDisplayElement> textDisplayElementList = new ArrayList<>();
    List<TextDisplayElement> labels = new ArrayList<>();

    TextDisplayElement selection;

    public HudHolder(ServerPlayer player) {
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

            TextDisplayElement element = new TextDisplayElement(TextUtil.parse(iconText(i)));
            element.setBrightness(Brightness.FULL_BRIGHT);
            this.setup(element);
            this.addElement(element);
            this.textDisplayElementList.add(element);

            TextDisplayElement label = new TextDisplayElement(labelText(i));
            label.setBrightness(Brightness.FULL_BRIGHT);
            this.setup(label);
            this.addElement(label);
            this.labels.add(label);

            var renderer = new HudPlayerModelHolder(player, PlayerModelRegistry.getTexturedModel(PlayerModelRegistry.getAnimations().get(i)));
            renderer.setScale(0.075f);
            modelAttachments.add(renderer);
            EntityAttachment.ofTicking(renderer, player);
            renderer.startWatching(player);
        }

        this.selection = new TextDisplayElement();
        this.setup(this.selection);
        this.selection.setText(Component.literal("\n\n\n").append(ComponentAligner.spacer(30)));
        //this.addElement(this.selection);

        this.gui.open();

        this.layout(0);
        this.modelAttachments.forEach(holder -> {
            for (Bone<?> bone : holder.getBones()) {
                bone.element().setTeleportDuration(null, 0);
                bone.element().setInterpolationDuration(null, 0);
                holder.applyPose(null, bone.getDefaultPose(), bone);
            }
            holder.asyncTick();
        });
        this.slotChanged(0);
    }

    protected void setup(TextDisplayElement element) {
        element.setOffset(new Vec3(0, player.getEyeHeight(), 0));
        element.setBillboardMode(Display.BillboardConstraints.CENTER);
        element.setTextOpacity((byte) 255);
        element.setTeleportDuration(2);
        element.setInterpolationDuration(2);
    }

    @Override
    protected void onTick() {
        super.onTick();

        if (player.isRemoved() || (age > 20 && hasInput())) {
            destroy();
        } else if (age < 20) {
            for (TextDisplayElement textDisplayElement : this.textDisplayElementList) {
                textDisplayElement.setOffset(new Vec3(0, player.getEyeHeight(), 0));
            }
            for (var holder : this.modelAttachments) {
                for (VirtualElement element : holder.getElements()) {
                    element.setOffset(new Vec3(0, player.getEyeHeight(), 0));
                }
            }
        }

        if (slot != -1)
            this.selection.setBackground(0xFF_000000 | Color.hslToRgb(((age+120) % 256) / 256., 0.7, 0.5));

        age++;
    }

    protected boolean hasInput() {
        return this.player.isShiftKeyDown() || this.player.getLastClientInput().left() || this.player.getLastClientInput().right() || this.player.getLastClientInput().forward() || this.player.getLastClientInput().backward() || this.player.getLastClientInput().jump();
    }

    protected void layout(int index) {
        var step = 60 / 3;
        var stepV = 60 / 3;

        for (int i = 0; i < this.textDisplayElementList.size(); i++) {
            Matrix4f matrix4f = new Matrix4f().translate(new Vector3f(0, -0.15f, index == i ? -0.8f : -1.0f));
            matrix4f.rotateLocal(new Quaternionf().rotateX(Mth.DEG_TO_RAD * (stepV - (i / 3) * stepV)));
            matrix4f.rotateLocal(new Quaternionf().rotateY(Mth.DEG_TO_RAD * (step - (i % 3) * step)));
            matrix4f.scale(0.35f);

            if (index == i) {
                Matrix4f matrix4f2 = new Matrix4f().translate(new Vector3f(0, -0.15f, -0.81f));
                matrix4f2.rotateLocal(new Quaternionf().rotateX(Mth.DEG_TO_RAD * (stepV - (i / 3) * stepV)));
                matrix4f2.rotateLocal(new Quaternionf().rotateY(Mth.DEG_TO_RAD * (step - (i % 3) * step)));
                matrix4f2.scale(0.35f);

                this.selection.setTransformation(matrix4f2);
                this.selection.startInterpolationIfDirty();
            }

            HudPlayerModelHolder playerModelHolder = this.modelAttachments.get(i);
            playerModelHolder.mat = matrix4f;
            if (index == i) {
                playerModelHolder.playAnimationLoop(PlayerModelRegistry.getAnimations().get(i));
                playerModelHolder.setActive(true);
            } else {
                playerModelHolder.getModel().animations().keySet().forEach(playerModelHolder.getAnimator()::stopAnimation);
                playerModelHolder.setActive(false);
            }

            this.labels.get(i).setTransformation(matrix4f);
            this.labels.get(i).startInterpolationIfDirty();

            this.textDisplayElementList.get(i).setTransformation(matrix4f);
            this.textDisplayElementList.get(i).startInterpolationIfDirty();
        }
    }

    protected String iconText(int index) {
        var entry = String.valueOf((char) (0xE001 + index));
        return "\n<font:danse:gesture>" + entry + "</font>\n\n";
    }

    protected Component labelText(int index) {
        return ComponentAligner.align(TextUtil.parse(PlayerModelRegistry.getAnimations().get(index)), TextUtil.Alignment.CENTER, 170/2);
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
                this.textDisplayElementList.get(i).setText(TextUtil.parse("<white>" + iconText(i)));
                //this.textDisplayElementList.get(i).setText(Component.empty());
                //this.textDisplayElementList.get(i).setBackground(0x00_000000);
                this.textDisplayElementList.get(i).setTextOpacity((byte)0);
                this.textDisplayElementList.get(i).setBackground(0xFF_80bf80);
                //this.labels.get(i).setBackground(0xff_101010);
                this.labels.get(i).setBackground(0xFF_508f50);
            } else {
                this.textDisplayElementList.get(i).setTextOpacity((byte)255);
                this.textDisplayElementList.get(i).setText(TextUtil.parse("<gray>" + iconText(i)));
                this.textDisplayElementList.get(i).setBackground(0xff_101010);
                //this.labels.get(i).setBackground(0xff_101010);
                this.labels.get(i).setBackground(0xff_202020);
            }
        }

        layout(slot);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.gui.close();
        this.modelAttachments.forEach(ElementHolder::destroy);
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        return player.player == this.player && super.startWatching(player);
    }

    @Override
    public boolean stopWatching(ServerGamePacketListenerImpl player) {
        var r = super.stopWatching(player);
        if (player.player == this.player) {
            destroy();
        }
        return r;
    }

    static class Color {
        public static int hslToRgb(double h, double s, double l) {
            double r, g, b;

            if (s == 0f) {
                r = g = b = l; // achromatic
            } else {
                double q = l < 0.5f ? l * (1 + s) : l + s - l * s;
                double p = 2 * l - q;
                r = hueToRgb(p, q, h + 1f / 3f);
                g = hueToRgb(p, q, h);
                b = hueToRgb(p, q, h - 1f / 3f);
            }
            return to255(r) << 16 | to255(g) << 8 | to255(b);
        }

        public static int to255(double v) {
            return (int) Math.min(255, 256 * v);
        }

        public static double hueToRgb(double p, double q, double t) {
            if (t < 0.)
                t += 1.;
            if (t > 1.)
                t -= 1.;
            if (t < 1. / 6.)
                return p + (q - p) * 6. * t;
            if (t < 1. / 2.)
                return q;
            if (t < 2. / 3.)
                return p + (q - p) * (2. / 3. - t) * 6.;
            return p;
        }
    }

}
