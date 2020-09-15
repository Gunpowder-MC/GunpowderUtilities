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
import io.github.gunpowder.mixin.cast.PlayerVanish;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(QueryResponseS2CPacket.class)
public class QueryResponseS2CPacket_Utilities {

    @Shadow
    private ServerMetadata metadata;

    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"))
    private void excludeVanished(PacketByteBuf buf, CallbackInfo ci) {

        // List where "faked" GameProfiles will be stored (all the players except vanished)
        List<GameProfile> fakedGameProfiles = new ArrayList<>();
        // Original player list
        List<ServerPlayerEntity> playerList = GunpowderMod.getInstance().getServer().getPlayerManager().getPlayerList();

        // Excluding vanished players
        for (ServerPlayerEntity player : playerList) {
            if (((PlayerVanish) player).isVanished()) {
                continue;
            }

            fakedGameProfiles.add(player.getGameProfile());
        }


        ServerMetadata.Players fakedPlayers = new ServerMetadata.Players(GunpowderMod.getInstance().getServer().getMaxPlayerCount(), fakedGameProfiles.size());

        GameProfile[] gameProfiles = fakedGameProfiles.toArray(new GameProfile[0]);
        fakedPlayers.setSample(gameProfiles);

        // Setting fake players to metadata
        metadata.setPlayers(fakedPlayers);
    }
}
