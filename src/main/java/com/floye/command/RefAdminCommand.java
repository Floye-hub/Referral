package com.floye.command;

import com.floye.referral.util.ClaimTracker;
import com.floye.referral.util.CodeManager;
import com.floye.referral.util.ReferralCounter;
import com.floye.referral.util.RewardManager;
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
        refAdminNode.then(LiteralArgumentBuilder.<ServerCommandSource>literal("total")
                .then(RequiredArgumentBuilder.<ServerCommandSource, Entity>argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("Le joueur spécifié n'est pas valide."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();
                            int totalReferrals = ReferralCounter.getCounter(targetUUID);
                            source.sendFeedback(() -> Text.literal(target.getName().getString() + " a un total de " + totalReferrals + " referral(s)."), true);
                            return 1;
                        })
                )
        );

        // /refadmin reload
        refAdminNode.then(LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    CodeManager.loadCodes();
                    ReferralCounter.loadCounters();
                    ClaimTracker.loadClaims();
                    RewardManager.load(); // Reload RewardManager config
                    source.sendFeedback(() -> Text.literal("Configurations rechargées."), true);
                    return 1;
                })
        );

        // /refadmin code {playername}
        refAdminNode.then(LiteralArgumentBuilder.<ServerCommandSource>literal("code")
                .then(RequiredArgumentBuilder.<ServerCommandSource, Entity>argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("Le joueur spécifié n'est pas valide."));
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
                            source.sendFeedback(() -> Text.literal("Le code de " + target.getName().getString() + " est : " + finalCode), true);
                            return 1;
                        })
                )
        );

        // /refadmin forcereward {playername}
        refAdminNode.then(LiteralArgumentBuilder.<ServerCommandSource>literal("forcereward")
                .then(RequiredArgumentBuilder.<ServerCommandSource, Entity>argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("Le joueur spécifié n'est pas valide."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();
                            int currentReferrals = ReferralCounter.getCounter(targetUUID);

                            RewardManager.claimRewards(source, targetUUID, currentReferrals + 1); // Simulate a new referral

                            return 1;
                        })
                )
        );

        // /refadmin forcereset {playername}
        refAdminNode.then(LiteralArgumentBuilder.<ServerCommandSource>literal("forcereset")
                .then(RequiredArgumentBuilder.<ServerCommandSource, Entity>argument("playername", EntityArgumentType.entity())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            Entity entity = EntityArgumentType.getEntity(context, "playername");

                            if (!(entity instanceof ServerPlayerEntity)) {
                                source.sendError(Text.literal("Le joueur spécifié n'est pas valide."));
                                return 0;
                            }

                            ServerPlayerEntity target = (ServerPlayerEntity) entity;
                            String targetUUID = target.getUuid().toString();
                            ReferralCounter.COUNTERS.put(targetUUID, 0);
                            ReferralCounter.saveCounters();

                            source.sendFeedback(() -> Text.literal("Le compteur de referrals de " + target.getName().getString() + " a été réinitialisé."), true);
                            return 1;
                        })
                )
        );

        // /refadmin forceset {playername} {amount}
        refAdminNode.then(LiteralArgumentBuilder.<ServerCommandSource>literal("forceset")
                .then(RequiredArgumentBuilder.<ServerCommandSource, Entity>argument("playername", EntityArgumentType.entity())
                        .then(RequiredArgumentBuilder.<ServerCommandSource, Integer>argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    Entity entity = EntityArgumentType.getEntity(context, "playername");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    if (!(entity instanceof ServerPlayerEntity)) {
                                        source.sendError(Text.literal("Le joueur spécifié n'est pas valide."));
                                        return 0;
                                    }

                                    if (amount < 0) {
                                        source.sendError(Text.literal("Le montant doit être positif."));
                                        return 0;
                                    }

                                    ServerPlayerEntity target = (ServerPlayerEntity) entity;
                                    String targetUUID = target.getUuid().toString();
                                    ReferralCounter.COUNTERS.put(targetUUID, amount);
                                    ReferralCounter.saveCounters();

                                    source.sendFeedback(() -> Text.literal("Le compteur de referrals de " + target.getName().getString() + " a été défini à " + amount + "."), true);
                                    return 1;
                                })
                        )
                )
        );

        dispatcher.register(refAdminNode);
    }
}