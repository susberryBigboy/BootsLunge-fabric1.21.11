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

            // =========================================================
            // 1. 純粋な推進力の計算（補正なしの統一値）
            // =========================================================
            double baseStrength = configServer.lungeBaseStrength
                    + (level * configServer.lungeLevelMultiplier)
                    + (currentCount * configServer.lungeCountMultiplier);

            Vec3 lungeVelocity;

            if (payload.request()) {
                boolean hasDirectionInput = payload.direction() != 0;

                if (!hasDirectionInput) {
                    // 【No Direction Jump】真上へ発動
                    lungeVelocity = new Vec3(0, baseStrength, 0);
                } else {
                    // 【Directional Jump】角度と方向に沿って分解
                    double pitchRad = Math.toRadians(Math.clamp(payload.angle(), 0.0, 90.0));
                    double horizontalScale = Math.cos(pitchRad);
                    double verticalScale = Math.sin(pitchRad);

                    double yawRad = Math.toRadians(player.getYRot());
                    Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
                    Vec3 right = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad)).normalize();

                    Vec3 rawDir = switch (payload.direction()) {
                        case 1 -> forward;
                        case 2 -> right.reverse();
                        case 3 -> forward.subtract(right);
                        case 4 -> forward.reverse();
                        case 6 -> forward.reverse().subtract(right);
                        case 8 -> right;
                        case 9 -> forward.add(right);
                        case 12 -> forward.reverse().add(right);
                        default -> Vec3.ZERO;
                    };

                    Vec3 horizDir = rawDir.lengthSqr() > 0 ? rawDir.normalize() : Vec3.ZERO;
                    Vec3 combinedDir = horizDir.scale(horizontalScale).add(0, verticalScale, 0);

                    lungeVelocity = combinedDir.scale(baseStrength);
                }

            } else {
                // 【視線方向 Lunge (Rキー)】
                Vec3 lookVec = player.getForward().normalize(); // バニラジャンプ分の値を加算
                lungeVelocity = lookVec.scale(baseStrength);
            }

            // =========================================================
            // 2. 速度の合成とバニラジャンプ重複の除去
            // =========================================================
            boolean inLiquidOrSnow = (player.isInLiquid() || player.isInPowderSnow);

            Vec3 velocity = player.getDeltaMovement();
            Vec3 currentVelocity = new Vec3(velocity.x, Math.min(0, velocity.y), velocity.z);
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

            // fallDistanceをリセット
            player.resetFallDistance();
            player.currentImpulseImpactPos = player.position();
            player.trackStartFallingPosition();

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