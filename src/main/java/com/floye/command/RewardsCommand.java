package com.floye.command;

import com.floye.referral.util.RewardsGUI;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class RewardsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rewards")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        // Ouvrir l'interface des rewards pour le joueur
                        new RewardsGUI(player).open();
                    } else {
                        context.getSource().sendError(Text.literal("Only a player can execute this command."));
                    }
                    return 1;
                }));
    }
}