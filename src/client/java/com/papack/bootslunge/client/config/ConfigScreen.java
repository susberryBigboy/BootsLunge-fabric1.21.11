package com.papack.bootslunge.client.config;


import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static com.papack.bootslunge.client.BootslungeClient.DEFAULT_CONFIG;
import static com.papack.bootslunge.client.BootslungeClient.configClient;

@Environment(EnvType.CLIENT)
public class ConfigScreen {


    public static Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Boots Lunge Settings"));

        builder.setGlobalized(false);
        builder.setGlobalizedExpanded(true);


        ConfigCategory generalConfig = builder.getOrCreateCategory(Component.literal("General").withStyle((s) -> s.withColor(ChatFormatting.GREEN)));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        SubCategoryBuilder screen = entryBuilder.startSubCategory(Component.literal("Config Screen")).setExpanded(true);

        screen.add(entryBuilder
                .startBooleanToggle(Component.translatable("config.bl.option.play_sound"), configClient.playSound)
                .setDefaultValue(DEFAULT_CONFIG.playSound)
                .setSaveConsumer(newValue -> configClient.playSound = newValue)
                .build());

        screen.add(entryBuilder
                .startBooleanToggle(Component.translatable("config.bl.option.spawn_particle"), configClient.spawnParticle)
                .setDefaultValue(DEFAULT_CONFIG.spawnParticle)
                .setSaveConsumer(newValue -> configClient.spawnParticle = newValue)
                .build());

        screen.add(entryBuilder
                .startBooleanToggle(Component.translatable("config.bl.option.emergency_brake"), configClient.emergencyBrake)
                .setDefaultValue(DEFAULT_CONFIG.emergencyBrake)
                .setSaveConsumer(newValue -> configClient.emergencyBrake = newValue)
                        .setTooltip(Component.translatable("onfig.bl.tooltip.emergency_brake"))
                .build());

        screen.add(entryBuilder
                .startBooleanToggle(Component.translatable("config.bl.option.directional_jump"), configClient.directionalJump)
                .setDefaultValue(DEFAULT_CONFIG.directionalJump)
                .setSaveConsumer(newValue -> configClient.directionalJump = newValue)
                .setTooltip(Component.translatable("onfig.bl.tooltip.directional_jump"))
                .build());

        screen.add(entryBuilder
                .startBooleanToggle(Component.translatable("config.bl.option.quick_directional_jump"), configClient.quickDirectionalJump)
                .setDefaultValue(DEFAULT_CONFIG.quickDirectionalJump)
                .setSaveConsumer(newValue -> configClient.quickDirectionalJump = newValue)
                .setTooltip(Component.translatable("onfig.bl.tooltip.quick_directional_jump"))
                .build());

        screen.add(entryBuilder
                .startIntField(Component.translatable("config.bl.option.directional_jump_angle"), configClient.directionalJumpAngle)
                .setDefaultValue(DEFAULT_CONFIG.directionalJumpAngle)
                .setSaveConsumer(newValue -> configClient.directionalJumpAngle = newValue)
                .setMin(0)
                .setMax(90)
                .build()
        );

        // SET ENTRY --------------------------------------------
        generalConfig.addEntry(screen.build());

        // SAVE -------------------------------------------------
        builder.setSavingRunnable(() -> configClient.save());

        return builder.build();
    }
}