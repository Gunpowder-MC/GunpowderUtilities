package io.github.gunpowder.mixin.utilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin_Utilities {
    @Inject(method = "canPlayerUse", at=@At("HEAD"), cancellable = true)
    void alwaysAccess(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
