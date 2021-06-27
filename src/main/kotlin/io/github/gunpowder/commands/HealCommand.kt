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
import io.github.gunpowder.api.util.TranslatedText
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

object HealCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("heal") {
                permission("utilities.heal.self")
                executes(HealCommand::healSelf)

                argument("player", EntityArgumentType.player()) {
                    permission("utilities.heal.other")
                    executes(HealCommand::healOther)
                }
            }
        }
    }

    private fun healSelf(context: CommandContext<ServerCommandSource>): Int {
        healPlayer(context.source.player)
        context.source.sendFeedback(
                TranslatedText("gunpowder_utilities.heal.self").translateTextForPlayer(context.source.player),
                false)
        return 1
    }

    private fun healOther(context: CommandContext<ServerCommandSource>): Int {
        val player = EntityArgumentType.getPlayer(context, "player")
        healPlayer(player)
        context.source.sendFeedback(
                TranslatedText("gunpowder_utilities.heal.other",player.displayName.asString()).translateTextForPlayer(context.source.player),
                false)
        return 1
    }

    private fun healPlayer(player: ServerPlayerEntity) {
        player.health = player.maxHealth;
        player.hungerManager.foodLevel = 20;
        player.extinguish();
        player.clearStatusEffects();
    }
}
