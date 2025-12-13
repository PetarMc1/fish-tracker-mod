package com.petarmc.fishtracker.client;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
        private static final File CONFIG_FILE = new File(System.getProperty("user.dir"), "fishtracker.properties");

    public String user = "";
    public String password = "";
    public String apiKey = "";
    public String endpoint = "https://api.tracker.petarmc.com";

    public ConfigManager() {
    }

    public void load() {
        if (!CONFIG_FILE.exists()) return;
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            Properties p = new Properties();
            p.load(fis);
            user = p.getProperty("user", "").trim();
            password = p.getProperty("password", "").trim();
            apiKey = p.getProperty("apiKey", "").trim();
            endpoint = p.getProperty("endpoint", "").trim();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public boolean isComplete() {
        return user != null && !user.isEmpty()
                && password != null && !password.isEmpty()
                && apiKey != null && !apiKey.isEmpty()
                && endpoint != null && !endpoint.isEmpty();
    }
}
