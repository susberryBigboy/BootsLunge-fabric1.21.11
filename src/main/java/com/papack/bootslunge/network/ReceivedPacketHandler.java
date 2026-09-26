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

    public static void lungeJumpAction(LungePacketPayload payload, ServerPlayNetworking.Context context) {

        if (context.player() instanceof ServerPlayer player) {

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            int level = Utils.getEnchantLevel(player, boots, Bootslunge.BOOTS_LUNGE);

            if (level <= 0) return;

            // Rapid-Fire Prevention Cooldown Check
            if (player.getCooldowns().isOnCooldown(boots)) return;
            // Jump mode cannot be activated while gliding
            if (player.isFallFlying() && payload.request()) return;

            UUID playerId = player.getUUID();
            int currentCount = LUNGE_COUNTS.getOrDefault(playerId, 0);

            // Check the number of remaining uses (if it exceeds the level limit, reject)
            if (currentCount >= level + 1) {
                return;
            }

            // Calculation of Thrust
            double adjustment = Math.clamp(payload.adjustment(), 0.1, 1.0);
            double baseStrength = (configServer.lungeBaseStrength
                    + (level * configServer.lungeLevelMultiplier)
                    + (currentCount * configServer.lungeCountMultiplier)) * adjustment;

            Vec3 lungeVelocity;

            if (payload.request()) {
                boolean hasDirectionInput = payload.direction() != 0;

                if (!hasDirectionInput) {
                    // [No Direction Jump] Activate straight up
                    lungeVelocity = new Vec3(0, baseStrength, 0);
                } else {
                    // [Directional Jump] Breakdown by Angle and Direction
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
                // [Look Direction Lunge (R Key)]
                Vec3 lookVec = player.getForward().normalize(); // Add the value for the Vanilla Jump
                lungeVelocity = lookVec.scale(baseStrength);
            }

            // Combining Speeds and Removing Duplicate Vanilla Jumps
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

            // Shift Key - Powerful braking
            if (payload.shiftDown()) {
                newVelocity = new Vec3(
                        newVelocity.x,
                        configServer.emergencyBrakeFloatVelocity,   // Raise it just a little to create a visual braking effect
                        newVelocity.z);
            }

            // Set it so that fall damage behaves similarly to when using Wind Charge
            player.setIgnoreFallDamageFromCurrentImpulse(true);
            player.currentImpulseImpactPos = player.position();
            player.resetFallDistance();

            //Grant Speed to Players & Synchronize
            player.setDeltaMovement(newVelocity);
            player.hurtMarked = true;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));

            // Increase the usage count by 1 and record it
            LUNGE_COUNTS.put(playerId, currentCount + 1);

            // Cooldown
            player.getCooldowns().addCooldown(boots, 5);

            // Sounds , Particles
            if (payload.sound()) {
                if (payload.shiftDown()) {
                    // Brake
                    player.level().playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WIND_CHARGE_BURST,
                            SoundSource.PLAYERS,
                            0.7F,
                            0.5F);
                } else {
                    // Normal
                    player.level().playSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WIND_CHARGE_BURST,
                            SoundSource.PLAYERS,
                            0.5F,
                            2.0F);
                }
            }

            if (payload.particle()) {
                if (payload.shiftDown()) {
                    // Brake
                    player.level().sendParticles(
                            ParticleTypes.EXPLOSION,
                            player.getX(), player.getY(), player.getZ(),
                            2,
                            0.2, 0.1, 0.2,
                            0.2
                    );
                } else {
                    // Normal
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
    }

    public static void resetCount(UUID playerId) {
        LUNGE_COUNTS.remove(playerId);
    }
}