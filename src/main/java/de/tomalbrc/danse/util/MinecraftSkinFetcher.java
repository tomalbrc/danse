package de.tomalbrc.danse.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import de.tomalbrc.danse.Danse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MinecraftSkinFetcher {
    private static final Gson gson = new Gson();
    private static final Map<String, CompletableFuture<BufferedImage>> FUTURE_CACHE = new ConcurrentHashMap<>();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static void fetchSkinFromEncodedProfile(String base64val, Consumer<MinecraftSkinData> callback) {
        String decodedJson = new String(Base64.getDecoder().decode(base64val));
        JsonObject textureData = gson.fromJson(decodedJson, JsonObject.class);

        JsonObject skin = textureData
                .getAsJsonObject("textures")
                .getAsJsonObject("SKIN");

        String url = skin.get("url").getAsString();

        JsonObject metadata = skin.getAsJsonObject("metadata");

        String modelStr = (metadata != null && metadata.has("model"))
                ? metadata.get("model").getAsString()
                : null;

        MinecraftSkinData.Model model =
                MinecraftSkinData.Model.from(modelStr);

        fetchSkinImageFromUrl(url,
                image -> callback.accept(new MinecraftSkinData(image, model)));
    }

    public static void fetchSkinFromProfile(GameProfile profile, Consumer<MinecraftSkinData> onFinish) {
        if (!profile.properties().containsKey("textures")) {
            onFinish.accept(Danse.STEVE_SKIN);
            return;
        }

        var skin = Danse.SERVER.services().sessionService().getTextures(profile).skin();
        if (skin == null) {
            onFinish.accept(Danse.STEVE_SKIN);
            return;
        }

        var model = MinecraftSkinData.Model.from(skin.getMetadata("model"));
        fetchSkinImageFromUrl(skin.getUrl(), image -> onFinish.accept(new MinecraftSkinData(image, model)));
    }

    public static void fetchSkinFromUrl(String url, Consumer<MinecraftSkinData> onFinish) {
        fetchSkinImageFromUrl(url, image -> onFinish.accept(MinecraftSkinData.from(image)));
    }

    public static void fetchSkinImageFromUrl(String url, Consumer<BufferedImage> onFinish) {
        FUTURE_CACHE.computeIfAbsent(url, x -> CompletableFuture
                .supplyAsync(() -> {
                    try {
                        byte[] skinImageBytes = downloadSkin(url);
                        return ImageIO.read(new ByteArrayInputStream(skinImageBytes));
                    } catch (IOException | InterruptedException e) {
                        throw new CompletionException(e);
                    }
                })
                .whenComplete((result, error) -> {
                    FUTURE_CACHE.remove(url);
                    if (error != null) {
                        Danse.LOGGER.error("Error fetching skin from URL: {}",
                                url, error.getCause() != null ? error.getCause() : error);
                        Danse.SERVER.execute(() -> onFinish.accept(Danse.STEVE_TEXTURE));
                    } else {
                        Danse.SERVER.execute(() -> onFinish.accept(result));
                    }
                })
        );
    }

    public static byte[] downloadSkin(String skinUrl) throws IOException, InterruptedException {
        try (InputStream in = HTTP_CLIENT
                .send(HttpRequest.newBuilder()
                                .uri(URI.create(skinUrl))
                                .timeout(Duration.ofSeconds(10))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofInputStream())
                .body()) {
            return in.readAllBytes();
        }
    }
}
