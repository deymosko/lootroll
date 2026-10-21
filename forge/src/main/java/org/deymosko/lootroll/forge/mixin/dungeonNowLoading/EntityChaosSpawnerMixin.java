package org.deymosko.lootroll.forge.mixin.dungeonNowLoading;

import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.deymosko.lootroll.LootDropHandler;
import org.deymosko.lootroll.forge.mixin.LivingEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity")
public abstract class EntityChaosSpawnerMixin {

    @Unique
    private int lootroll$deathTime;

    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void lootroll$onTickDeath(CallbackInfo ci) {
        ChaosSpawnerEntity self = (ChaosSpawnerEntity) (Object) this;
        lootroll$deathTime++;

        if (lootroll$deathTime == 1) {
            self.setXRot(0.0F);
        }

        if (lootroll$deathTime >= 160 && !self.level().isClientSide() && !self.isRemoved()) {
            Level level = self.level();

            DamageSource damageSource = getDamageSource(self);
            if (damageSource == null) {
                damageSource = level.damageSources().generic();
                setDamageSource(self, damageSource);
            }

            Entity killer = damageSource.getEntity();
            LivingEntity killCredit = self.getKillCredit();

            int deathScore = getDeathScore(self);
            if (deathScore >= 0 && killCredit != null) {
                killCredit.awardKillScore(self, deathScore, damageSource);
            }

            if (level instanceof ServerLevel serverLevel) {
                if (killer == null || killer.killedEntity(serverLevel, self)) {
                    LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(self.getLootTable());
                    LootParams.Builder builder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.THIS_ENTITY, self)
                            .withParameter(LootContextParams.ORIGIN, self.position())
                            .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                            .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());

                    LootParams lootParams = builder.create(LootContextParamSets.ENTITY);

                    List<ItemStack> drops = lootTable.getRandomItems(lootParams, self.getLootTableSeed());
                    LootDropHandler.startVoteSession(drops, self);
                }
                level.broadcastEntityEvent(self, (byte) 3);
            }

            level.broadcastEntityEvent(self, (byte) 60);
            self.remove(Entity.RemovalReason.KILLED);

            ci.cancel();
        }
    }



    @Unique
    private DamageSource getDamageSource(ChaosSpawnerEntity self) {
        return ((ChaosSpawnerAccessor) self).lootroll$getKilledDamageSource();
    }

    @Unique
    private void setDamageSource(ChaosSpawnerEntity self, DamageSource source) {
        ((ChaosSpawnerAccessor) self).lootroll$setKilledDamageSource(source);
    }

    @Unique
    private int getDeathScore(ChaosSpawnerEntity self) {
        return ((LivingEntityAccessor) self).getDeathScore();
    }

    @Unique
    private void callDropAllDeathLoot(ChaosSpawnerEntity self, DamageSource source) {
        ((LivingEntityAccessor) self).callDropEquipment();
        ((LivingEntityAccessor) self).callDropExperience();
    }

    @Unique
    private void callCreateWitherRose(ChaosSpawnerEntity self, LivingEntity target) {
        ((LivingEntityAccessor) self).callCreateWitherRose(target);
    }
}
