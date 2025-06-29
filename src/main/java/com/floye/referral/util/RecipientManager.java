package com.floye.referral.util;

// RecipientManager.java
public class RecipientManager {
    private static String currentRecipientUUID = null;

    public static void setCurrentRecipient(String uuid) {
        currentRecipientUUID = uuid;
    }

    public static String getCurrentRecipient() {
        return currentRecipientUUID;
    }

    public static void clearRecipient() {
        currentRecipientUUID = null;
    }
}