package com.petarmc.fishtracker.integration;

import com.petarmc.fishtracker.FishtrackerClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> {
            if (FishtrackerClient.INSTANCE != null) {
                return FishtrackerClient.INSTANCE.createConfigScreen(parent);
            }
            return parent;
        };
    }
}
