package org.deymosko.lootroll.fabric;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Implemented by {@link org.deymosko.lootroll.fabric.mixin.LivingEntityMixin}; non-null while the entity's death drops are being captured. */
public interface DropCaptureHolder {
    List<ItemStack> lootroll$getCapturedDrops();
}
