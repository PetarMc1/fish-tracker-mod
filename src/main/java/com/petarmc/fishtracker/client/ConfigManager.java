package com.petarmc.fishtracker.client;

import java.io.*;
import java.util.Properties;
import com.petarmc.lib.log.PLog;

public class ConfigManager {
    private static final File CONFIG_FILE = new File(System.getProperty("user.dir"), "fishtracker.properties");
    private static final PLog log = new PLog("ConfigManager");

    public String user = "";
    public String password = "";
    public String apiKey = "";
    public String endpoint = "https://api.tracker.petarmc.com";

    public ConfigManager() {
        if (CONFIG_FILE.exists()) {
            load();
        } else {
            save();
        }
    }

    public void load() {
        if (!CONFIG_FILE.exists()) return;
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            Properties p = new Properties();
            p.load(fis);
            String v;

            v = p.getProperty("user");
            if (v != null && !v.trim().isEmpty()) user = v.trim();

            v = p.getProperty("password");
            if (v != null && !v.trim().isEmpty()) password = v.trim();

            v = p.getProperty("apiKey");
            if (v != null && !v.trim().isEmpty()) apiKey = v.trim();

            v = p.getProperty("endpoint");
            if (v != null && !v.trim().isEmpty()) endpoint = v.trim();
        } catch (IOException e) {
            log.error("Failed to load fishtracker.properties", e);
        }
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            Properties p = new Properties();
            p.setProperty("user", user);
            p.setProperty("password", password);
            p.setProperty("apiKey", apiKey);
            p.setProperty("endpoint", endpoint);
            p.store(fos, "Fishtracker config");
        } catch (IOException e) {
            log.error("Failed to save fishtracker.properties", e);
        }
    }

    public boolean isComplete() {
        return user != null && !user.isEmpty()
                && password != null && !password.isEmpty()
                && apiKey != null && !apiKey.isEmpty()
                && endpoint != null && !endpoint.isEmpty();
    }
}
