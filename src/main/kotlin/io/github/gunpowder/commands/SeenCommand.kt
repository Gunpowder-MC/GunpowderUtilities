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