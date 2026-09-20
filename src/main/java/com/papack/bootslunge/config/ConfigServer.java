package com.papack.bootslunge.config;

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

public class ConfigServer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("BootsLunge");
    private static final File FILE = CONFIG_DIR.resolve("config_server.json").toFile();

    // 推進力 (Unified Lunge Strength) ---------------------
    @SerializedName("lunge_base_strength")
    public double lungeBaseStrength = 0.8;

    @SerializedName("lunge_level_multiplier")
    public double lungeLevelMultiplier = 0.2;

    @SerializedName("lunge_count_multiplier")
    public double lungeCountMultiplier = 0.3;

    @SerializedName("emergency_brake_float_velocity")
    public double emergencyBrakeFloatVelocity = 0.1;

    // In Liquid / Snow ------------------------------------
    @SerializedName("in_liquid_lunge_velocity_damping_multiplier")
    public double inLiquidLungeVelocityDampingMultiplier = 0.3;

    @SerializedName("in_liquid_current_velocity_damping_multiplier")
    public double inLiquidCurrentVelocityDampingMultiplier = 0.2;

    public static ConfigServer load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                ConfigServer configServer = GSON.fromJson(reader, ConfigServer.class);
                if (configServer != null) return configServer;
            } catch (Exception e) {
                Bootslunge.LOGGER.error("Failed to load config", e);
            }
        }
        ConfigServer defaultConfigServer = new ConfigServer();
        defaultConfigServer.save();
        return defaultConfigServer;
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