package org.deymosko.lootroll.forge.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("deathScore")
    int getDeathScore();

    @Accessor("deathScore")
    void setDeathScore(int score);
    @Accessor("lastHurtByPlayerTime")
    int getLastHurtByPlayerTime();


    @Accessor("lastHurtByPlayer")
    Player getLastHurtByPlayer();


    @Invoker("dropEquipment")
    void callDropEquipment();

    @Invoker("dropExperience")
    void callDropExperience();

    @Invoker("createWitherRose")
    void callCreateWitherRose(LivingEntity target);
}
