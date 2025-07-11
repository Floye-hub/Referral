package com.floye.referral.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class RewardManager {
    private static final Path REWARDS_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ref_rewards.json");
    private static final Path CLAIMED_REWARDS_PATH = FabricLoader.getInstance().getConfigDir().resolve("ref_claimed_rewards.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Reward> REWARDS = new ArrayList<>();
    private static final Map<String, Set<Integer>> CLAIMED_REWARDS = new HashMap<>();



    public static class Reward {
        public int requiredReferrals;
        public List<String> commands;
        public String message;
        public String item;
        public int slot;
        public String displayName;
        public List<String> lore;
    }

    public static ItemStack getRewardItemStack(Reward reward) {
        Item rewardItem = getRewardItem(reward.item);
        return new ItemStack(rewardItem);
    }

    public static void load() {
        REWARDS.clear();
        CLAIMED_REWARDS.clear();

        // Load rewards configuration
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
            createDefaultRewardsConfig();
        }

        // Load claimed rewards
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

        // Reward for 5 referrals
        Reward reward1 = new Reward();
        reward1.requiredReferrals = 5;
        reward1.commands = Arrays.asList(
                "give @p minecraft:diamond 1",
                "effect give @p minecraft:strength 1200 0"
        );
        reward1.message = "Vous avez reçu 1 diamant et un effet de force pour avoir 5 referrals!";
        reward1.item = "minecraft:diamond";
        reward1.slot = 10;
        reward1.displayName = "Récompense pour 5 parrainages";
        reward1.lore = Arrays.asList("Cliquez pour réclamer", "Nécessite 5 parrainages");
        defaultRewards.add(reward1);

        // Reward for 10 referrals
        Reward reward2 = new Reward();
        reward2.requiredReferrals = 10;
        reward2.commands = Arrays.asList(
                "give @p minecraft:diamond_block 1",
                "give @p minecraft:emerald_block 1",
                "effect give @p minecraft:resistance 1200 0"
        );
        reward2.message = "Vous avez reçu des blocs précieux et un effet de résistance pour 10 referrals!";
        reward2.item = "minecraft:diamond_block";
        reward2.slot = 16;
        reward2.displayName = "Récompense pour 10 parrainages";
        reward2.lore = Arrays.asList("Cliquez pour réclamer", "Nécessite 10 parrainages");
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

    public static void claimReward(ServerPlayerEntity player, int requiredReferrals) {
        String playerUUID = player.getUuid().toString();
        Set<Integer> claimed = CLAIMED_REWARDS.computeIfAbsent(playerUUID, k -> new HashSet<>());

        if (claimed.contains(requiredReferrals)) {
            player.sendMessage(Text.literal("Vous avez déjà réclamé cette récompense!"), false);
            return;
        }

        for (Reward reward : REWARDS) {
            if (reward.requiredReferrals == requiredReferrals) {
                MinecraftServer server = player.getServer();
                if (server == null) return;

                // Execute reward commands
                for (String command : reward.commands) {
                    String processedCommand = command.replace("@p", player.getName().getString());
                    server.getCommandManager().executeWithPrefix(
                            server.getCommandSource().withSilent(),
                            processedCommand
                    );
                }

                // Send reward message
                player.sendMessage(Text.literal(reward.message), false);

                // Mark as claimed
                claimed.add(requiredReferrals);
                saveClaimedRewards();
                return;
            }
        }

        player.sendMessage(Text.literal("Récompense introuvable!"), false);
    }

    public static List<Reward> getRewards() {
        return Collections.unmodifiableList(REWARDS);
    }

    public static Item getRewardItem(String itemId) {
        return Registries.ITEM.get(Identifier.of(itemId));
    }

    public static boolean hasUnclaimedRewards(String playerUUID, int referralCount) {
        Set<Integer> claimed = CLAIMED_REWARDS.getOrDefault(playerUUID, new HashSet<>());
        return REWARDS.stream()
                .anyMatch(reward -> reward.requiredReferrals <= referralCount && !claimed.contains(reward.requiredReferrals));
    }
}