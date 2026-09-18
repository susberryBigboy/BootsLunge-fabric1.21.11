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

            if (payload.request()) {

                boolean hasDirectionInput = payload.direction() != 0;

                // 1. 従来の基準値をそれぞれ計算
                double baseMoveStrength = hasDirectionInput
                        ? (configServer.directionJumpMoveStrengthBase + level * configServer.directionJumpMoveStrengthLevelMultiplier)
                        : 0.0;

                double baseJumpStrength = hasDirectionInput
                        ? configServer.directionJumpMoveStrengthJumpStrength
                        : (configServer.noDirectionJumpJumpStrengthBase + (level * configServer.noDirectionJumpJumpStrengthLevelMultiplier) + (currentCount * configServer.noDirectionJumpJumpStrengthCountMultiplier));

                // 2. angle (0°〜90°) に応じた重み付け（ラジアン変換）
                // 0° のとき: 水平 100% / Y軸 0%
                // 90° のとき: 水平 0% / Y軸 100%
                double pitchRad = Math.toRadians(Math.clamp(payload.angle(), 0.0, 90.0));

                double horizontalScale = Math.cos(pitchRad); // 0°で1.0、90°で0.0
                double verticalScale = Math.sin(pitchRad);   // 0°で0.0、90°で1.0

                // 3. 従来と同じパワー感を維持したまま角度を適用
                double finalMoveStrength = baseMoveStrength * horizontalScale;
                double finalJumpStrength = baseJumpStrength * (hasDirectionInput ? verticalScale : 1.0);

                if (!hasDirectionInput) {
                    lungeVelocity = new Vec3(0, finalJumpStrength, 0);
                } else {
                    double yawRad = Math.toRadians(player.getYRot());
                    Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
                    Vec3 right = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad)).normalize();

                    Vec3 horizDir = switch (payload.direction()) {
                        case 1 -> forward;
                        case 2 -> right.scale(-1);
                        case 3 -> forward.subtract(right).normalize();
                        case 4 -> forward.scale(-1);
                        case 6 -> forward.add(right).scale(-1).normalize();
                        case 8 -> right;
                        case 9 -> forward.add(right).normalize();
                        case 12 -> forward.scale(-1).add(right).normalize();
                        default -> Vec3.ZERO;
                    };

                    lungeVelocity = horizDir.scale(finalMoveStrength).add(0, finalJumpStrength, 0);
                }

                player.resetFallDistance();
                player.trackStartFallingPosition();

            } else {
                // Lunge: 視線方向へ推進
                Vec3 lookVec = player.getForward();
                double strength = configServer.lungeJumpStrengthBase + (level * configServer.lungeJumpStrengthLevelMultiplier) + (currentCount * configServer.lungeJumpStrengthCountMultiplier);
                lungeVelocity = lookVec.scale(strength);
            }

            // 水、マグマ、雪に入っている場合
            boolean inLiquidOrSnow = (player.isInLiquid() || player.isInPowderSnow);

            Vec3 currentVelocity = player.getDeltaMovement();
            Vec3 newVelocity;

            if (inLiquidOrSnow) {
                // 水中では慣性が強すぎるため、既存の速度を加算せず、
                // 弱めた Lunge 速度のみに置き換える（または既存速度を強く減衰させてから足す）
                lungeVelocity = lungeVelocity.scale(configServer.inLiquidLungeVelocityDampingMultiplier); // 半減(0.5)よりもう少し落とす
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

            // Cooldown (Prevent consecutive execution)
            player.getCooldowns().addCooldown(boots, 5);

            // Sounds
            if (payload.sound()) {
                player.level().playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LUNGE_1,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.5F);
            }

            // Particles
            if (payload.sound()) {
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