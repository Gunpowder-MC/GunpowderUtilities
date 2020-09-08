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
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.api.builders.Text
import net.minecraft.item.Wearable
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand

object HatCommand {
    private val ILLEGAL_ITEM_EXCEPTION = SimpleCommandExceptionType(LiteralText("You can't use that item as a hat!"))

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("hat") {
                executes(HatCommand::execute)
            }
        }
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        val hand = player.mainHandStack
        if (hand.item is Wearable) {
            throw ILLEGAL_ITEM_EXCEPTION.create()
        }

        val head = player.inventory.armor[3]

        if (hand.isEmpty) {
            player.sendMessage(LiteralText("Put an item in your hand first!"), false)
            return -1
        }

        player.setStackInHand(Hand.MAIN_HAND, head)
        player.inventory.armor[3] = hand
        player.sendMessage(
                Text.builder {
                    text("Enjoy your new ")
                    text(TranslatableText(hand.item.translationKey)) {
                        color(Formatting.YELLOW)
                    }
                    text(" hat!")
                },
                true
        )
        return 1
    }
}
