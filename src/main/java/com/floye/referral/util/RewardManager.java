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
    private static final Path REWARDS_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("referral/ref_rewards.json");
    private static final Path CLAIMED_REWARDS_PATH = FabricLoader.getInstance().getConfigDir().resolve("referral/ref_claimed_rewards.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String guiTitle = "Referral Rewards"; // Default value
    private static final List<Reward> REWARDS = new ArrayList<>();
    private static final Map<String, Set<Integer>> CLAIMED_REWARDS = new HashMap<>();



    public static class Reward {
        public int requiredReferrals;
        public List<String> commands;
        public String message;
        public String item;
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

        System.out.println("[RewardManager] Starting to load rewards.");

        if (REWARDS_CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(REWARDS_CONFIG_PATH.toFile())) {
                Map<String, Object> config = GSON.fromJson(reader, Map.class);
                guiTitle = (String) config.getOrDefault("guiTitle", "Referral Rewards");

                List<Reward> loadedRewards = GSON.fromJson(GSON.toJson(config.get("rewards")), new TypeToken<List<Reward>>() {}.getType());
                if (loadedRewards != null) {
                    REWARDS.addAll(loadedRewards);
                    REWARDS.sort(Comparator.comparingInt(r -> r.requiredReferrals));
                    System.out.println("[RewardManager] Loaded rewards:");
                    for (Reward reward : REWARDS) {
                        System.out.println("  - " + reward.displayName + " (Item: " + reward.item + ")");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[RewardManager] Configuration file not found. Creating default file.");
            createDefaultRewardsConfig();
        }

        // Load claimed rewards
        if (CLAIMED_REWARDS_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CLAIMED_REWARDS_PATH.toFile())) {
                Type type = new TypeToken<Map<String, Set<Integer>>>() {}.getType();
                Map<String, Set<Integer>> loadedClaims = GSON.fromJson(reader, type);
                if (loadedClaims != null) {
                    CLAIMED_REWARDS.putAll(loadedClaims);
                    System.out.println("[RewardManager] Claimed rewards loaded: " + CLAIMED_REWARDS.size() + " players.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getGuiTitle() {
        return guiTitle;
    }

    private static void createDefaultRewardsConfig() {
        List<Reward> defaultRewards = new ArrayList<>();

        // Add default title
        String guiTitle = "Referral Rewards";

        // Reward for 5 referrals
        Reward reward1 = new Reward();
        reward1.requiredReferrals = 5;
        reward1.commands = Arrays.asList(
                "give @p minecraft:diamond 1",
                "effect give @p minecraft:strength 1200 0"
        );
        reward1.message = "You received 1 diamond and a strength effect for having 5 referrals!";
        reward1.item = "minecraft:diamond";
        reward1.displayName = "Reward for 5 referrals";
        reward1.lore = Arrays.asList("Click to claim", "Requires 5 referrals");
        defaultRewards.add(reward1);

        // Reward for 10 referrals
        Reward reward2 = new Reward();
        reward2.requiredReferrals = 10;
        reward2.commands = Arrays.asList(
                "give @p minecraft:diamond_block 1",
                "give @p minecraft:emerald_block 1",
                "effect give @p minecraft:resistance 1200 0"
        );
        reward2.message = "You received precious blocks and a resistance effect for 10 referrals!";
        reward2.item = "minecraft:diamond_block";
        reward2.displayName = "Reward for 10 referrals";
        reward2.lore = Arrays.asList("Click to claim", "Requires 10 referrals");
        defaultRewards.add(reward2);

        try {
            // Ensure directory exists
            File parentDir = REWARDS_CONFIG_PATH.getParent().toFile();
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    System.err.println("[RewardManager] Failed to create config directory: " + parentDir);
                    return;
                }
            }

            // Write default config
            try (Writer writer = new FileWriter(REWARDS_CONFIG_PATH.toFile())) {
                Map<String, Object> config = new HashMap<>();
                config.put("guiTitle", guiTitle);
                config.put("rewards", defaultRewards);
                GSON.toJson(config, writer);
                System.out.println("[RewardManager] Default config created at: " + REWARDS_CONFIG_PATH);
            }
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
        int currentReferrals = ReferralCounter.getCounter(playerUUID);

        // Check if player has enough referrals
        if (currentReferrals < requiredReferrals) {
            player.sendMessage(Text.literal("You don't have enough referrals for this reward!"), false);
            return;
        }

        Set<Integer> claimed = CLAIMED_REWARDS.computeIfAbsent(playerUUID, k -> new HashSet<>());

        if (claimed.contains(requiredReferrals)) {
            player.sendMessage(Text.literal("You have already claimed this reward!"), false);
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

        player.sendMessage(Text.literal("Reward not found!"), false);
    }

    public static List<Reward> getRewards() {
        return Collections.unmodifiableList(REWARDS);
    }

    public static Item getRewardItem(String itemId) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if (item == null) {
            System.out.println("[RewardManager] Invalid item: " + itemId);
        }
        return item;
    }

    public static boolean hasUnclaimedRewards(String playerUUID, int referralCount) {
        Set<Integer> claimed = CLAIMED_REWARDS.getOrDefault(playerUUID, new HashSet<>());
        return REWARDS.stream()
                .anyMatch(reward -> reward.requiredReferrals <= referralCount && !claimed.contains(reward.requiredReferrals));
    }
}