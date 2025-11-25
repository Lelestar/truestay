package ca.uqac.inf865.truestay.presentation.common.utils

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max

/**
 * Utilities to compute dynamic slider bounds and step count
 * from a set of integer values (e.g., price or surface).
 * Produces rounded, human-friendly intervals using 1/2/5×10^k steps.
 */

/**
 * Slider configuration for Compose RangeSlider.
 * - `min`/`max`: inclusive float bounds.
 * - `steps`: number of discrete steps between min and max (Compose expects intervals - 1).
 */
data class SliderConfig(val min: Float, val max: Float, val steps: Int)

/**
 * Compute a nice slider configuration from raw values.
 * Falls back to provided min/max when values are empty.
 *
 * @param values Source integer values (e.g., prices, surfaces).
 * @param fallbackMin Fallback minimum when list is empty.
 * @param fallbackMax Fallback maximum when list is empty.
 * @param targetIntervals Desired number of intervals (actual may differ after rounding).
 */
fun computeSliderConfig(
    values: List<Int>,
    fallbackMin: Int,
    fallbackMax: Int,
    targetIntervals: Int = 10
): SliderConfig {
    if (values.isEmpty()) {
        val min = fallbackMin.toFloat()
        val max = fallbackMax.toFloat()
        val intervals = (targetIntervals).coerceAtLeast(1)
        return SliderConfig(min = min, max = max, steps = (intervals - 1))
    }

    val rawMin = values.minOrNull() ?: fallbackMin
    val rawMax = values.maxOrNull() ?: fallbackMax
    val range = rawMax - rawMin
    if (range <= 0) {
        // Avoid zero-range sliders
        return SliderConfig(min = rawMin.toFloat(), max = (rawMin + 1).toFloat(), steps = 0)
    }

    val roughStep = max(1, range / targetIntervals)
    val stepSize = niceStep(roughStep)
    val minBound = floor(rawMin / stepSize.toDouble()).toInt() * stepSize
    val maxBound = ceil(rawMax / stepSize.toDouble()).toInt() * stepSize
    val intervals = max(1, (maxBound - minBound) / stepSize)
    val steps = (intervals - 1).coerceAtLeast(0)

    return SliderConfig(min = minBound.toFloat(), max = maxBound.toFloat(), steps = steps)
}

/**
 * Find a "nice" step size close to n using the sequence 1/2/5 × 10^k.
 */
private fun niceStep(n: Int): Int {
    if (n <= 0) return 1
    val exp = floor(log10(n.toDouble())).toInt()
    var base = 1
    repeat(exp) { base *= 10 }
    val candidates = intArrayOf(1, 2, 5).map { it * base }
    for (c in candidates) {
        if (n <= c) return c
    }
    return 10 * base
}
