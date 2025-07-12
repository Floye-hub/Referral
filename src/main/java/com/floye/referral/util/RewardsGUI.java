package com.floye.referral.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RewardsGUI extends SimpleGui {
    private final ServerPlayerEntity player;

    // Pour chaque page, on affiche 27 récompenses.
    private int currentPage = 0;
    private static final int PAGE_SIZE = 27; // 9x3 slots pour les récompenses

    public RewardsGUI(ServerPlayerEntity player) {
        // Utilisation d'une interface 9x4 (36 slots) afin de disposer d'espaces additionnels pour la navigation.
        super(ScreenHandlerType.GENERIC_9X4, player, false);
        this.player = player;
        this.setTitle(Text.literal(RewardManager.getGuiTitle()));
        initializeGUI();
    }

    private void initializeGUI() {
        // Remplir tous les slots avec un fond
        ItemStack background = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        GuiElementBuilder backgroundElement = new GuiElementBuilder()
                .setItem(background.getItem())
                .setName(Text.empty());

        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, backgroundElement);
        }

        // Calculer l'index de début et de fin des récompenses à afficher pour la page actuelle.
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, RewardManager.getRewards().size());

        // Afficher les récompenses sur la page
        for (int i = startIndex; i < endIndex; i++) {
            RewardManager.Reward reward = RewardManager.getRewards().get(i);
            Item rewardItem = RewardManager.getRewardItem(reward.item);
            ItemStack rewardStack = new ItemStack(rewardItem);

            GuiElementBuilder element = GuiElementBuilder.from(rewardStack)
                    .setName(Text.literal(reward.displayName).formatted(Formatting.GOLD));

            if (reward.lore != null && !reward.lore.isEmpty()) {
                for (String line : reward.lore) {
                    element.addLoreLine(Text.literal(line).formatted(Formatting.GRAY));
                }
            }

            element.setCallback((slot, type, action, gui) -> {
                RewardManager.claimReward(player, reward.requiredReferrals);
                gui.close();
            });

            // Positionner la récompense dans la zone dédiée (slots 0 à 26)
            this.setSlot(i - startIndex, element);
        }

        // Bouton "Page précédente" dans le slot 27 (premier slot de la 2ème ligne, hors zone de récompenses)
        if (currentPage > 0) {
            this.setSlot(27, new GuiElementBuilder()
                    .setItem(Items.ARROW)
                    .setName(Text.literal("Previous page"))
                    .setCallback((slot, type, action, gui) -> {
                        currentPage--;
                        initializeGUI();
                    }));
        }

        // Bouton "Page suivante" dans le slot 35 (dernier slot de l'interface)
        if (endIndex < RewardManager.getRewards().size()) {
            this.setSlot(35, new GuiElementBuilder()
                    .setItem(Items.ARROW)
                    .setName(Text.literal("Next page"))
                    .setCallback((slot, type, action, gui) -> {
                        currentPage++;
                        initializeGUI();
                    }));
        }
    }

    @Override
    public void onOpen() {
        super.onOpen();
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}