package org.deymosko.lootroll.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "com.github.alexthe666.iceandfire.entity.EntityDragonBase")
public interface EntityDragonBaseAccessor {
    @Invoker("getRandomDrop")
    ItemStack callGetRandomDrop();
}
