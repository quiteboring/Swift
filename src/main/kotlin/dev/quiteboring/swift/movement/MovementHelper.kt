package dev.quiteboring.swift.movement

import net.minecraft.util.math.BlockPos

object MovementHelper {

  fun isSolid(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z) ?: return false
    return !state.getCollisionShape(ctx.world, BlockPos(x, y, z)).isEmpty
  }

  fun isPassable(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z) ?: return false
    return state.getCollisionShape(ctx.world, BlockPos(x, y, z)).isEmpty
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
