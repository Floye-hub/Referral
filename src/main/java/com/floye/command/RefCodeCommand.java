package com.floye.command;

import com.floye.referral.util.ClaimTracker;
import com.floye.referral.util.CodeManager;
import com.floye.referral.util.RewardManager;
import com.floye.referral.util.ReferralCounter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

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
                                source.sendFeedback(() -> Text.literal("Votre code est : " + finalCode), false);
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

                                        // Vérifie si le joueur a déjà réclamé un code
                                        if (ClaimTracker.hasClaimed(playerUUID)) {
                                            source.sendFeedback(() -> Text.literal("Vous avez déjà réclamé un code !"), false);
                                            return 0;
                                        }

                                        String playercode = StringArgumentType.getString(context, "playercode");

                                        // Trouver le joueur correspondant au code
                                        String recipientUUID = CodeManager.findPlayerUUIDByCode(playercode);
                                        if (recipientUUID == null) {
                                            source.sendFeedback(() -> Text.literal("Code invalide ou non trouvé."), false);
                                            return 0;
                                        }

                                        // Incrémente le compteur de referrals pour le joueur trouvé
                                        ReferralCounter.incrementCounter(recipientUUID);
                                        int total = ReferralCounter.getCounter(recipientUUID);

                                        // Marque le joueur actuel comme ayant réclamé un code
                                        ClaimTracker.markAsClaimed(playerUUID);

                                        source.sendFeedback(() -> Text.literal("Le joueur avec le code " + playercode +
                                                " a maintenant " + total + " referral(s)."), false);
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
                                RewardManager.claimReward(player, totalReferrals);

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
                                source.sendFeedback(() -> Text.literal("Vous avez un total de " + totalReferrals + " referral(s)."), false);
                                return 1;
                            })
                    );

            dispatcher.register(refNode);
        });
    }
}