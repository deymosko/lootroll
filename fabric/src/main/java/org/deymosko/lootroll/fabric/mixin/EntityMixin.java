package org.deymosko.lootroll.fabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.deymosko.lootroll.fabric.DropCaptureHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"), cancellable = true)
    private void lootroll$captureDrop(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        if ((Object) this instanceof DropCaptureHolder holder) {
            List<ItemStack> captured = holder.lootroll$getCapturedDrops();
            if (captured != null) {
                if (!stack.isEmpty()) {
                    captured.add(stack.copy());
                }
                cir.setReturnValue(null);
            }
        }
    }
}
