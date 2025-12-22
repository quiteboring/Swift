package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.util.BlockUtils.isPassable
import dev.quiteboring.swift.util.BlockUtils.isSolid
import net.minecraft.block.CarpetBlock

object MovementHelper {

  fun isSafe(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    if (!hasValidGround(ctx, x, y - 1, z)) return false
    if (!isPassable(ctx, x, y, z)) return false
    if (!isPassable(ctx, x, y + 1, z)) return false
    return true
  }

  private fun hasValidGround(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z) ?: return false
    return isSolid(ctx, x, y, z, state)
  }

}
