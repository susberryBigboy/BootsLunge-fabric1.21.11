package com.papack.bootslunge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.papack.bootslunge.Bootslunge;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("BootsLunge");
    private static final File FILE = CONFIG_DIR.resolve("config.json").toFile();

    public double directionJump_moveStrength_base = 1.0;
    public double directionJump_moveStrength_levelMultiplier = 0.2;
    public double directionJump_moveStrength_jumpStrength = 0.35;

    public double no_directionJump_jumpStrength_base = 0.8;
    public double no_directionJump_jumpStrength_levelMultiplier = 0.4;
    public double no_directionJump_jumpStrength_countMultiplier = 0.1;

    public double lungeJump_strength_base = 0.8;
    public double lungeJump_strength_levelMultiplier = 0.4;
    public double lungeJump_strength_countMultiplier = 0.1;

    public double inLiquid_lungeVelocity_dampingMultiplier = 0.3;
    public double inLiquid_currentVelocity_dampingMultiplier = 0.2;


    public static Config load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                Config config = GSON.fromJson(reader, Config.class);
                if (config != null) return config;
            } catch (Exception e) {
                Bootslunge.LOGGER.error("Failed to load config", e);
            }
        }
        Config defaultConfig = new Config();
        defaultConfig.save();
        return defaultConfig;
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