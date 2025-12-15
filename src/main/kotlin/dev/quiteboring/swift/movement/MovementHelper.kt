package dev.quiteboring.swift.movement

import dev.quiteboring.swift.calc.PrecomputedData

object MovementHelper {

  fun isSolid(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    val flags = ctx.getFlags(x, y, z)
    return (flags and PrecomputedData.MASK_SOLID) != 0 && (flags and PrecomputedData.MASK_AVOID) == 0
  }

  fun isPassable(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    val flags = ctx.getFlags(x, y, z)
    return ((flags and PrecomputedData.MASK_WALKABLE) != 0 || (flags and PrecomputedData.MASK_LIQUID) != 0)
      && (flags and PrecomputedData.MASK_AVOID) == 0
  }

  fun isLiquid(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    return (ctx.getFlags(x, y, z) and PrecomputedData.MASK_LIQUID) != 0
  }

  fun isSafe(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    if (!isSolid(ctx, x, y - 1, z)) {
        // println("isSafe check failed: No ground at $x, ${y-1}, $z")
        return false
    }
    if (!isPassable(ctx, x, y, z)) {
        // println("isSafe check failed: Feet obstructed at $x, $y, $z")
        return false
    }
    if (!isPassable(ctx, x, y + 1, z)) {
        // println("isSafe check failed: Head obstructed at $x, ${y+1}, $z")
        return false
    }

    return true
  }
}
