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

import com.mojang.authlib.GameProfile;
import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.mixin.cast.VanishedPlayerManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin_Utilities implements VanishedPlayerManager {

    @Final
    @Shadow
    private List<ServerPlayerEntity> players;

    @Final
    @Shadow
    protected int maxPlayers;

    private int vanishedCount = 0;

    @Inject(
            method = "checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;canBypassPlayerLimit(Lcom/mojang/authlib/GameProfile;)Z"
            ),
            cancellable = true
    )
    private void checkCanJoinServer(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        PlayerManager manager = (PlayerManager) (Object) this;
        cir.setReturnValue(
                this.players.size() - ((VanishedPlayerManager) manager).getVanishedCount() >= this.maxPlayers && !manager.canBypassPlayerLimit(profile) ?
                        new TranslatableText("multiplayer.disconnect.server_full") :
                        null
        );

    }

    @Override
    public int getVanishedCount() {
        return this.vanishedCount;
    }

    @Override
    public void setVanishedCount(int vanishedCount) {
        this.vanishedCount = vanishedCount;
    }
}
