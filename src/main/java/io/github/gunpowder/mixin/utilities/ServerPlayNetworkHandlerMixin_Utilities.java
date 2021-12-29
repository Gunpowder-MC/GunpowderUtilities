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

import io.github.gunpowder.api.components.ComponentsKt;
import io.github.gunpowder.entities.VanishComponent;
import io.github.gunpowder.mixin.cast.VanishedPlayerManager;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_Utilities {

    @Final
    @Shadow
    static Logger LOGGER;

    @Shadow
    public ServerPlayerEntity player;

    @Final
    @Shadow
    private MinecraftServer server;


    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    void noEmptyPlayerPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        // Disabled as getEntries is client-only
        //if (packet instanceof PlayerListS2CPacket && ((PlayerListS2CPacket) packet).getEntries().size() == 0) {
        //    ci.cancel();
        //}
    }

    @Inject(
            method = "onDisconnected(Lnet/minecraft/text/Text;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void noVanishDisconnectMessage(Text reason, CallbackInfo ci) {
        if(ComponentsKt.with(player, VanishComponent.class).isVanished()) {
            VanishedPlayerManager vanishedPlayerManager = (VanishedPlayerManager) this.server.getPlayerManager();
            vanishedPlayerManager.setVanishedCount(vanishedPlayerManager.getVanishedCount() - 1);
            this.player.onDisconnect();
            this.server.getPlayerManager().remove(this.player);
            if (this.server.isHost(this.player.getGameProfile())) {
                LOGGER.info("Stopping singleplayer server as player logged out");
                this.server.stop(false);
            }
            ci.cancel();
        }
    }
}
