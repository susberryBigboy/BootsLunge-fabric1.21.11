package com.papack.bootslunge.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
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

    @SerializedName("play_sound")
    public boolean playSound = true;

    @SerializedName("spawn_particle")
    public boolean spawnParticle = true;

    @SerializedName("enable_ctrl_quick_jump")
    public boolean enableCtrlQuickJump = true;

    @SerializedName("directional_jump_angle")
    public int directionalJumpAngle = 60;


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
