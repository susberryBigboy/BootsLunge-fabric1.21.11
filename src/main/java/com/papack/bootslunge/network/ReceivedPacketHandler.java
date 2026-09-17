package com.papack.bootslunge.network;

import com.papack.bootslunge.Bootslunge;
import com.papack.bootslunge.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.papack.bootslunge.Bootslunge.config;

public class ReceivedPacketHandler {

    private static final Map<UUID, Integer> LUNGE_COUNTS = new HashMap<>();

    public static void lungeActionHandler(LungePacketPayload payload, ServerPlayNetworking.Context context) {

        if (context.player() instanceof ServerPlayer player) {

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int level = Utils.getEnchantLevel(player, boots, Bootslunge.BOOTS_LUNGE);

            if (level <= 0) return;

            // 10tickの連射防止クールダウンチェック
            if (player.getCooldowns().isOnCooldown(boots)) return;
            // 水、マグマ、雪の判定
            // 滑空中はjumpモードは実行不可
            if (player.isFallFlying() && payload.request()) return;

            UUID playerId = player.getUUID();
            int currentCount = LUNGE_COUNTS.getOrDefault(playerId, 0);

            // 【重要】残りの使用回数をチェック (レベル回数以上なら空中での発動を拒否)
            if (currentCount >= level + 1) {
                return;
            }

            if (currentCount == 0) {
                player.resetFallDistance();
                player.trackStartFallingPosition();
            }

            Vec3 lungeVelocity;
            Vec3 directionPower;

            if (payload.request()) {

                // 1. 方向キー入力の有無で制御を変更
                boolean hasDirectionInput = payload.direction() != 0;

                // 水平方向のダッシュ力（無入力時は0、入力時はサッと大きく移動）
                double moveStrength = hasDirectionInput ? (config.directionJumpMoveStrengthBase + level * config.directionJumpMoveStrengthLevelMultiplier) : 0.0;

                // 視線角度（Yaw）のラジアン計算
                double yawRad = Math.toRadians(player.getYRot());
                Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
                Vec3 right = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad)).normalize();

                directionPower = switch (payload.direction()) {
                    case 1 -> forward.scale(moveStrength);                                    // 前進
                    case 2 -> right.scale(-moveStrength);                                   // 左
                    case 3 -> forward.subtract(right).normalize().scale(moveStrength);       // 左前
                    case 4 -> forward.scale(-moveStrength);                                  // 後方
                    case 6 -> forward.add(right).scale(-moveStrength).normalize().scale(moveStrength); // 左後
                    case 8 -> right.scale(moveStrength);                                     // 右
                    case 9 -> forward.add(right).normalize().scale(moveStrength);            // 右前
                    case 12 -> forward.scale(-1).add(right).normalize().scale(moveStrength); // 右後
                    default -> Vec3.ZERO;
                };

                // 2. Y軸上昇（ジャンプ力）の計算
                // 方向キー入力あり：わずかに浮き上がる程度（0.35〜0.4程度でふわっと低空維持）
                // 方向キー入力なし：従来のしっかりした2段ジャンプ（0.8〜）
                double jumpStrength;
                if (hasDirectionInput) {
                    jumpStrength = config.directionJumpMoveStrengthJumpStrength;
                } else {
                    jumpStrength = config.noDirectionJumpJumpStrengthBase + (level * config.noDirectionJumpJumpStrengthLevelMultiplier) + (currentCount * config.noDirectionJumpJumpStrengthCountMultiplier);
                }

                lungeVelocity = new Vec3(0, jumpStrength, 0).add(directionPower);

                player.resetFallDistance();
                player.trackStartFallingPosition();

            } else {
                // Lunge: 視線方向へ推進
                Vec3 lookVec = player.getForward();
                double strength = config.lungeJumpStrengthBase + (level * config.lungeJumpStrengthLevelMultiplier) + (currentCount * config.lungeJumpStrengthCountMultiplier);
                lungeVelocity = lookVec.scale(strength);
            }

            // 水、マグマ、雪に入っている場合
            boolean inLiquidOrSnow = (player.isInLiquid() || player.isInPowderSnow);

            Vec3 currentVelocity = player.getDeltaMovement();
            Vec3 newVelocity;

            if (inLiquidOrSnow) {
                // 水中では慣性が強すぎるため、既存の速度を加算せず、
                // 弱めた Lunge 速度のみに置き換える（または既存速度を強く減衰させてから足す）
                lungeVelocity = lungeVelocity.scale(config.inLiquidLungeVelocityDampingMultiplier); // 半減(0.5)よりもう少し落とす
                newVelocity = currentVelocity.scale(config.inLiquidCurrentVelocityDampingMultiplier).add(lungeVelocity);
            } else {
                newVelocity = currentVelocity.add(lungeVelocity);
            }

            // プレイヤーへ速度付与＆同期
            player.setDeltaMovement(newVelocity);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            // 使用回数を +1 して記録
            LUNGE_COUNTS.put(playerId, currentCount + 1);

            // クールダウンは常に5tick（連射防止用）
            player.getCooldowns().addCooldown(boots, 5);

            // サウンド再生
            player.level().playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LUNGE_1,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.5F);
        }
    }

    public static void resetCount(UUID playerId) {
        LUNGE_COUNTS.remove(playerId);
    }
}