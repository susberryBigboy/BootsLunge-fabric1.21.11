package com.papack.bootslunge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record LungePacketPayload(
        boolean request,
        boolean sound,
        boolean particle,
        boolean quickJump,
        int direction,
        int angle) implements CustomPacketPayload {

    // パケット識別用のID
    public static final Type<LungePacketPayload> TYPE = new Type<>(LungePacketConstants.BL_PACKET_ID);

    // データの読み書き方法（Codec）を定義
    public static final StreamCodec<FriendlyByteBuf, LungePacketPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, LungePacketPayload::request,
                    ByteBufCodecs.BOOL, LungePacketPayload::sound,
                    ByteBufCodecs.BOOL, LungePacketPayload::particle,
                    ByteBufCodecs.BOOL, LungePacketPayload::quickJump,
                    ByteBufCodecs.INT, LungePacketPayload::direction,
                    ByteBufCodecs.INT, LungePacketPayload::angle,
                    LungePacketPayload::new
            );

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}