package com.petarmc.fishtracker.client;

import com.petarmc.lib.log.PLog;
import com.petarmc.lib.net.HttpClientWrapper;
import com.petarmc.lib.task.TaskScheduler;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class NetworkHandler {

    private static final PLog log = new PLog("NetworkHandler");
    private final ConfigManager config;
    private final EncryptionManager encryption;
    private final HttpClientWrapper client;
    private final TaskScheduler scheduler;
    private String gamemode = null;

    public NetworkHandler(ConfigManager config, EncryptionManager encryption) {
        this.config = config;
        this.encryption = encryption;
        this.client = new HttpClientWrapper(3);
        this.scheduler = new TaskScheduler(4);
    }

    public void setGamemode(String g) {
        this.gamemode = g;
    }

    public boolean fetchKey() {
        try {

            String url = config.endpoint + "/get/user/key";

            String credentials = config.user + ":" + config.password;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("x-api-key", config.apiKey)
                    .header("User-Agent", "FishtrackerClient/1.0")
                    .build();

            log.debug("Fetching Fernet key from: " + url);

            String resp = client.post(req).join();
            if (resp == null) {
                log.error("Empty response when fetching key");
                return false;
            }

            String key = extractKeyFromResponse(resp);
            if (key == null) {
                log.error("fernetKey not found in response. Response preview: " + (resp.length() > 200 ? resp.substring(0,200) + "..." : resp));
                return false;
            }
            encryption.setKey(key);
            log.info("Fernet key loaded successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to fetch key", e);
            return false;
        }
    }

    private String extractKeyFromResponse(String resp) {
        if (resp == null) return null;

        java.util.regex.Pattern[] patterns = new java.util.regex.Pattern[]{
                java.util.regex.Pattern.compile("\"fernetKey\"\\s*:\\s*\"([^\"]+)\"")
        };

        for (java.util.regex.Pattern p : patterns) {
            java.util.regex.Matcher m = p.matcher(resp);
            if (m.find()) return m.group(1);
        }

        String t = resp.trim();
        if (t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1);
        }

        if (t.matches("[A-Za-z0-9_\\-]{16,255}={0,2}")) return t;

        return null;
    }


    public void send(String path, String json) {
        scheduler.runAsync(() -> {
            try {

                String encrypted = encryption.encrypt(json);
                byte[] payload = encrypted.getBytes(StandardCharsets.UTF_8);

                String url = config.endpoint + "/post/" + path + "?name=" + config.user;

                log.debug("Sending encrypted data to: " + url + " (payload length: " + payload.length + ")");


                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                        .header("Content-Type", "application/octet-stream")
                        .header("x-api-key", config.apiKey)
                        .header("User-Agent", "FishtrackerClient/1.0");

                if (gamemode != null) {
                    reqBuilder.header("x-gamemode", gamemode);
                }

                HttpRequest req = reqBuilder.build();

                client.post(req).join();

                log.info("Data successfully sent to" + gamemode + "gamemode");
                log.debug("Data sent to " + path + "with x-gamemode: " + gamemode);
            } catch (Exception e) {
                log.error("Failed to send encrypted data", e);
            }
        });
    }
}
