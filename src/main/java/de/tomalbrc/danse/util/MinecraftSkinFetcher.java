package de.tomalbrc.danse.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MinecraftSkinFetcher {
    private static final Gson gson = new Gson();
    private static final Map<String, BufferedImage> CACHED_SKINS = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<BufferedImage>> FUTURE_CACHE = new ConcurrentHashMap<>();

    public static void fetchSkin(String base64val, Consumer<BufferedImage> callback) {
        if (CACHED_SKINS.containsKey(base64val)) {
            callback.accept(CACHED_SKINS.get(base64val));
            return;
        }

        FUTURE_CACHE.computeIfAbsent(base64val, key ->
                CompletableFuture.supplyAsync(() -> {
                    String decodedJson = new String(Base64.getDecoder().decode(base64val));
                    JsonObject textureData = gson.fromJson(decodedJson, JsonObject.class);
                    return textureData.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                }).thenApplyAsync(url -> {
                    if (url != null) {
                        try {
                            return downloadSkin(url);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }).thenApply(skinData -> {
                    if (skinData != null) {
                        try {
                            var img = ImageIO.read(new ByteArrayInputStream(skinData));
                            if (img != null) {
                                CACHED_SKINS.put(base64val, img);
                            }
                            FUTURE_CACHE.remove(base64val);
                            return img;

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                })
        ).thenAccept(callback);
    }

    public static byte[] downloadSkin(String skinUrl) throws IOException, InterruptedException {
        try (InputStream in = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder().uri(URI.create(skinUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofInputStream()).body();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int r;
            while ((r = in.read(buffer)) > 0) out.write(buffer, 0, r);
            return out.toByteArray();
        }
    }
}
