package com.floye.referral;

import com.floye.command.RefCodeCommand;
import com.floye.referral.util.ClaimTracker;
import com.floye.referral.util.CodeManager;
import com.floye.referral.util.ReferralCounter;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Referral implements ModInitializer {
	public static final String MOD_ID = "referral";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Charger les codes de referral existants
		CodeManager.loadCodes();

		// Charger les compteurs des referrals
		ReferralCounter.loadCounters();

		// Charger les joueurs ayant déjà réclamé un code
		ClaimTracker.loadClaims();

		// Enregistrer la commande
		RefCodeCommand.register();

		LOGGER.info("Hello Fabric world!");
	}
}