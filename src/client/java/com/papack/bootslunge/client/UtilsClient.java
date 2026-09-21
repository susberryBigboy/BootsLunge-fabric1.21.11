package com.papack.bootslunge.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class UtilsClient {

    /**
     * Checks whether there are any collidable blocks (including half-blocks and stairs) at the player's feet (1–3 blocks directly below).
     *
     * @param player   Target Player
     * @param distance Range to check downward (e.g., 3)
     * @return True if there are blocks that can collide
     */
    public static boolean hasSolidBlockBelow(Player player, int distance) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition(); // player foot coordinates
        int yv = (int) Math.ceil(Math.abs(player.getDeltaMovement().y()));
        int below = Math.max(yv, distance);

        CollisionContext context = CollisionContext.of(player);

        for (int i = 1; i <= below; i++) {
            BlockPos targetPos = playerPos.below(i);
            BlockState state = level.getBlockState(targetPos);

            // Checks whether a block has a collision shape (CollisionShape) and cannot be passed through
            if (!state.getCollisionShape(level, targetPos, context).isEmpty()) {
                return true;
            }
        }

        return false;
    }
}