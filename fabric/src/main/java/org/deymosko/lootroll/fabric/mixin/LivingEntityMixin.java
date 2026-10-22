package org.deymosko.lootroll.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.LootDropHandler;
import org.deymosko.lootroll.fabric.DropCaptureHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric has no LivingDropsEvent, so capture everything spawned through
 * spawnAtLocation while a tracked entity drops its death loot.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements DropCaptureHolder {

    @Unique
    private List<ItemStack> lootroll$capturedDrops;

    @Override
    public List<ItemStack> lootroll$getCapturedDrops() {
        return lootroll$capturedDrops;
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void lootroll$startCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if (LootDropHandler.isTracked((LivingEntity) (Object) this)) {
            lootroll$capturedDrops = new ArrayList<>();
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
    private void lootroll$finishCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
        List<ItemStack> drops = lootroll$capturedDrops;
        lootroll$capturedDrops = null;
        if (drops != null && !drops.isEmpty()) {
            LootDropHandler.onEntityDrops((LivingEntity) (Object) this, drops);
        }
    }
}
