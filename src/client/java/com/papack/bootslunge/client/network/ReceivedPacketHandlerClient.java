package com.papack.bootslunge.client.network;

import com.papack.bootslunge.client.BootslungeClient;
import com.papack.bootslunge.network.LungePacketPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ReceivedPacketHandlerClient {

    public static void setEnableCtrlQuickJump(LungePacketPayload payload, ClientPlayNetworking.Context ignoreContext) {
        if (payload.direction() == 100) {
            BootslungeClient.enableCtrlJump = payload.request();
        }
    }
}