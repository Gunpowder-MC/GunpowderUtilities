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
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.builders.Command
import io.github.gunpowder.api.util.TranslatedText
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.*
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.network.ServerPlayerInteractionManager
import net.minecraft.util.collection.DefaultedList

object InvseeCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Command.builder(dispatcher) {
            command("invsee") {
                requires { it.hasPermissionLevel(4) }
                argument("player", GameProfileArgumentType.gameProfile()) {
                    executes(InvseeCommand::execute)

                    literal("enderchest", "echest") {
                        executes(InvseeCommand::executeEnder)
                    }
                }
            }
        }
    }

    private fun executeEnder(context: CommandContext<ServerCommandSource>): Int {
        val server = GunpowderMod.instance.server
        val profile = GameProfileArgumentType.getProfileArgument(context, "player").first()

        var offline = false
        val player = server.playerManager.getPlayer(profile.id) ?: ServerPlayerEntity(server, server.overworld, profile).also {
            server.playerManager.loadPlayerData(it)
            offline = true
        }

        context.source.player.openHandledScreen(SimpleNamedScreenHandlerFactory({ i: Int, playerInventory: PlayerInventory?, _: PlayerEntity? ->
            GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i, playerInventory, player.enderChestInventory, 3).apply {
                addListener(object : ScreenHandlerListener {
                    override fun onSlotUpdate(handler: ScreenHandler?, slotId: Int, stack: ItemStack?) {
                        // save
                        if (offline) {
                            server.playerManager.playerList.add(player)
                            server.playerManager.saveAllPlayerData()
                            server.playerManager.playerList.remove(player)
                        }
                    }

                    override fun onPropertyUpdate(handler: ScreenHandler?, property: Int, value: Int) {
                        // pass
                    }

                })
            }
        }, TranslatedText("gunpowder_utilities.invsee.title_enderchest", player.entityName).translateTextForPlayer(context.source.player)))

        return 1
    }

    private fun execute(context: CommandContext<ServerCommandSource>): Int {
        val server = GunpowderMod.instance.server
        val profile = GameProfileArgumentType.getProfileArgument(context, "player").first()

        var offline = false
        val player = server.playerManager.getPlayer(profile.id) ?: ServerPlayerEntity(server, server.overworld, profile).also {
            server.playerManager.loadPlayerData(it)
            offline = true
        }

        context.source.player.openHandledScreen(SimpleNamedScreenHandlerFactory({ i: Int, playerInventory: PlayerInventory?, _: PlayerEntity? ->
            GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, i, playerInventory, player.inventory, 4).apply {
                addListener(object : ScreenHandlerListener {
                    override fun onSlotUpdate(handler: ScreenHandler?, slotId: Int, stack: ItemStack?) {
                        // save
                        if (offline) {
                            server.playerManager.playerList.add(player)
                            server.playerManager.saveAllPlayerData()
                            server.playerManager.playerList.remove(player)
                        }
                    }

                    override fun onPropertyUpdate(handler: ScreenHandler?, property: Int, value: Int) {
                        // pass
                    }

                })
            }
        }, TranslatedText("gunpowder_utilities.invsee.title", player.entityName).translateTextForPlayer(context.source.player)))

        return 1
    }
}
