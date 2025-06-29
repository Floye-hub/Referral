package com.floye.referral.util;

import java.util.HashMap;
import java.util.Map;

public class ReferralCounter {
    // Stocke le nombre de referrals par UUID
    private static final Map<String, Integer> COUNTERS = new HashMap<>();

    public static void incrementCounter(String uuid) {
        int current = COUNTERS.getOrDefault(uuid, 0);
        COUNTERS.put(uuid, current + 1);
        // Vous pouvez ajouter une logique pour sauvegarder ces données si nécessaire.
    }

    public static int getCounter(String uuid) {
        return COUNTERS.getOrDefault(uuid, 0);
    }
}