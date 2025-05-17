package de.tomalbrc.danse.emotecraft;

import de.tomalbrc.bil.file.bbmodel.BbAnimation;
import de.tomalbrc.bil.file.bbmodel.BbAnimator;
import de.tomalbrc.bil.file.bbmodel.BbKeyframe;
import de.tomalbrc.bil.file.bbmodel.BbModel;
import de.tomalbrc.bil.file.extra.easing.EasingType;
import de.tomalbrc.bil.file.extra.interpolation.Interpolation;
import de.tomalbrc.bil.json.CachedUuidDeserializer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;

import java.util.*;

public class EmoteConverter {
    private static final float TICKS_PER_SECOND = 20f;

    private static class Combined {
        String name;
        float px = 0, py = 0, pz = 0;
        float rx = 0, ry = 0, rz = 0;
        String easing;
        boolean hasPos = false, hasRot = false;
    }

    private static Map<String, UUID> BONE_UUIDS() {
        return Map.of(
                "head", CachedUuidDeserializer.get("fd7c0f39-b83e-d6fa-63ec-59ea1c8a668a"),
                "body", CachedUuidDeserializer.get("2ad9f081-c775-ba36-359b-469065391598"),
                "leftArm", CachedUuidDeserializer.get("787fcc9f-ba12-5313-1639-bdf9ba2e79cf"),
                "rightArm", CachedUuidDeserializer.get("2c3db723-21d8-1f80-412e-e73c14ee4cb3"),
                "leftLeg", CachedUuidDeserializer.get("27b11a0e-fd44-c101-1e12-6caa83bd474a"),
                "rightLeg", CachedUuidDeserializer.get("628e82f7-9a35-9267-39cb-ebe43519a7e3")
        );
    }

    public static void convertAndAddTo(EmotecraftAnimationFile src, BbModel model) {
        List<Move> moves = new ArrayList<>(src.emote.moves);
        moves.sort(Comparator.comparingInt(m -> m.tick));

        Map<String, Map<Integer, Combined>> bucket = new HashMap<>();
        for (Move m : moves) {
            int tick = m.tick;
            String easing = m.easing;
            for (String boneKey : BONE_UUIDS().keySet()) {
                PartTransformation pt = switch (boneKey) {
                    case "head" -> m.head;
                    case "body" -> m.body;
                    case "leftArm" -> m.leftArm;
                    case "rightArm" -> m.rightArm;
                    case "leftLeg" -> m.leftLeg;
                    case "rightLeg" -> m.rightLeg;
                    default -> null;
                };
                if (pt == null) continue;

                Combined c = bucket
                        .computeIfAbsent(boneKey, k -> new TreeMap<>())
                        .computeIfAbsent(tick, t -> new Combined());
                c.easing = easing;
                c.name = boneKey;

                if (pt.x != null) {
                    c.px = pt.x;
                    c.hasPos = true;
                }
                if (pt.y != null) {
                    c.py = pt.y;
                    c.hasPos = true;
                }
                if (pt.z != null) {
                    c.pz = pt.z;
                    c.hasPos = true;
                }

                if (pt.pitch != null) {
                    c.rx = pt.pitch;
                    c.hasRot = true;
                }
                if (pt.yaw != null) {
                    c.ry = pt.yaw;
                    c.hasRot = true;
                }
                if (pt.roll != null) {
                    c.rz = pt.roll;
                    c.hasRot = true;
                }
            }
        }

        BbAnimation anim = new BbAnimation();
        anim.uuid = CachedUuidDeserializer.get(UUID.randomUUID().toString());
        anim.name = Optional.ofNullable(src.name).orElse("converted");
        anim.loop = src.emote.isLoop
                ? BbAnimation.LoopMode.LOOP
                : BbAnimation.LoopMode.ONCE;
        anim.override = true;
        anim.length = (src.emote.stopTick - src.emote.beginTick) / TICKS_PER_SECOND;
        anim.snapping = 0.05f;
        anim.startDelay = "0";
        anim.loopDelay = String.valueOf(src.emote.returnTick / TICKS_PER_SECOND);
        anim.animators = new Object2ObjectArrayMap<>();

        for (var entry : bucket.entrySet()) {
            String boneKey = entry.getKey();
            UUID boneId = BONE_UUIDS().get(boneKey);
            Map<Integer, Combined> byTick = entry.getValue();

            BbAnimator animator = anim.animators.computeIfAbsent(boneId, k -> {
                BbAnimator a = new BbAnimator();
                a.keyframes = new ObjectArrayList<>();
                return a;
            });

            for (var tickEntry : byTick.entrySet()) {
                float time = tickEntry.getKey() / TICKS_PER_SECOND;
                Combined c = tickEntry.getValue();

                // pos frame
                if (c.hasPos) {
                    BbKeyframe kf = makeKeyframe("position", time);
                    float x = c.px;
                    float y = c.py;
                    float z = c.pz;
                    addDataPoint(kf, x, y, z);
                    animator.keyframes.add(kf);
                }

                // rot frame
                if (c.hasRot) {
                    BbKeyframe kf = makeKeyframe("rotation", time);
                    float x = ((c.name.equals("body") ? -1.f : 1.f) * c.rx) * Mth.RAD_TO_DEG;
                    float y = c.ry * Mth.RAD_TO_DEG;
                    float z = c.rz * Mth.RAD_TO_DEG;
                    addDataPoint(kf, x, y, z);
                    animator.keyframes.add(kf);
                }
            }
        }

        model.animations.add(anim);
    }

    private static BbKeyframe makeKeyframe(String channel, float time) {
        BbKeyframe kf = new BbKeyframe();
        kf.uuid = CachedUuidDeserializer.get(UUID.randomUUID().toString());
        kf.time = time;
        kf.channel = BbKeyframe.Channel.valueOf(channel.toUpperCase());
        kf.interpolation = Interpolation.LINEAR;
        kf.easing = EasingType.EASE_IN_OUT_QUAD;
        kf.dataPoints = new ObjectArrayList<>();
        return kf;
    }

    private static void addDataPoint(BbKeyframe kf, float x, float y, float z) {
        var dx = new BbKeyframe.DataPointValue();
        dx.setValue(x);
        var dy = new BbKeyframe.DataPointValue();
        dy.setValue(y);
        var dz = new BbKeyframe.DataPointValue();
        dz.setValue(z);
        kf.dataPoints.add(Map.of("x", dx, "y", dy, "z", dz));
    }
}
