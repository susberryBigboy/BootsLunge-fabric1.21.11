package com.papack.bootslunge.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.papack.bootslunge.Bootslunge;
import com.papack.bootslunge.network.LungePacketPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

public class BootslungeClient implements ClientModInitializer {

    public static final KeyMapping.Category KEY_CATEGORY_BOOT_LUNGE = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Bootslunge.MOD_ID, "main"));
    public static final String KEY_DESC_BOOT_LUNGE = "key.desc.bootlunge.lunge_key";

    public static KeyMapping LUNGE_KEY;
    private static boolean wasJumpPressed = false; // 連打防止用フラグ


    @Override
    public void onInitializeClient() {

        LUNGE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_DESC_BOOT_LUNGE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Q,
                KEY_CATEGORY_BOOT_LUNGE
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!(client.player instanceof LocalPlayer player)) return;

            while (LUNGE_KEY.consumeClick()) {
                ClientPlayNetworking.send(new LungePacketPayload(false));

            }

            // 2. 空中ジャンプ（JumpKey）の処理
            boolean isJumpPressed = client.options.keyJump.isDown();

            if (!player.onGround()) {

                Level level = player.level();
                BlockState blockState = level.getBlockState(player.blockPosition().below());

                // 空中で「今キーが押され、直前は押されていなかった」瞬間に発動
                if (isJumpPressed && !wasJumpPressed && blockState.is(Blocks.AIR)) {
                    ClientPlayNetworking.send(new LungePacketPayload(true));
                }
            }

            wasJumpPressed = isJumpPressed;
        });
    }
}