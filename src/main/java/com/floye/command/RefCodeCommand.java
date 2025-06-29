package com.floye.command;// RefCodeCommand.java
import com.floye.referral.util.CodeManager;
import com.floye.referral.util.RecipientManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

public class RefCodeCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> refNode = LiteralArgumentBuilder
                    .<ServerCommandSource>literal("ref")
                    .then(LiteralArgumentBuilder
                            .<ServerCommandSource>literal("claim")
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
                    .then(RequiredArgumentBuilder
                            .<ServerCommandSource, String>argument("playercode", StringArgumentType.string())
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                String playercode = StringArgumentType.getString(context, "playercode");

                                String recipientUUID = CodeManager.findPlayerUUIDByCode(playercode);
                                if (recipientUUID == null) {
                                    source.sendFeedback(() -> Text.literal("Code invalide ou non trouvé."), false);
                                    return 0;
                                }

                                RecipientManager.setCurrentRecipient(recipientUUID);
                                source.sendFeedback(() -> Text.literal("Joueur avec le code " + playercode + " est maintenant le destinataire."), false);
                                return 1;
                            })
                    );

            dispatcher.register(refNode);
        });
    }
}