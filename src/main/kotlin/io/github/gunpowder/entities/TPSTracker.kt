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

package io.github.gunpowder.entities

import com.google.common.math.Stats
import com.google.common.math.StatsAccumulator
import java.util.concurrent.TimeUnit
import kotlin.math.min

class TPSTracker {
    private val tracker = mutableListOf<Double>()
    private var tickStart = System.nanoTime()

    fun startTick() {
        tickStart = System.nanoTime()
    }

    fun endTick() {
        val tickEnd = System.nanoTime()
        val timeSpent = (tickEnd - tickStart)
        val ms = TimeUnit.NANOSECONDS.toMillis(timeSpent)

        tracker.add(ms.toDouble())
        while (tracker.count() > 200) {
            // More than 10 seconds
            tracker.removeAt(0)
        }
    }

    private fun getStats(): Stats {
        val acc = StatsAccumulator()
        acc.addAll(tracker.toList())
        return acc.snapshot()
    }

    fun getTps(): Double = min(20.0, 1000.0 / getMspt())
    fun getMspt(): Double = getStats().mean()
}
