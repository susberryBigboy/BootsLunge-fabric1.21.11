package com.papack.bootslunge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.papack.bootslunge.Bootslunge;
import com.papack.bootslunge.client.network.ReceivedPacketHandlerClient;
import com.papack.bootslunge.network.LungePacketPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.papack.bootslunge.Bootslunge.config;

public class BootslungeClient implements ClientModInitializer {

    public static final KeyMapping.Category KEY_CATEGORY_BOOT_LUNGE = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Bootslunge.MOD_ID, "main"));
    public static final String KEY_DESC_BOOT_LUNGE = "key.desc.bootlunge.lunge_key";
    private static final int NO_DIRECTION = 0;

    public static KeyMapping LUNGE_KEY;

    private static boolean wasJumpPressed = false;
    private static int offGroundTicks = 0; // 空中にいる時間をカウント

    public static boolean enableCtrlJump = false;

    @Override
    public void onInitializeClient() {

        // Key Binding
        LUNGE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_DESC_BOOT_LUNGE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY_BOOT_LUNGE
        ));

        // Packet Receiver
        ClientPlayNetworking.registerGlobalReceiver(LungePacketPayload.TYPE, ReceivedPacketHandlerClient::setEnableCtrlQuickJump);

        // Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!(client.player instanceof LocalPlayer player)) return;

            // 1. LUNGE_KEY の処理
            while (LUNGE_KEY.consumeClick()) {
                ClientPlayNetworking.send(new LungePacketPayload(false, NO_DIRECTION));
            }

            // 2. 空中時間のカウント処理
            if (player.onGround() || player.isInLiquid() || player.isInPowderSnow) {
                offGroundTicks = 0;
            } else {
                offGroundTicks++;
            }

            // 3. 空中ジャンプ（JumpKey）の処理
            boolean isJumpPressed = client.options.keyJump.isDown();
            boolean isCtrlPressed = client.options.keySprint.isDown();

            // 「今キーが押された瞬間」かつ「空中に3Tick（約0.15秒）以上いる時」のみ許可
            // 地上ジャンプ直後の誤暴発を完全回避します
            if (isJumpPressed && !wasJumpPressed) {
                if ((!player.onGround() && offGroundTicks >= 3) || (isCtrlPressed && config.enableCtrlQuickJump)) {
                    int direction = NO_DIRECTION;
                    direction += client.options.keyUp.isDown() ? 1 : 0;
                    direction += client.options.keyLeft.isDown() ? 2 : 0;
                    direction += client.options.keyDown.isDown() ? 4 : 0;
                    direction += client.options.keyRight.isDown() ? 8 : 0;

                    ClientPlayNetworking.send(new LungePacketPayload(true, direction));
                }
            }

            wasJumpPressed = isJumpPressed;
        });
    }
}