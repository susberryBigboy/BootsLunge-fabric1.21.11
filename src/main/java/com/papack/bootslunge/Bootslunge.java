package com.papack.bootslunge;

import com.papack.bootslunge.config.ConfigServer;
import com.papack.bootslunge.network.LungePacketPayload;
import com.papack.bootslunge.network.ReceivedPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootslunge implements ModInitializer {

    public static final String MOD_ID = "bootslunge";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final ResourceKey<Enchantment> BOOTS_LUNGE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Bootslunge.MOD_ID, "boots_lunge")
    );

    // Config - Server
    public static ConfigServer configServer;

    @Override
    public void onInitialize() {

        // Config initialize
        configServer = ConfigServer.load();

        // Commands
        Commands.register();

        // Packet
        PayloadTypeRegistry.playS2C().register(LungePacketPayload.TYPE, LungePacketPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LungePacketPayload.TYPE, LungePacketPayload.CODEC);

        // Packet Receiver
        ServerPlayNetworking.registerGlobalReceiver(LungePacketPayload.TYPE, ReceivedPacketHandler::lungeJumpAction);
    }
}