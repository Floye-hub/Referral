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

    public RewardsGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.player = player;
        this.setTitle(Text.literal(RewardManager.getGuiTitle()));
        initializeGUI();
    }
    private int currentPage = 0;
    private static final int PAGE_SIZE = 27; // Taille d'une page (9x3 slots)

    private void initializeGUI() {
        // Remplir tous les slots avec un fond
        ItemStack background = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        GuiElementBuilder backgroundElement = new GuiElementBuilder()
                .setItem(background.getItem())
                .setName(Text.empty());

        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, backgroundElement);
        }

        // Calculer les récompenses à afficher pour la page actuelle
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, RewardManager.getRewards().size());

        // Afficher les récompenses
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

            this.setSlot(i - startIndex, element); // Afficher la récompense dans le slot correspondant
        }

        // Ajout des boutons de navigation
        if (currentPage > 0) {
            this.setSlot(27, new GuiElementBuilder()
                    .setItem(Items.ARROW)
                    .setName(Text.literal("Page précédente"))
                    .setCallback((slot, type, action, gui) -> {
                        currentPage--;
                        initializeGUI();
                    }));
        }

        if (endIndex < RewardManager.getRewards().size()) {
            this.setSlot(35, new GuiElementBuilder()
                    .setItem(Items.ARROW)
                    .setName(Text.literal("Page suivante"))
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