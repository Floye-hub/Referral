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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RewardsGUI extends SimpleGui {
    private final ServerPlayerEntity player;

    // Pour chaque page, on affiche 27 récompenses.
    private int currentPage = 0;
    private static final int PAGE_SIZE = 27; // 9x3 slots pour les récompenses

    public RewardsGUI(ServerPlayerEntity player) {
        // Utilisation d'une interface 9x4 (36 slots) afin de disposer d'espaces additionnels pour la navigation.
        super(ScreenHandlerType.GENERIC_9X3, player, false);
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

        // Utiliser la méthode getVisibleRewards pour obtenir une fenêtre de 5 récompenses
        String playerUUID = player.getUuid().toString();
        List<RewardManager.Reward> visibleRewards = RewardManager.getVisibleRewards(playerUUID);

        int[] rewardSlots = new int[]{11, 12, 13, 14, 15};


        // Afficher chacune des 5 récompenses dans les slots spécifiés
        for (int i = 0; i < visibleRewards.size() && i < rewardSlots.length; i++) {
            RewardManager.Reward reward = visibleRewards.get(i);
            Item rewardItem = RewardManager.getRewardItem(reward.item);
            ItemStack rewardStack = new ItemStack(rewardItem);

            GuiElementBuilder element = GuiElementBuilder.from(rewardStack)
                    .setName(Text.literal(reward.displayName).formatted(Formatting.GOLD));

            if (reward.lore != null && !reward.lore.isEmpty()) {
                for (String line : reward.lore) {
                    element.addLoreLine(Text.literal(line).formatted(Formatting.GRAY));
                }
            }

            // Vérifier si le joueur peut réclamer cette récompense
            int currentReferrals = ReferralCounter.getCounter(playerUUID);
            Set<Integer> claimed = RewardManager.getClaimedRewards().getOrDefault(playerUUID, new HashSet<>());

            if (currentReferrals >= reward.requiredReferrals && !claimed.contains(reward.requiredReferrals)) {
                element.setCallback((slot, type, action, gui) -> {
                    RewardManager.claimReward(player, reward.requiredReferrals);
                    gui.close();
                });
                element.glow(true);
            } else {
                // Si la récompense n'est pas disponible, afficher en grisé
                element.setItem(Items.GRAY_DYE);
                if (currentReferrals < reward.requiredReferrals) {
                    element.addLoreLine(Text.literal("Requires " + reward.requiredReferrals + " referrals").formatted(Formatting.RED));
                } else {
                    element.addLoreLine(Text.literal("Already claimed").formatted(Formatting.RED));
                }
            }
            this.setSlot(rewardSlots[i], element);
        }

        // Bouton "Page précédente" dans le slot 27

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