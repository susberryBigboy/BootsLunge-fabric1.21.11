package com.papack.bootslunge.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class UtilsClient {

    /**
     * プレイヤーの足もと（直下 1～3 ブロック）に衝突可能なブロック（ハーフブロック、階段含む）があるか判定する
     *
     * @param player   対象のプレイヤー
     * @param distance 下方向にチェックする範囲（例: 3）
     * @return 衝突できるブロックが存在すれば true
     */
    public static boolean hasSolidBlockBelow(Player player, int distance) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition(); // プレイヤーの足もとの座標

        CollisionContext context = CollisionContext.of(player);

        for (int i = 1; i <= distance; i++) {
            BlockPos targetPos = playerPos.below(i);
            BlockState state = level.getBlockState(targetPos);

            // 衝突判定（CollisionShape）を持ち、通り抜けられないブロックかをチェック
            if (!state.getCollisionShape(level, targetPos, context).isEmpty()) {
                return true;
            }
        }

        return false;
    }
}