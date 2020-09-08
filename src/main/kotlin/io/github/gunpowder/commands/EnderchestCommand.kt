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
import io.github.gunpowder.api.builders.Command
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.EnderChestInventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

object EnderchestCommand {
    private val containerName
        get() = TranslatableText("container.enderchest")

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("enderchest", "ec") {
                executes { open(it.source.player) }
                //TODO: Implement for offline player.
                argument("target", EntityArgumentType.player()) {
                    requires { it.hasPermissionLevel(4) }
                    executes { open(it.source.player, EntityArgumentType.getPlayer(it, "target")) }
                }
            }
        }
    }

    private fun open(player: ServerPlayerEntity, target: ServerPlayerEntity = player): Int {
        val enderChestInventory: EnderChestInventory = target.enderChestInventory
        player.openHandledScreen(SimpleNamedScreenHandlerFactory({ i: Int,
                                                                   playerInventory: PlayerInventory?, _: PlayerEntity? ->
            GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, enderChestInventory)
        },
                if (player == target) containerName
                else containerName.append(" ").append(LiteralText(target.entityName).formatted(Formatting.YELLOW))
        ))

        player.incrementStat(Stats.OPEN_ENDERCHEST)
        return 1
    }
}
