package com.floye.referral.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RewardsGUI extends SimpleGui {

    private final ServerPlayerEntity player;

    public RewardsGUI(ServerPlayerEntity player) {
        // Ici, on crée une interface similaire à un inventaire avec 3 lignes de 9 slots.
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.player = player;
        this.setTitle(Text.literal("Mes Rewards"));
        initializeGUI();
    }

    private void initializeGUI() {
        // Exemple : Afficher 27 rewards fictives.
        // Remplacez cette boucle par la récupération dynamique de rewards via RewardManager
        for (int i = 0; i < 27; i++) {
            // Exemple d'item de récompense (ici, NETHER_STAR en guise de placeholder)
            ItemStack rewardStack = new ItemStack(Items.NETHER_STAR, 1);

            // Création du composant GUI pour ce slot, similaire à GuiElementBuilder dans ShopGUI
            int finalI = i;
            GuiElementBuilder element = GuiElementBuilder.from(rewardStack)
                    .setName(Text.literal("Reward " + (i + 1)).formatted(Formatting.GOLD))
                    .addLoreLine(Text.literal("Cliquez pour récupérer cette récompense.").formatted(Formatting.GRAY))
                    // On définit un callback pour gérer le clic sur la reward
                    .setCallback((slot, type, action, gui) -> {
                        // Ici, vous pouvez appeler votre RewardManager pour "claim" la reward
                        // RewardManager.claimReward(player.getUuid().toString(), rewardId);
                        player.sendMessage(Text.literal("Récompense " + (finalI + 1) + " cliquée !").formatted(Formatting.GREEN), false);
                        // Fermez la GUI après le clic, si nécessaire
                        gui.close();
                    });

            // Placer l'élément dans le slot i
            this.setSlot(i, element);
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
        // Par exemple, envoyer une notification réseau ou activer un overlay si besoin
        // ServerPlayNetworking.send(player, new YourOverlayPayload(true));
    }

    @Override
    public void onClose() {
        super.onClose();
        // Par exemple, désactiver l'overlay
        // ServerPlayNetworking.send(player, new YourOverlayPayload(false));
    }
}