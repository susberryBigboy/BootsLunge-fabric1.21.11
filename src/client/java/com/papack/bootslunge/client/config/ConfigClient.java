package com.papack.bootslunge.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.papack.bootslunge.Bootslunge;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigClient {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("BootsLunge");
    private static final File FILE = CONFIG_DIR.resolve("config_client.json").toFile();

    public boolean playSound = true;
    public boolean spawnParticle = true;

    public boolean emergencyBrake = true;
    public boolean emergencyBrakeAuto = true;
    public boolean emergencyBrakeDisableDirection = true;
    public boolean directionalJump = true;
    public boolean quickDirectionalJump = true;

    public int directionalJumpAngle = 60;
    public float powerAdjustmentLunge = 1.0f;
    public float powerAdjustmentDirection = 1.0f;

    public static ConfigClient load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                ConfigClient configClient = GSON.fromJson(reader, ConfigClient.class);
                if (configClient != null) return configClient;
            } catch (Exception e) {
                Bootslunge.LOGGER.error("Failed to load config", e);
            }
        }
        ConfigClient defaultConfigClient = new ConfigClient();
        defaultConfigClient.save();
        return defaultConfigClient;
    }

    public void save() {
        try {
            if (Files.notExists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            Bootslunge.LOGGER.error("Failed to save config", e);
        }
    }
}