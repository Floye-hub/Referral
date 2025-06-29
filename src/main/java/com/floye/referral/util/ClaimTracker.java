package com.floye.referral.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ClaimTracker {
    private static final Path CLAIMS_PATH = FabricLoader.getInstance().getConfigDir().resolve("ref_claims.json");
    private static final File CLAIMS_FILE = CLAIMS_PATH.toFile();
    private static final Gson GSON = new Gson();
    private static final Set<String> CLAIMED_PLAYERS = new HashSet<>();

    // Charger les données au démarrage
    public static void loadClaims() {
        if (CLAIMS_FILE.exists()) {
            try (Reader reader = new FileReader(CLAIMS_FILE)) {
                Set<String> loadedClaims = GSON.fromJson(reader, new TypeToken<Set<String>>(){}.getType());
                if (loadedClaims != null) {
                    CLAIMED_PLAYERS.addAll(loadedClaims);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Sauvegarder les données
    public static void saveClaims() {
        try (Writer writer = new FileWriter(CLAIMS_FILE)) {
            GSON.toJson(CLAIMED_PLAYERS, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Vérifie si un joueur a déjà réclamé un code
    public static boolean hasClaimed(String playerUUID) {
        return CLAIMED_PLAYERS.contains(playerUUID);
    }

    // Marque un joueur comme ayant réclamé un code
    public static void markAsClaimed(String playerUUID) {
        CLAIMED_PLAYERS.add(playerUUID);
        saveClaims();
    }
}