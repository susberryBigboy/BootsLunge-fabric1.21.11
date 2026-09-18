package com.papack.bootslunge.network;

import com.papack.bootslunge.Bootslunge;
import com.papack.bootslunge.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
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

import static com.papack.bootslunge.Bootslunge.configServer;

public class ReceivedPacketHandler {

    private static final Map<UUID, Integer> LUNGE_COUNTS = new HashMap<>();

    public static void lungeActionHandler(LungePacketPayload payload, ServerPlayNetworking.Context context) {

        if (context.player() instanceof ServerPlayer player) {

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int level = Utils.getEnchantLevel(player, boots, Bootslunge.BOOTS_LUNGE);

            if (level <= 0) return;

            // 10tickの連射防止クールダウンチェック
            if (player.getCooldowns().isOnCooldown(boots)) return;
            // 滑空中はjumpモードは実行不可
            if (player.isFallFlying() && payload.request()) return;

            UUID playerId = player.getUUID();
            int currentCount = LUNGE_COUNTS.getOrDefault(playerId, 0);

            // 残りの使用回数をチェック (レベル回数以上なら拒否)
            if (currentCount >= level + 1) {
                return;
            }

            if (currentCount == 0) {
                player.resetFallDistance();
                player.trackStartFallingPosition();
            }

            // =========================================================
            // 推進力の統一計算
            // =========================================================
            // 全モード共通の基本パワーを計算 (レベルと使用回数で拡張)
            double baseStrength = configServer.lungeBaseStrength
                    + (level * configServer.lungeLevelMultiplier)
                    + (currentCount * configServer.lungeCountMultiplier);

            Vec3 lungeVelocity;

            if (payload.request()) {
                // WASD + Space (または Ctrl+Space) / Spaceのみ
                boolean hasDirectionInput = payload.direction() != 0;

                if (!hasDirectionInput) {
                    // 【No Direction Jump】真上(Y軸)へ全パワーを加算
                    lungeVelocity = new Vec3(0, baseStrength, 0);
                } else {
                    // 【Directional Jump】角度(angle: 0〜90)とWASD入力方向へパワーを分解
                    double pitchRad = Math.toRadians(Math.clamp(payload.angle(), 0.0, 90.0));
                    double horizontalScale = Math.cos(pitchRad); // 水平方向倍率
                    double verticalScale = Math.sin(pitchRad);   // Y軸(垂直)方向倍率

                    // プレイヤーの視線（Yaw）に基づく「前(forward)」と「右(right)」の単位ベクトル
                    double yawRad = Math.toRadians(player.getYRot());
                    Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
                    Vec3 right = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad)).normalize();

                    // 方向キーに応じて「生の方向」を合成し、最後に必ず1回だけ normalize() する
                    Vec3 rawDir = switch (payload.direction()) {
                        case 1 -> forward;                             // 前
                        case 2 -> right.reverse();                     // 左
                        case 3 -> forward.subtract(right);             // 左前
                        case 4 -> forward.reverse();                   // 後
                        case 6 -> forward.reverse().subtract(right);   // 左後
                        case 8 -> right;                               // 右
                        case 9 -> forward.add(right);                  // 右前
                        case 12 -> forward.reverse().add(right);        // 右後
                        default -> Vec3.ZERO;
                    };

                    // 確実に長さを 1.0 に正規化
                    Vec3 horizDir = rawDir.lengthSqr() > 0 ? rawDir.normalize() : Vec3.ZERO;

                    // 水平方向(horizDir * horizontalScale) と 垂直方向(Y * verticalScale) を合成
                    // (horizontalScale^2 + verticalScale^2 = 1.0 になるため、combinedDir の長さも正確に 1.0 になります)
                    Vec3 combinedDir = horizDir.scale(horizontalScale).add(0, verticalScale, 0);
                    lungeVelocity = combinedDir.scale(baseStrength);
                }

                player.resetFallDistance();
                player.trackStartFallingPosition();

            } else {
                // 【視線方向 Lunge (Rキー)】視線単位ベクトル (LookVector) に全パワーを加算
                Vec3 lookVec = player.getForward().normalize();
                lungeVelocity = lookVec.scale(baseStrength);
            }

            // =========================================================
            // 水、マグマ、雪の減衰処理（既存仕様の維持）
            // =========================================================
            boolean inLiquidOrSnow = (player.isInLiquid() || player.isInPowderSnow);

            Vec3 currentVelocity = player.getDeltaMovement();
            Vec3 newVelocity;

            if (inLiquidOrSnow) {
                lungeVelocity = lungeVelocity.scale(configServer.inLiquidLungeVelocityDampingMultiplier);
                newVelocity = currentVelocity.scale(configServer.inLiquidCurrentVelocityDampingMultiplier).add(lungeVelocity);
            } else {
                newVelocity = currentVelocity.add(lungeVelocity);
            }

            // プレイヤーへ速度付与＆同期
            player.setDeltaMovement(newVelocity);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            // 使用回数を +1 して記録
            LUNGE_COUNTS.put(playerId, currentCount + 1);

            // クールダウン (5tick)
            player.getCooldowns().addCooldown(boots, 5);

            // 効果音・パーティクル処理
            if (payload.sound()) {
                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LUNGE_1,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.5F);
            }

            if (payload.particle()) {
                player.level().sendParticles(
                        ParticleTypes.CLOUD,
                        player.getX(), player.getY(), player.getZ(),
                        3,
                        0.1, 0.1, 0.1,
                        0.05
                );
            }
        }
    }

    public static void resetCount(UUID playerId) {
        LUNGE_COUNTS.remove(playerId);
    }
}