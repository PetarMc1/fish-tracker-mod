package com.petarmc.fishtracker;

import com.petarmc.lib.log.LogConfig;
import net.fabricmc.api.ModInitializer;
import com.petarmc.lib.log.PLog;

public class Fishtracker implements ModInitializer {

    public static final String MOD_ID = "fishtracker";
    private static final PLog log = new PLog("FishTracker");

    @Override
    public void onInitialize() {
        LogConfig.globalPrefix = "[FishTracker]";
        log.info("initialized");
    }
}
