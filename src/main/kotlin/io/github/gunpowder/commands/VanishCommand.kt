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

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.mixin.cast.PlayerVanish
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object VanishCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("vanish") {
                requires { it.hasPermissionLevel(4) }
                executes { vanish(it.source.player) }
                //TODO: Implement for offline players.
                argument("target", EntityArgumentType.player()) {
                    argument("set", BoolArgumentType.bool()) {
                        executes {
                            vanish(
                                    EntityArgumentType.getPlayer(it, "target"),
                                    BoolArgumentType.getBool(it, "set"),
                                    it.source.player
                            )
                        }
                    }
                    executes { vanish(EntityArgumentType.getPlayer(it, "target"), it.source.player) }
                }
            }
        }
    }

    private fun vanish(target: ServerPlayerEntity) = vanish(target, !(target as PlayerVanish).isVanished)

    private fun vanish(target: ServerPlayerEntity, player: ServerPlayerEntity) = vanish(target, !(target as PlayerVanish).isVanished, player)

    private fun vanish(player: ServerPlayerEntity, set: Boolean, target: ServerPlayerEntity = player): Int {
        (target as PlayerVanish).isVanished = set
        if (player == target)
            player.sendMessage(
                    LiteralText(
                            if (set)
                                "Puff! You have vanished from the world."
                            else
                                "You are no longer vanished."
                    ),
                    true
            )
        else
            target.sendMessage(
                    LiteralText("${player.entityName} set vanish to $set for you!"),
                    true
            )

        return if (set) 1 else 0
    }
}
