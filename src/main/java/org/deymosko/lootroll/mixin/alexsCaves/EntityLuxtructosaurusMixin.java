package org.deymosko.lootroll.mixin.alexsCaves;


import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.entity.living.LuxtructosaurusEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.LootDropHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.github.alexmodguy.alexscaves.server.entity.living.SauropodBaseEntity.ANIMATION_EPIC_DEATH;

@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.living.LuxtructosaurusEntity")
public abstract class EntityLuxtructosaurusMixin
{

    @Accessor("deathItems")
    public abstract List<ItemStack> getDeathItems();

    @Accessor("collectedLoot")
    public abstract boolean getCollectedLoot();

    @Invoker("populateDeathLootForLuxtructosaurus")
    abstract void callpopulateDeathLootForLuxtructosaurus();


    @Inject(method = "tickDeath", at = @At("HEAD"), cancellable = true)
    private void onDeathTick(CallbackInfo ci)
    {
        if(Config.ALEXS_CAVES_INT.get())
        {
            LuxtructosaurusEntity entity = (LuxtructosaurusEntity) (Object) this;
            if (!entity.level().isClientSide && !this.getCollectedLoot()) {
                this.callpopulateDeathLootForLuxtructosaurus();
            }
            if (entity.getAnimation() == ANIMATION_EPIC_DEATH) {
                if (entity.getAnimationTick() >= 100 && entity.getAnimationTick() <= 110 && entity.level().isClientSide) {
                    for (int i = 0; i < 50; i++) {
                        entity.level().addAlwaysVisibleParticle(ACParticleRegistry.LUXTRUCTOSAURUS_ASH.get(), true, entity.getX(), entity.getY(), entity.getZ(), entity.getId(), 0, 0);
                    }
                }
                if (entity.getAnimationTick() > 110 && !entity.level().isClientSide() & !entity.isRemoved()) {
                    if (!this.getDeathItems().isEmpty()) {
                        ItemStack currentStack = ItemStack.EMPTY;
                        while (!this.getDeathItems().isEmpty() || !currentStack.isEmpty()) {
                            if (currentStack.isEmpty()) {
                                currentStack = getDeathItems().remove(0);
                            }
                            if (currentStack.getCount() > 0) {
                                ItemStack one = currentStack.copy();
                                one.setCount(1);
                                currentStack.shrink(1);
                                List<ItemStack> drops = new ArrayList<>();
                                drops.add(one);
                                LootDropHandler.startVoteSession(drops, entity);
                                drops.clear();
                            }
                        }

                    }
                    entity.level().broadcastEntityEvent(entity, (byte) 60);
                    entity.remove(Entity.RemovalReason.KILLED);
                }
            }
            return;
        }
    }
}
