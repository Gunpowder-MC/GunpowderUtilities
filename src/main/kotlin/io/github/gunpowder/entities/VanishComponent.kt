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

package io.github.gunpowder.entities

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.components.Component
import io.github.gunpowder.mixin.cast.VanishedPlayerManager
import io.github.gunpowder.mixin.utilities.ThreadedAnvilChunkStorageAccessor_Utilities
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.MessageType
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.EntityTrackingListener
import net.minecraft.server.world.ServerChunkManager
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import java.util.function.Consumer

class VanishComponent : Component<ServerPlayerEntity>() {
    var _isVanished: Boolean = false

    override fun fromNbt(tag: NbtCompound) {
        setVanished(tag.getBoolean("gp:vanish"))
    }

    override fun writeNbt(tag: NbtCompound) {
        tag.putBoolean("gp:vanish", isVanished())
    }

    fun isVanished(): Boolean {
        return _isVanished
    }

    fun setVanished(enabled: Boolean) {
        if (_isVanished == enabled) {
            // Same state, do nothing
            return
        }

        _isVanished = enabled
        bound.isInvisible = enabled

        // Sending player ADD/REMOVE packet
        // This updates the tablist

        val playerManager = GunpowderMod.instance.server.playerManager
        playerManager.sendToAll(
            PlayerListS2CPacket(
                if (enabled) PlayerListS2CPacket.Action.REMOVE_PLAYER else PlayerListS2CPacket.Action.ADD_PLAYER,
                bound
            )
        )

        // Updating vanished count
        val vanishedPlayerManager = playerManager as VanishedPlayerManager
        val vanishedCount = vanishedPlayerManager.vanishedCount
        vanishedPlayerManager.vanishedCount = if (enabled) vanishedCount + 1 else vanishedCount - 1

        val storage = (bound.world.chunkManager as ServerChunkManager).threadedAnvilChunkStorage
        val trackerEntry = (storage as ThreadedAnvilChunkStorageAccessor_Utilities).entityTrackers[bound.id]

        // Starting / stopping the player tracking
        // This actually removes the player (otherwise hacked clients still see it with certain hacks)
        trackerEntry?.playersTracking?.forEach(
            Consumer { tracking: EntityTrackingListener ->
                if (enabled) trackerEntry.entry.stopTracking(tracking.player) else trackerEntry.entry.startTracking(tracking.player)
            }
        )

        // Faking leave - join message
        val msg = TranslatableText(
            if (enabled) "multiplayer.player.left" else "multiplayer.player.joined",
            bound.displayName
        )
        playerManager.broadcast(msg.formatted(Formatting.YELLOW), MessageType.SYSTEM, Util.NIL_UUID)
    }
}
