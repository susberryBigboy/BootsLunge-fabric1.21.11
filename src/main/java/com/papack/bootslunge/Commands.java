package com.papack.bootslunge;

import com.mojang.brigadier.context.CommandContext;
import com.papack.bootslunge.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import static com.papack.bootslunge.Bootslunge.config;

public class Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, ignore) ->
                dispatcher.register(
                        net.minecraft.commands.Commands.literal("bl")
                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))

                                // --- 基本・管理コマンド ---
                                // 設定をデフォルトにリセット（管理者専用）
                                .then(net.minecraft.commands.Commands.literal("reset")
                                        .executes(Commands::resetToDefault))

                                // 設定リロード（管理者専用）
                                .then(net.minecraft.commands.Commands.literal("reload")
                                        .executes(Commands::reloadConfig))));
    }

    private static int resetToDefault(CommandContext<CommandSourceStack> ctx) {
        // Create new instance
        config = new Config();
        save();

        ctx.getSource().sendSuccess(() -> Component.literal("Settings reset to defaults"), true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {

        // Reload
        config = Config.load();

        ctx.getSource().sendSuccess(() -> Component.literal("Configuration reloaded successfully."), true);
        return 1;
    }

    private static void save() {
        config.save();
        config = Config.load();
    }
}
