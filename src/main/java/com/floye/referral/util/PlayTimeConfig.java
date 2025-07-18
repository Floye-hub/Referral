package com.floye.referral.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayTimeConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("referral/playtime_config.json");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();
    private static final Gson GSON = new Gson();

    private static long minPlayTimeTicks;
    private static long maxPlayTimeTicks;

    // Valeurs par défaut
    private static final long DEFAULT_MIN_PLAY_TIME_MINUTES = 30;
    private static final long DEFAULT_MAX_PLAY_TIME_HOURS = 12;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            createDefaultConfig();
        }

        try (Reader reader = new FileReader(CONFIG_FILE)) {
            JsonObject config = GSON.fromJson(reader, JsonObject.class);
            minPlayTimeTicks = config.get("minPlayTimeMinutes").getAsLong() * 20 * 60; // Convertir en ticks
            maxPlayTimeTicks = config.get("maxPlayTimeHours").getAsLong() * 20 * 60 * 60; // Convertir en ticks
        } catch (Exception e) {
            // En cas d'erreur, utiliser les valeurs par défaut et log l'erreur
            minPlayTimeTicks = DEFAULT_MIN_PLAY_TIME_MINUTES * 20 * 60;
            maxPlayTimeTicks = DEFAULT_MAX_PLAY_TIME_HOURS * 20 * 60 * 60;
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();
        defaultConfig.addProperty("minPlayTimeMinutes", DEFAULT_MIN_PLAY_TIME_MINUTES);
        defaultConfig.addProperty("maxPlayTimeHours", DEFAULT_MAX_PLAY_TIME_HOURS);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(defaultConfig, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getMinPlayTimeTicks() {
        return minPlayTimeTicks;
    }

    public static long getMaxPlayTimeTicks() {
        return maxPlayTimeTicks;
    }

    public static boolean hasValidPlayTime(ServerPlayerEntity player) {
        long playerPlayTime = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        return playerPlayTime >= minPlayTimeTicks && playerPlayTime <= maxPlayTimeTicks;
    }
}