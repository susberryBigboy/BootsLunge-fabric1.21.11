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

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("BootsLunge");
    private static final File FILE = CONFIG_DIR.resolve("config.json").toFile();

    @SerializedName("play_sound")
    public boolean playSound = true;

    @SerializedName("spawn_particle")
    public boolean spawnParticle = true;

    // Directional Jump -------------------------------------
    @SerializedName("directional_jump_move_strength_base")
    public double directionJumpMoveStrengthBase = 1.0;

    @SerializedName("directional_jump_move_strength_level_multiplier")
    public double directionJumpMoveStrengthLevelMultiplier = 0.2;

    @SerializedName("directional_jump_move_strength_jump_strength")
    public double directionJumpMoveStrengthJumpStrength = 0.35;

    // No Direction Jump ------------------------------------
    @SerializedName("no_direction_jump_jump_strength_base")
    public double noDirectionJumpJumpStrengthBase = 0.8;

    @SerializedName("no_direction_jump_jump_level_multiplier")
    public double noDirectionJumpJumpStrengthLevelMultiplier = 0.4;

    @SerializedName("no_direction_jump_jump_count_multiplier")
    public double noDirectionJumpJumpStrengthCountMultiplier = 0.1;

    // Lunge Jump -------------------------------------------
    @SerializedName("lunge_jump_strength_base")
    public double lungeJumpStrengthBase = 0.8;

    @SerializedName("lunge_jump_strength_level_multiplier")
    public double lungeJumpStrengthLevelMultiplier = 0.4;

    @SerializedName("lunge_jump_strength_count_multiplier")
    public double lungeJumpStrengthCountMultiplier = 0.1;

    // In Liquid --------------------------------------------
    @SerializedName("in_liquid_lunge_velocity_damping_multiplier")
    public double inLiquidLungeVelocityDampingMultiplier = 0.3;

    @SerializedName("in_liquid_current_velocity_damping_multiplier")
    public double inLiquidCurrentVelocityDampingMultiplier = 0.2;


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