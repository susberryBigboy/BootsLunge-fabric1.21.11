package com.papack.bootslunge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record LungePacketPayload(
        boolean request,
        boolean shiftDown,
        boolean sound,
        boolean particle,
        int direction,
        int angle,
        float adjustment) implements CustomPacketPayload {

    public static final Type<LungePacketPayload> TYPE = new Type<>(LungePacketConstants.BL_PACKET_ID);

    public static final StreamCodec<FriendlyByteBuf, LungePacketPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, LungePacketPayload::request,
                    ByteBufCodecs.BOOL, LungePacketPayload::shiftDown,
                    ByteBufCodecs.BOOL, LungePacketPayload::sound,
                    ByteBufCodecs.BOOL, LungePacketPayload::particle,
                    ByteBufCodecs.INT, LungePacketPayload::direction,
                    ByteBufCodecs.INT, LungePacketPayload::angle,
                    ByteBufCodecs.FLOAT,LungePacketPayload::adjustment,
                    LungePacketPayload::new
            );

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}