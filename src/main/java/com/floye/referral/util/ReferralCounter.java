package com.floye.referral.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ReferralCounter {
    // Stocke le nombre de referrals par UUID
    private static final Map<String, Integer> COUNTERS = new HashMap<>();

    // Chemin du fichier de sauvegarde
    private static final Path COUNTERS_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("ref_counters.json");

    private static final Gson GSON = new Gson();

    /**
     * Charge les compteurs depuis le fichier JSON au démarrage
     */
    public static void loadCounters() {
        if (COUNTERS_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(COUNTERS_PATH.toFile())) {
                Type type = new TypeToken<Map<String, Integer>>() {}.getType();
                Map<String, Integer> loadedCounters = GSON.fromJson(reader, type);
                if (loadedCounters != null) {
                    COUNTERS.putAll(loadedCounters);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sauvegarde l'état actuel des compteurs dans le fichier JSON
     */
    public static void saveCounters() {
        try (Writer writer = new FileWriter(COUNTERS_PATH.toFile())) {
            GSON.toJson(COUNTERS, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Incrémente le compteur pour un UUID donné et sauvegarde l'état
     * @param uuid l'identifiant du joueur
     */
    public static void incrementCounter(String uuid) {
        int current = COUNTERS.getOrDefault(uuid, 0);
        COUNTERS.put(uuid, current + 1);
        // Sauvegarde le compteur mis à jour dans le fichier JSON
        saveCounters();
    }

    /**
     * Récupère le compteur pour un UUID donné
     * @param uuid l'identifiant du joueur
     * @return le nombre de referrals
     */
    public static int getCounter(String uuid) {
        return COUNTERS.getOrDefault(uuid, 0);
    }
}