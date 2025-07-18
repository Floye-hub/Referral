package com.floye.command;

import com.floye.referral.util.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.stat.Stats;

public class RefCodeCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Noeud principal de la commande /ref
            LiteralArgumentBuilder<ServerCommandSource> refNode = LiteralArgumentBuilder
                    .<ServerCommandSource>literal("ref")

                    // Sous-commande pour afficher le code du joueur
                    .then(LiteralArgumentBuilder
                            .<ServerCommandSource>literal("code")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ServerPlayerEntity player = source.getPlayerOrThrow();
                                String playerUUID = player.getUuid().toString();

                                String code = CodeManager.getCode(playerUUID);
                                if (code == null) {
                                    String cleanUUID = playerUUID.replace("-", "");
                                    code = cleanUUID.substring(0, Math.min(8, cleanUUID.length()));
                                    CodeManager.setCode(playerUUID, code);
                                }

                                String finalCode = code;
                                source.sendFeedback(() -> Text.literal("Your code is: " + finalCode), false);
                                return 1;
                            })
                    )

                    // Sous-commande pour réclamer un referral sur le joueur correspondant au code entré
                    // Sous-commande pour réclamer un referral sur le joueur correspondant au code entré
                    .then(LiteralArgumentBuilder
                            .<ServerCommandSource>literal("claim")
                            .then(RequiredArgumentBuilder
                                    .<ServerCommandSource, String>argument("playercode", StringArgumentType.string())
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        ServerPlayerEntity player = source.getPlayerOrThrow();
                                        String playerUUID = player.getUuid().toString();

                                        // Vérifie le temps de jeu
                                        if (!PlayTimeConfig.hasValidPlayTime(player)) {
                                            source.sendFeedback(() -> Text.literal("You must have played between " + PlayTimeConfig.getMinPlayTimeTicks()/20/60 + " minutes and " + PlayTimeConfig.getMaxPlayTimeTicks()/20/60/60 + " hours to claim a referral code!"), false);
                                            return 0;
                                        }

                                        // Vérifie si le joueur a déjà réclamé un code
                                        if (ClaimTracker.hasClaimed(playerUUID)) {
                                            source.sendFeedback(() -> Text.literal("You have already claimed a code!"), false);
                                            return 0;
                                        }

                                        String playercode = StringArgumentType.getString(context, "playercode");

                                        // Trouver le joueur correspondant au code
                                        String recipientUUID = CodeManager.findPlayerUUIDByCode(playercode);
                                        if (recipientUUID == null) {
                                            source.sendFeedback(() -> Text.literal("Invalid code or not found."), false);
                                            return 0;
                                        }

                                        // Empêcher un joueur de réclamer son propre code
                                        if (playerUUID.equals(recipientUUID)) {
                                            source.sendFeedback(() -> Text.literal("You cannot claim your own code!"), false);
                                            return 0;
                                        }

                                        // Incrémente le compteur de referrals pour le joueur trouvé
                                        ReferralCounter.incrementCounter(recipientUUID);
                                        int total = ReferralCounter.getCounter(recipientUUID);

                                        // Marque le joueur actuel comme ayant réclamé un code
                                        ClaimTracker.markAsClaimed(playerUUID);

                                        source.sendFeedback(() -> Text.literal("The player with code " + playercode +
                                                " now has " + total + " referral(s)."), false);
                                        return 1;
                                    })
                            )
                    )

                    .then(LiteralArgumentBuilder
                            .<ServerCommandSource>literal("rewards")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ServerPlayerEntity player = source.getPlayerOrThrow();
                                String playerUUID = player.getUuid().toString();

                                int totalReferrals = ReferralCounter.getCounter(playerUUID);

                                // Ouvrir le GUI des récompenses
                                new RewardsGUI(player).open();
                                return 1;
                            })
                    )
                    // Sous-commande pour afficher le total de referrals
                    .then(LiteralArgumentBuilder
                            .<ServerCommandSource>literal("total")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ServerPlayerEntity player = source.getPlayerOrThrow();
                                String playerUUID = player.getUuid().toString();

                                // Récupérer le total de referrals
                                int totalReferrals = ReferralCounter.getCounter(playerUUID);

                                // Envoyer le message au joueur
                                source.sendFeedback(() -> Text.literal("You have a total of " + totalReferrals + " referral(s)."), false);
                                return 1;
                            })
                    );

            dispatcher.register(refNode);
        });
    }

    private static boolean hasValidPlayTime(ServerPlayerEntity player) {
        // 30 minutes en ticks (20 ticks/second * 60 seconds/minute * 30 minutes)
        long minPlayTime = 20 * 60 * 30;
        // 12 heures en ticks (20 * 60 * 60 * 12)
        long maxPlayTime = 20 * 60 * 60 * 12;

        long playerPlayTime = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

        return playerPlayTime >= minPlayTime && playerPlayTime <= maxPlayTime;
    }

}