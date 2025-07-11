package com.floye.referral.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
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
        this.setTitle(Text.literal("Récompenses de parrainage"));
        initializeGUI();
    }

    private void initializeGUI() {
        // D'abord remplir tous les slots avec un fond
        ItemStack background = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        GuiElementBuilder backgroundElement = new GuiElementBuilder()
                .setItem(background.getItem())
                .setName(Text.empty());

        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, backgroundElement);
        }

        // Puis ajouter les rewards
        for (RewardManager.Reward reward : RewardManager.getRewards()) {
            ItemStack rewardStack = RewardManager.getRewardItemStack(reward);

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

            this.setSlot(reward.slot, element);
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