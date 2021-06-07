/*
 * MIT License
 *
 * Copyright (c) 2020 GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder.mixin.utilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin_Utilities extends AbstractRecipeScreenHandler<CraftingInventory> {
    @Shadow @Final private ScreenHandlerContext context;

    public CraftingScreenHandlerMixin_Utilities(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Shadow @Final private PlayerEntity player;

    @Shadow @Final private CraftingInventory input;

    @Shadow @Final private CraftingResultInventory result;

    @Shadow public abstract void close(PlayerEntity player);

    @Shadow protected static void updateResult(ScreenHandler screenHandler, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
    }

    @Inject(method = "onContentChanged", at = @At(value = "HEAD", target = "Lnet/minecraft/screen/CraftingScreenHandler;onContentChanged(Lnet/minecraft/inventory/Inventory;)V"), cancellable = true)
    private void modifyContentChanged(Inventory inventory, CallbackInfo ci) {
        if (context == ScreenHandlerContext.EMPTY) {
            updateResult(this, this.player.getEntityWorld(), this.player, this.input, this.result);
            ci.cancel();
        }
    }

    @Inject(method = "close", at = @At(value = "HEAD", target = "Lnet/minecraft/screen/CraftingScreenHandler;close(Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void modifyClose(PlayerEntity player, CallbackInfo ci) {
        if (context == ScreenHandlerContext.EMPTY) {
            super.close(player);
            this.dropInventory(player, input);
            ci.cancel();
        }
    }
}
