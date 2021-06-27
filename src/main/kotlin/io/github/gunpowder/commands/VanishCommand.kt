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
import io.github.gunpowder.api.components.with
import io.github.gunpowder.api.util.TranslatedText
import io.github.gunpowder.entities.VanishComponent
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Formatting

object VanishCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("vanish") {
                permission("utilities.vanish", 4)
                literal("toggle") {
                    executes(VanishCommand::toggleVanish)
                }
                executes (VanishCommand::displayVanishInfo)
            }
        }
    }

    private fun toggleVanish(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player.with<VanishComponent>()
        player.setVanished(!player.isVanished())

        // Sending info to player
        ctx.source.player.sendMessage(
                TranslatedText(
                        if (player.isVanished())
                            "gunpowder_utilities.vanish.toggle.on"
                        else
                            "gunpowder_utilities.vanish.toggle.off"
                ).translateTextForPlayer(ctx.source.player).formatted(Formatting.AQUA),
                true
        )
        return 1
    }

    private fun displayVanishInfo(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player
        player.sendMessage(
                TranslatedText(
                        if (player.with<VanishComponent>().isVanished())
                            "gunpowder_utilities.vanish.info.on"
                        else
                            "gunpowder_utilities.vanish.info.off"
                ).translateTextForPlayer(ctx.source.player).formatted(Formatting.AQUA),
                true
        )
        return 1
    }
}
