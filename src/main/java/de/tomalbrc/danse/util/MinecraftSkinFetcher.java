package de.tomalbrc.danse.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MinecraftSkinFetcher {
    private static final String MOJANG_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Gson gson = new Gson();
    private static final Map<String, BufferedImage> skinCache = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<BufferedImage>> futureCache = new ConcurrentHashMap<>();

    public static void fetchSkin(String texVal, Consumer<BufferedImage> callback) {
        if (skinCache.containsKey(texVal)) {
            callback.accept(skinCache.get(texVal));
            return;
        }

        futureCache.computeIfAbsent(texVal, key ->
                CompletableFuture.supplyAsync(() -> {
                    String base64Data = texVal;
                    String decodedJson = new String(Base64.getDecoder().decode(base64Data));
                    JsonObject textureData = gson.fromJson(decodedJson, JsonObject.class);
                    return textureData.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                }).thenApplyAsync(url -> {
                    if (url != null) {
                        try {
                            return downloadSkin(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }).thenApply(skinData -> {
                    if (skinData != null) {
                        try {
                            var img = ImageIO.read(new ByteArrayInputStream(skinData));
                            if (img != null) {
                                skinCache.put(texVal, img);
                            }
                            futureCache.remove(texVal);
                            return img;

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                })
        ).thenAccept(callback);
    }

    private static byte[] downloadSkin(String skinUrl) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        URL url = new URL(skinUrl);
        try (InputStream in = url.openStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return outputStream.toByteArray();
    }

    private static String fetchJsonFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to fetch data: HTTP " + conn.getResponseCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
