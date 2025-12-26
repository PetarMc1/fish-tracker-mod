package com.petarmc.fishtracker.client;

import com.petarmc.lib.log.LogConfig;
import com.petarmc.lib.log.PLog;
import com.petarmc.lib.chat.ChatPatternMatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import me.shedaniel.clothconfig2.api.*;

import java.util.regex.Pattern;

public class FishtrackerClient implements ClientModInitializer {

    private static final PLog log = new PLog("FishtrackerClient");

    private final ConfigManager config = new ConfigManager();
    private final EncryptionManager encryption = new EncryptionManager();
    private NetworkHandler network;
    private KeyBinding openGuiKey;
    private final ChatPatternMatcher chatMatcher = new ChatPatternMatcher();

    private boolean debugMode = false;


    @Override
    public void onInitializeClient() {
        LogConfig.globalPrefix = "[FishTracker]";

        config.load();
        network = new NetworkHandler(config, encryption);

        if (config.isComplete() && !network.fetchKey()) {
            log.warn("Failed to fetch Fernet key — check config");
        }

        // Initialize chat patterns
        initializeChatPatterns();

        // Chat message listeners
        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, ts) ->
            chatMatcher.processMessage(msg.getString()));
        ClientReceiveMessageEvents.GAME.register((msg, overlay) ->
            chatMatcher.processMessage(msg.getString()));

        // Keybinding
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fishtracker.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
               KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) openConfigGui();
        });

        LogConfig.globalLevel = debugMode ? com.petarmc.lib.log.LogLevel.DEBUG : com.petarmc.lib.log.LogLevel.INFO;
        log.info("Fishtracker client initialized");
    }

    private void initializeChatPatterns() {
        // Register fish catch pattern
        chatMatcher.registerPattern(
            "fish_catch",
            "(EPIC|GREAT|NICE|GOOD|LEGENDARY|INSANE)? ?CATCH! (?:Your Augments caught|You caught) (?:a|an) ([^!]+?)(?: with a length of [\\d.]+cm)?[.!]",
            Pattern.CASE_INSENSITIVE,
            match -> {
                String rarityKey = match.getGroup(1) != null ? match.getGroup(1).toUpperCase() : "";
                String fish = match.getGroup(2).trim();
                int rarity = mapRarity(rarityKey);

                if (debugMode) log.debug("Parsed fish: " + fish + ", rarity: " + rarity);
                else log.info("Caught fish: " + fish);

                network.send("fish", "{\"fish\":\"" + fish + "\",\"rarity\":" + rarity + "}");
            }
        );

        // Register new entry pattern
        chatMatcher.registerPattern(
            "new_entry",
            "NEW ENTRY! You caught (?:a|an) (.+?) for the first time[.!]",
            Pattern.CASE_INSENSITIVE,
            match -> {
                String entry = match.getGroup(1).trim();
                int rarity = mapNewEntry(entry);

                if (debugMode) log.debug("Parsed new entry: " + entry + ", rarity: " + rarity);
                else log.info("New entry: " + entry);

                network.send("fish", "{\"fish\":\"" + entry + "\",\"rarity\":" + rarity + "}");
            }
        );

        // Register exact crab message
        chatMatcher.registerExactMatch(
            "crab",
            "You’ve hooked a Crab!",
            (message, matchId) -> {
                if (debugMode) log.debug("Parsed crab: Crab");
                else log.info("Caught crab: Crab");

                network.send("crab", "{\"fish\":\"crab\"}");
            }
        );
    }

    private int mapRarity(String key) {
        return switch (key) {
            case "GOOD" -> 1;
            case "NICE" -> 2;
            case "GREAT" -> 3;
            case "EPIC" -> 4;
            case "LEGENDARY" -> 6;
            case "INSANE" -> 7;
            default -> 5;
        };
    }

    private int mapNewEntry(String entry) {
        String[] parts = entry.split(" ", 2);
        if (parts.length == 0) return 5;
        return switch (parts[0].toUpperCase()) {
            case "BRONZE" -> 1;
            case "SILVER" -> 2;
            case "GOLD" -> 3;
            case "DIAMOND" -> 4;
            case "PLATINUM" -> 6;
            case "MYTHICAL" -> 7;
            default -> 5;
        };
    }

    private String getServerType() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()) {
            return "Integrated Server";
        } else {
            if (client.getNetworkHandler() != null) {
                String brand = client.getNetworkHandler().getBrand();
                if (brand != null && !brand.isEmpty()) {
                    return brand;
                } else {
                    net.minecraft.client.network.ServerInfo server = client.getCurrentServerEntry();
                    if (server != null) {
                        return "Unknown (" + server.address + ")";
                    } else {
                        return "Not connected";
                    }
                }
            } else {
                return "Not connected";
            }
        }
    }

    private String getGamemode() {
        String brand = getServerType();
        if (brand.contains(" (Velocity)")) {
            String part = brand.substring(0, brand.indexOf(" (Velocity)"));
            String clean = stripColorCodes(part).toLowerCase();
            return clean;
        }
        return null;
    }

    private String getCleanServerName() {
        String brand = getServerType();
        if (brand.contains(" (Velocity)")) {
            String part = brand.substring(0, brand.indexOf(" (Velocity)"));
            return stripColorCodes(part);
        }
        return brand;
    }

    private String stripColorCodes(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "");
    }

    private void openConfigGui() {
        network.setGamemode(getGamemode());
        log.info("Current server: " + getCleanServerName() + ", gamemode: " + getGamemode());
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("gui.fishtracker.title"));
        builder.setParentScreen(MinecraftClient.getInstance().currentScreen);
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("gui.fishtracker.category.general"));
        ConfigEntryBuilder eb = builder.entryBuilder();
        general.addEntry(eb.startTextDescription(Text.translatable("gui.fishtracker.description")).build());

        general.addEntry(eb.startStrField(Text.translatable("gui.fishtracker.field.username"), config.user)
                .setSaveConsumer(val -> config.user = val == null ? "" : val.trim())
                .setTooltip(Text.translatable("gui.fishtracker.tooltip.username"))
                .build());

        general.addEntry(eb.startStrField(Text.translatable("gui.fishtracker.field.password"), config.password)
                .setSaveConsumer(val -> config.password = val == null ? "" : val.trim())
                .setTooltip(Text.translatable("gui.fishtracker.tooltip.password"))
                .build());

        general.addEntry(eb.startStrField(Text.translatable("gui.fishtracker.field.api_key"), config.apiKey)
                .setSaveConsumer(val -> config.apiKey = val == null ? "" : val.trim())
                .setTooltip(Text.translatable("gui.fishtracker.tooltip.api_key"))
                .build());

        general.addEntry(eb.startStrField(Text.translatable("gui.fishtracker.field.endpoint"), config.endpoint)
                .setSaveConsumer(val -> config.endpoint = val == null ? "" : val.trim())
                .setTooltip(Text.translatable("gui.fishtracker.tooltip.endpoint"))
                .build());

        general.addEntry(eb.startTextDescription(Text.translatable("gui.fishtracker.section.debug")).build());

        general.addEntry(eb.startBooleanToggle(Text.translatable("gui.fishtracker.field.debug"), debugMode)
                .setSaveConsumer(val -> debugMode = val)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("gui.fishtracker.tooltip.debug"))
                .build());

        builder.setSavingRunnable(() -> {
            config.save();
            if (config.isComplete()) network.fetchKey();
            LogConfig.globalLevel = debugMode ? com.petarmc.lib.log.LogLevel.DEBUG : com.petarmc.lib.log.LogLevel.INFO;
        });

        MinecraftClient.getInstance().setScreen(builder.build());
    }
}
