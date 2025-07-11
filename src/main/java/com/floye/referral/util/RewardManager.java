package com.floye.referral.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class RewardManager {
    private static final Path REWARDS_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ref_rewards.json");
    private static final Path CLAIMED_REWARDS_PATH = FabricLoader.getInstance().getConfigDir().resolve("ref_claimed_rewards.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Structure pour stocker les récompenses disponibles
    private static final List<Reward> REWARDS = new ArrayList<>();

    // Structure pour stocker les récompenses déjà réclamées par les joueurs
    private static final Map<String, Set<Integer>> CLAIMED_REWARDS = new HashMap<>();

    public static class Reward {
        public int requiredReferrals;
        public List<String> commands; // Liste de commandes à exécuter
        public String message; // Message à afficher au joueur
    }

    public static void load() {
        REWARDS.clear(); // Nettoyer les récompenses existantes avant rechargement

        // Charger la configuration des récompenses
        if (REWARDS_CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(REWARDS_CONFIG_PATH.toFile())) {
                Type type = new TypeToken<List<Reward>>() {}.getType();
                List<Reward> loadedRewards = GSON.fromJson(reader, type);
                if (loadedRewards != null) {
                    REWARDS.addAll(loadedRewards);
                    REWARDS.sort(Comparator.comparingInt(r -> r.requiredReferrals));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Créer un fichier de configuration par défaut si inexistant
            createDefaultRewardsConfig();
        }

        // Charger les récompenses déjà réclamées
        if (CLAIMED_REWARDS_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CLAIMED_REWARDS_PATH.toFile())) {
                Type type = new TypeToken<Map<String, Set<Integer>>>() {}.getType();
                Map<String, Set<Integer>> loadedClaims = GSON.fromJson(reader, type);
                if (loadedClaims != null) {
                    CLAIMED_REWARDS.putAll(loadedClaims);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createDefaultRewardsConfig() {
        List<Reward> defaultRewards = new ArrayList<>();

        // Récompense pour 5 referrals
        Reward reward1 = new Reward();
        reward1.requiredReferrals = 5;
        reward1.commands = Arrays.asList(
                "give @p minecraft:diamond 1",
                "effect give @p minecraft:strength 1200 0"
        );
        reward1.message = "Vous avez reçu 1 diamant et un effet de force pour avoir 5 referrals!";
        defaultRewards.add(reward1);

        // Récompense pour 10 referrals
        Reward reward2 = new Reward();
        reward2.requiredReferrals = 10;
        reward2.commands = Arrays.asList(
                "give @p minecraft:diamond_block 1",
                "give @p minecraft:emerald_block 1",
                "effect give @p minecraft:resistance 1200 0"
        );
        reward2.message = "Vous avez reçu des blocs précieux et un effet de résistance pour 10 referrals!";
        defaultRewards.add(reward2);

        try (Writer writer = new FileWriter(REWARDS_CONFIG_PATH.toFile())) {
            GSON.toJson(defaultRewards, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveClaimedRewards() {
        try (Writer writer = new FileWriter(CLAIMED_REWARDS_PATH.toFile())) {
            GSON.toJson(CLAIMED_REWARDS, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void claimRewards(ServerCommandSource source, String playerUUID, int currentReferrals) {
        Set<Integer> claimed = CLAIMED_REWARDS.computeIfAbsent(playerUUID, k -> new HashSet<>());
        boolean claimedAny = false;

        for (Reward reward : REWARDS) {
            if (currentReferrals >= reward.requiredReferrals && !claimed.contains(reward.requiredReferrals)) {
                MinecraftServer server = source.getServer();
                ServerCommandSource consoleSource = server.getCommandSource();
                String playerName = source.getName();

                // Exécuter toutes les commandes de la récompense
                for (String command : reward.commands) {
                    String processedCommand = command.replace("@p", playerName);
                    server.getCommandManager().executeWithPrefix(
                            consoleSource.withSilent(),
                            processedCommand
                    );
                }

                // Afficher le message au joueur
                source.sendFeedback(() -> Text.literal(reward.message), false);

                // Marquer comme réclamé
                claimed.add(reward.requiredReferrals);
                claimedAny = true;
            }
        }

        if (claimedAny) {
            saveClaimedRewards();
        } else {
            source.sendFeedback(() -> Text.literal("Vous n'avez aucune récompense à réclamer pour le moment."), false);
        }
    }

    // Méthode utilitaire pour obtenir la liste des récompenses (optionnel)
    public static List<Reward> getRewards() {
        return Collections.unmodifiableList(REWARDS);
    }
}