package com.papack.bootslunge;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class Utils {

    public static int getEnchantLevel(Player clientPlayer, ItemStack itemStack, ResourceKey<Enchantment> enchantments) {
        if (itemStack == null || itemStack.isEmpty()) return 0;

        Level world = clientPlayer.level();
        Holder<Enchantment> enchantment =
                world.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(enchantments);

        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);
    }
}
