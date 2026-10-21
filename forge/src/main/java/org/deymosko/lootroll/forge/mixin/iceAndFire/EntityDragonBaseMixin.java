package org.deymosko.lootroll.forge.mixin.iceAndFire;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.deymosko.lootroll.Config;
import org.deymosko.lootroll.LootDropHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.github.alexthe666.iceandfire.entity.EntityDragonBase")
public abstract class EntityDragonBaseMixin {

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void lootrollInteract(Player player, Vec3 vec, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this;
        if(Config.ICE_AND_FIRE_INT.get())
        {
            RandomSource random = RandomSource.create();
            int lastDeathStage = Math.min(dragon.getAgeInDays() / 5, 25);
            ItemStack stack = player.getItemInHand(hand);
            if(!dragon.level().isClientSide)
            {
                ServerLevel serverLevel = (ServerLevel) dragon.level();


                if (dragon.isModelDead() && dragon.getDeathStage() < lastDeathStage && player.mayBuild()) {
                    if (!dragon.level().isClientSide && !stack.isEmpty() && stack.getItem() != null && stack.getItem() == Items.GLASS_BOTTLE && dragon.getDeathStage() < lastDeathStage / 2 && IafConfig.dragonDropBlood) {
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }

                        dragon.setDeathStage(dragon.getDeathStage() + 1);
                        player.getInventory().add(new ItemStack(dragon.getBloodItem(), 1));
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    } else {
                        if (!dragon.level().isClientSide && stack.isEmpty() && IafConfig.dragonDropSkull) {
                            if (dragon.getDeathStage() >= lastDeathStage - 1) {
                                List<ItemStack> drops = new ArrayList<>();
                                ItemStack skull = dragon.getSkull().copy();
                                skull.setTag(new CompoundTag());
                                skull.getTag().putInt("Stage", dragon.getDragonStage());
                                skull.getTag().putInt("DragonType", 0);
                                skull.getTag().putInt("DragonAge", dragon.getAgeInDays());
                                dragon.setDeathStage(dragon.getDeathStage() + 1);
                                if (!dragon.level().isClientSide) {
                                    drops.add(skull);
                                    LootDropHandler.startVoteSession(drops, dragon);
                                }

                                dragon.remove(Entity.RemovalReason.DISCARDED);
                            } else if (dragon.getDeathStage() == lastDeathStage / 2 - 1 && IafConfig.dragonDropHeart) {
                                ItemStack heart = new ItemStack(dragon.getHeartItem(), 1);
                                ItemStack egg = new ItemStack(dragon.getVariantEgg(random.nextInt(4)), 1);
                                List<ItemStack> drops = new ArrayList<>();

                                if (!dragon.level().isClientSide) {
                                    drops.add(heart);
                                    if (!dragon.isMale() && dragon.getDragonStage() > 3) {
                                        drops.add(egg);
                                    }
                                    LootDropHandler.startVoteSession(drops, dragon);
                                }
                                dragon.setDeathStage(dragon.getDeathStage() + 1);
                            } else {
                                dragon.setDeathStage(dragon.getDeathStage() + 1);
                                List<ItemStack> drops = new ArrayList<>();
                                ItemStack drop = ((EntityDragonBaseAccessor) dragon).callGetRandomDrop();
                                drops.add(drop);
                                if (!drop.isEmpty() && !dragon.level().isClientSide) {
                                    LootDropHandler.startVoteSession(drops, dragon);
                                }
                            }
                        }
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }

}
