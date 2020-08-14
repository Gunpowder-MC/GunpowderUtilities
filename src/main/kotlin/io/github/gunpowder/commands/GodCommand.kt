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
import com.mojang.brigadier.context.CommandContext
import io.github.gunpowder.api.builders.Command
import io.github.ladysnake.pal.Pal
import io.github.ladysnake.pal.VanillaAbilities
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText

object GodCommand {
    val ESSENTIALS_ABILITY_GOD = Pal.getAbilitySource("essentials", "godmode")

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("god") {
                requires { it.hasPermissionLevel(4) }

                executes(GodCommand::toggleGodSelf)
                argument("player", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }
                    executes(GodCommand::toggleGodOther)
                }
            }
        }
    }

    private fun toggleGodSelf(context: CommandContext<ServerCommandSource>): Int {
        // Set god
        toggleGod(context.source.player)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully toggled godmode"),
                false)

        return 1
    }

    private fun toggleGodOther(context: CommandContext<ServerCommandSource>): Int {
        // Get player
        val player = EntityArgumentType.getPlayer(context, "player")

        // Set god
        toggleGod(player)

        // Send feedback
        context.source.sendFeedback(
                LiteralText("Successfully toggled godmode for ${player.displayName.asString()}"),
                false)

        return 1
    }

    private fun toggleGod(player: ServerPlayerEntity) {
        if (ESSENTIALS_ABILITY_GOD.grants(player, VanillaAbilities.INVULNERABLE)) {
            ESSENTIALS_ABILITY_GOD.revokeFrom(player, VanillaAbilities.INVULNERABLE)
        } else {
            ESSENTIALS_ABILITY_GOD.grantTo(player, VanillaAbilities.INVULNERABLE)
        }
        player.sendAbilitiesUpdate()
    }
}
