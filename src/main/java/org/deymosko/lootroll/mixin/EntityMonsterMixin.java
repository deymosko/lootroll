package org.deymosko.lootroll.mixin;

import net.minecraft.world.entity.monster.Monster;
import org.checkerframework.common.reflection.qual.Invoke;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Monster.class)
public interface EntityMonsterMixin
{
    @Invoker("shouldDropLoot")
    boolean shouldDropLoot();
}
