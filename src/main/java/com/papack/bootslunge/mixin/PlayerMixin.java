package com.papack.bootslunge.mixin;

import com.papack.bootslunge.network.ReceivedPacketHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void bootsLunge$onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.level().isClientSide() && player.onGround()) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

            // ブーツのクールダウンが明けていて、しっかり着地している時のみカウントをリセットする
            if (!player.getCooldowns().isOnCooldown(boots)) {
                ReceivedPacketHandler.resetCount(player.getUUID());
            }
        }
    }
}