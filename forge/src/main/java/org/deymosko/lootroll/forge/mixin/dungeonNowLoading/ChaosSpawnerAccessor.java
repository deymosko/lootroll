package org.deymosko.lootroll.forge.mixin.dungeonNowLoading;


import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChaosSpawnerEntity.class, remap = false)
public interface ChaosSpawnerAccessor {
    @Accessor("killedDamageSource")
    DamageSource lootroll$getKilledDamageSource();

    @Accessor("killedDamageSource")
    void lootroll$setKilledDamageSource(DamageSource source);
}

