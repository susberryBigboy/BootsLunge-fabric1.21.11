package com.papack.bootslunge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

// record を使うと Getter やコンストラクタが自動生成されるため非常にすっきり書けます！
public record LungePacketPayload(boolean request) implements CustomPacketPayload {

    // パケット識別用のID
    public static final Type<LungePacketPayload> TYPE = new Type<>(LungePacketConstants.BL_PACKET_ID);

    // データの読み書き方法（Codec）を定義
    public static final StreamCodec<FriendlyByteBuf, LungePacketPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, LungePacketPayload::request,
                    LungePacketPayload::new
            );

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}