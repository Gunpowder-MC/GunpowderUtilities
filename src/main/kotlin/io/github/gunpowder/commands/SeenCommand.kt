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

package io.github.gunpowder.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.api.exposed.PlayerTable
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import org.jetbrains.exposed.sql.*
import java.time.format.DateTimeFormatter

object SeenCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("seen") {
                permission("utilities.seen", 0)
                argument("player", GameProfileArgumentType.gameProfile()) {
                    executes(::seen)
                }
            }
        }
    }

    private fun seen(context: CommandContext<ServerCommandSource>): Int {
        val profiles = GameProfileArgumentType.getProfileArgument(context, "player")
        val profile = profiles.first()

        GunpowderMod.instance.database.transaction {
            val row = PlayerTable.select { PlayerTable.id.eq(profile.id) }.firstOrNull()
            row?.get(PlayerTable.lastSeen)
        }.thenAccept {
            if (it == null) {
                context.source.sendError(
                    LiteralText(
                        "Unknown player"
                    )
                )
            } else {
                val server = context.source.minecraftServer
                var offline = false
                val player = server.playerManager.getPlayer(profile.id) ?: ServerPlayerEntity(server, server.overworld, profile).also { p ->
                    p.readCustomDataFromNbt(server.playerManager.loadPlayerData(p))
                    offline = true
                }
                context.source.sendFeedback(
                    LiteralText(
                        "Player ${profile.name} was last seen on ${
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                                it
                            )
                        } at ${player.pos}"
                    ), false
                )

                if (offline) {
                    server.playerManager.remove(player)
                }
            }
        }

        return 1
    }
}