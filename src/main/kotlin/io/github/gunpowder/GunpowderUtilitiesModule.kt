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

package io.github.gunpowder

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.commands.*
import io.github.gunpowder.entities.TPSTracker
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

class GunpowderUtilitiesModule : GunpowderModule {
    override val name = "utilities"
    override val toggleable = true
    val gunpowder = GunpowderMod.instance

    override fun registerCommands() {
        gunpowder.registry.registerCommand(EnderchestCommand::register)
        gunpowder.registry.registerCommand(FlightCommand::register)
        gunpowder.registry.registerCommand(GodCommand::register)
        gunpowder.registry.registerCommand(HatCommand::register)
        gunpowder.registry.registerCommand(HeadCommand::register)
        gunpowder.registry.registerCommand(HealCommand::register)
        gunpowder.registry.registerCommand(InvseeCommand::register)
        // essentials.registry.registerCommand(SpeedCommand::register)  // Not yet functional
        gunpowder.registry.registerCommand(TPSCommand::register)
        gunpowder.registry.registerCommand(TrashCommand::register)
        gunpowder.registry.registerCommand(VanishCommand::register)
        gunpowder.registry.registerCommand(WorkbenchCommand::register)
    }

    override fun registerEvents() {
        ServerTickEvents.START_WORLD_TICK.register(ServerTickEvents.StartWorldTick { world ->
            getTracker(world.registryKey.value.path).startTick()
        })
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEvents.EndWorldTick { world ->
            getTracker(world.registryKey.value.path).endTick()
        })
    }

    companion object {
        val tpsTrackers = mutableMapOf<String, TPSTracker>()

        @JvmStatic
        fun getTracker(name: String): TPSTracker {
            var c = tpsTrackers[name]
            if (c == null) {
                c = TPSTracker()
                tpsTrackers[name] = c
            }
            return c
        }
    }
}