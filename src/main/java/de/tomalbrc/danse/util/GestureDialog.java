package de.tomalbrc.danse.util;

import de.tomalbrc.danse.Danse;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.dialogutils.DialogUtils;
import de.tomalbrc.dialogutils.util.TextAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.font.BitmapProvider;
import eu.pb4.polymer.resourcepack.extras.api.format.font.FontAsset;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GestureDialog {
    public static Dialog DIALOG;
    static byte[] DEFAULT_ICON;

    static {
        try {
            DEFAULT_ICON = Objects.requireNonNull(Danse.class.getResourceAsStream("/model/danse/helicopter_icon.png")).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add() {
        Map<String, byte[]> filemap = new Object2ObjectOpenHashMap<>();

        int columns = 3;
        int width = 240;

        List<DialogBody> bodies = new ArrayList<>();
        List<DialogEntry> dialogEntries = new ObjectArrayList<>();

        var fontAssetBuilder = FontAsset.builder();
        var animations = PlayerModelRegistry.getAnimations();
        for (int i = 0; i < animations.size(); i++) {
            var animation = animations.get(i);
            var name = animation + "_icon";
            var iconStream = getIcon(name);
            var entry = String.valueOf((char) (0xE001 + i));
            if (iconStream != null) {
                fontAssetBuilder.add(BitmapProvider.builder(Util.id("font/" + name)).height(32).ascent(16).chars(entry));
                filemap.put(AssetPaths.texture(Util.id("font/" + name)), iconStream);
            }

            var iconStr = "<font:danse:gesture>" + entry + "</font>";
            var labelStr = StringUtil.capitalize(animation);
            dialogEntries.add(new DialogEntry(animation, iconStr, labelStr, width));
        }

        var d = fontAssetBuilder.build().toJson().replace("minecraft:", "");
        filemap.put("assets/danse/font/gesture.json", d.getBytes(StandardCharsets.UTF_8));

        var iconSpace = TextAligner.alignSingleLine(" ", TextAligner.Align.CENTER, (width - (33 * columns)) / (columns * 2));

        for (int i = 0; i < dialogEntries.size(); i += columns) {
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            StringBuilder spaceLine = new StringBuilder();

            for (int j = i; j < i + columns && j < dialogEntries.size(); j++) {
                var dialogEntry = dialogEntries.get(j);
                var iconCombined = wrapCmd(dialogEntry.animation, iconSpace + dialogEntry.icon + iconSpace);
                var labels = wrapCmd(dialogEntry.animation, TextAligner.alignSingleLine(dialogEntry.text, TextAligner.Align.CENTER, width / columns));

                line1.append(iconCombined);
                line2.append(labels);

                spaceLine.append(wrapCmd(dialogEntry.animation, TextAligner.alignSingleLine(" ".repeat(8), TextAligner.Align.CENTER, width / columns)));
            }

            var parsed = TextUtil.parse(TextAligner.wrapDefaultFont(spaceLine + "\n" + line1 + "\n" + spaceLine + "\n" + spaceLine + "\n" + line2));
            bodies.add(new PlainMessage(parsed, width));
        }

        var data = new CommonDialogData(Component.literal("Gestures"), Optional.empty(), true, true, DialogAction.CLOSE, bodies, List.of());
        var btn = new ActionButton(new CommonButtonData(Component.literal("Close"), 150), Optional.empty());
        var dialog = new NoticeDialog(data, btn);

        var dialogId = Util.id("gestures");
        DialogUtils.registerDialog(dialogId, dialog);
        DialogUtils.registerQuickDialog(dialogId);

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> filemap.forEach(resourcePackBuilder::addData));

        DIALOG = dialog;
    }

    private static String wrapCmd(String anim, String s) {
        return "<run_cmd:'gesture " + anim + "'>" + s + "</run_cmd>";
    }


    private static byte[] getIcon(String iconName) {
        var is = Danse.class.getResourceAsStream("/model/danse/" + iconName + ".png");
        if (is != null) {
            try {
                return is.readAllBytes();
            } catch (IOException e) {
                return DEFAULT_ICON;
            }
        } else {
            var f = FabricLoader.getInstance().getConfigDir().resolve("danse/" + iconName);
            var file = f.toFile();
            if (file.exists()) {
                try (var stream = new FileInputStream(file)) {
                    return stream.readAllBytes();
                } catch (Exception e) {
                    return DEFAULT_ICON;
                }
            }
        }

        return DEFAULT_ICON;
    }

    private record DialogEntry(String animation, String icon, String text, int width) {

    }
}
