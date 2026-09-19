package com.papack.bootslunge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.papack.bootslunge.Bootslunge;
import com.papack.bootslunge.client.config.ConfigClient;
import com.papack.bootslunge.client.config.ConfigScreen;
import com.papack.bootslunge.network.LungePacketPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class BootslungeClient implements ClientModInitializer {

    public static final KeyMapping.Category KEY_CATEGORY_BOOT_LUNGE = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Bootslunge.MOD_ID, "main"));
    public static final String KEY_DESC_BOOT_LUNGE = "key.desc.bootlunge.lunge_key";
    public static final String KEY_DESC_BOOT_LUNGE_CONFIG_SCREEN = "key.desc.bootlunge.config_screen";
    private static final int NO_DIRECTION = 0;

    public static KeyMapping LUNGE_KEY;
    public static KeyMapping CONFIG_SCREEN_KEY;

    // Config - Client
    public static ConfigClient configClient;
    public static ConfigClient DEFAULT_CONFIG;

    private static boolean wasJumpPressed = false;
    private static int offGroundTicks = 0; // 空中にいる時間をカウント
    private static int brakeCooldownTicks = 0;

    @Override
    public void onInitializeClient() {

        // Config initialize
        configClient = ConfigClient.load();
        DEFAULT_CONFIG = new ConfigClient();

        // Key Binding
        LUNGE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_DESC_BOOT_LUNGE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY_BOOT_LUNGE
        ));

        CONFIG_SCREEN_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_DESC_BOOT_LUNGE_CONFIG_SCREEN,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                KEY_CATEGORY_BOOT_LUNGE
        ));

        // Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!(client.player instanceof LocalPlayer player)) return;

            // Config Screen
            if (CONFIG_SCREEN_KEY.consumeClick()) {
                if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
                    client.setScreen(ConfigScreen.getConfigScreen(null));
                } else {
                    client.player.displayClientMessage(Component.literal("\"Cloth Config API\".\n" + "It is not installed."), false);
                }
            }

            // 1. LUNGE_KEY の処理
            while (LUNGE_KEY.consumeClick()) {
                ClientPlayNetworking.send(new LungePacketPayload(
                        false,
                        player.isShiftKeyDown(),
                        configClient.playSound,
                        configClient.spawnParticle,
                        NO_DIRECTION,
                        0));
            }

            // 2. 空中時間のカウント処理
            if (player.onGround() || player.isInLiquid() || player.isInPowderSnow) {
                offGroundTicks = 0;
            } else {
                offGroundTicks++;
            }

            // ブレーキのクールダウン
            if (brakeCooldownTicks > 0) {
                brakeCooldownTicks--;
            }

            // 3. 空中ジャンプ（JumpKey）の処理
            boolean isJumpPressed = client.options.keyJump.isDown();
            boolean isCtrlPressed = client.options.keySprint.isDown();
            boolean isShiftPressed = player.isShiftKeyDown();
            boolean canJump = (configClient.directionalJump && !player.onGround() && offGroundTicks >= 3) || (configClient.quickDirectionalJump && isCtrlPressed);

            if (canJump && isJumpPressed) {
                if (configClient.emergencyBrakeAutoOption && isShiftPressed) {
                    if (brakeCooldownTicks == 0 && UtilsClient.hasSolidBlockBelow(player, 3)) {
                        sendJumpPacket(client, player);
                        brakeCooldownTicks = 20;
                    }
                } else {
                    if (!wasJumpPressed) {
                        sendJumpPacket(client, player);
                    }
                }
            }

            wasJumpPressed = isJumpPressed;
        });
    }

    private static void sendJumpPacket(Minecraft client, LocalPlayer player) {
        int direction = NO_DIRECTION;
        direction += client.options.keyUp.isDown() ? 1 : 0;
        direction += client.options.keyLeft.isDown() ? 2 : 0;
        direction += client.options.keyDown.isDown() ? 4 : 0;
        direction += client.options.keyRight.isDown() ? 8 : 0;

        ClientPlayNetworking.send(new LungePacketPayload(true,
                configClient.emergencyBrake && player.isShiftKeyDown(),
                configClient.playSound,
                configClient.spawnParticle,
                direction,
                configClient.directionalJumpAngle));
    }
}