package com.floye.referral.util;// CodeManager.java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CodeManager {
    private static final Path CODES_PATH = FabricLoader.getInstance().getConfigDir().resolve("referral/ref_codes.json");
    private static final File CODES_FILE = CODES_PATH.toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> CODES = new HashMap<>();

    public static void loadCodes() {
        if (CODES_FILE.exists()) {
            try (Reader reader = new FileReader(CODES_FILE)) {
                CODES.putAll(GSON.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveCodes() {
        try (Writer writer = new FileWriter(CODES_FILE)) {
            GSON.toJson(CODES, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCode(String uuid) {
        return CODES.get(uuid);
    }

    public static void setCode(String uuid, String code) {
        CODES.put(uuid, code);
        saveCodes();
    }
    // CodeManager.java
    public static String findPlayerUUIDByCode(String code) {
        for (Map.Entry<String, String> entry : CODES.entrySet()) {
            if (entry.getValue().equals(code)) {
                return entry.getKey();
            }
        }
        return null;
    }
}