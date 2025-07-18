package com.floye.command;

import com.floye.referral.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import static net.minecraft.server.command.CommandManager.*;

public class RefAdminCommand {

    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> refAdminNode = LiteralArgumentBuilder
                .<ServerCommandSource>literal("refadmin")
                .requires(source -> source.hasPermissionLevel(2)); // Requires OP level 2

        // /refadmin total {playername}
        refAdminNode.then(literal("total")
                .then(argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("The specified player is not valid."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();
                            int totalReferrals = ReferralCounter.getCounter(targetUUID);
                            source.sendFeedback(() -> Text.literal(target.getName().getString() + " has a total of " + totalReferrals + " referral(s)."), true);
                            return 1;
                        })
                )
        );

        // /refadmin reload
        refAdminNode.then(literal("reload")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    CodeManager.loadCodes();
                    ReferralCounter.loadCounters();
                    ClaimTracker.loadClaims();
                    RewardManager.load(); // Reload RewardManager config
                    PlayTimeConfig.loadConfig(); // Reload PlayTimeConfig
                    source.sendFeedback(() -> Text.literal("Configurations reloaded."), true);
                    return 1;
                })
        );

        // /refadmin code {playername}
        refAdminNode.then(literal("code")
                .then(argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("The specified player is not valid."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();
                            String code = CodeManager.getCode(targetUUID);

                            if (code == null) {
                                String cleanUUID = targetUUID.replace("-", "");
                                code = cleanUUID.substring(0, Math.min(8, cleanUUID.length()));
                                CodeManager.setCode(targetUUID, code);
                            }

                            String finalCode = code;
                            source.sendFeedback(() -> Text.literal(target.getName().getString() + "'s code is: " + finalCode), true);
                            return 1;
                        })
                )
        );

        // /refadmin forcereward {playername}
        refAdminNode.then(literal("forcereward")
                .then(argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("The specified player is not valid."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();
                            int currentReferrals = ReferralCounter.getCounter(targetUUID);

                            RewardManager.claimReward(target, currentReferrals + 1);

                            return 1;
                        })
                )
        );

        // /refadmin forcereset {playername}
        refAdminNode.then(literal("forcereward")
                .then(argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("The specified player is not valid."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();

                            // Incrémente le compteur de referrals
                            ReferralCounter.incrementCounter(targetUUID);

                            // Envoie le feedback à l'utilisateur qui a exécuté la commande
                            source.sendFeedback(
                                    () -> Text.literal("You added 1 to the referral counter of "
                                            + target.getName().getString()),
                                    true
                            );

                            return 1;
                        })
                )
        );

        // /refadmin forceset {playername} {amount}
        refAdminNode.then(literal("forceset")
                .then(argument("playername", EntityArgumentType.player())
                        .then(argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    Entity entity = EntityArgumentType.getEntity(context, "playername");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    if (!(entity instanceof ServerPlayerEntity)) {
                                        source.sendError(Text.literal("The specified player is not valid."));
                                        return 0;
                                    }

                                    if (amount < 0) {
                                        source.sendError(Text.literal("The amount must be positive."));
                                        return 0;
                                    }

                                    ServerPlayerEntity target = (ServerPlayerEntity) entity;
                                    String targetUUID = target.getUuid().toString();
                                    ReferralCounter.COUNTERS.put(targetUUID, amount);
                                    ReferralCounter.saveCounters();

                                    source.sendFeedback(() -> Text.literal(target.getName().getString() + "'s referral counter has been set to " + amount + "."), true);
                                    return 1;
                                })
                        )
                )
        );

        dispatcher.register(refAdminNode);
    }
}