package dev.quiteboring.swift.util

import dev.quiteboring.swift.finder.movement.CalculationContext
import net.minecraft.block.BlockState
import net.minecraft.block.CarpetBlock

object BlockUtils {

  fun isSolid(ctx: CalculationContext, x: Int, y: Int, z: Int, state: BlockState? = ctx.bsa.get(x, y, z)): Boolean {
    if (state == null) return false
    if (state.isAir) return false
    if (state.block is CarpetBlock) return false

    return !state.getCollisionShape(null, null).isEmpty
  }

  fun isPassable(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z) ?: return false
    return !isSolid(ctx, x, y, z, state)
  }

}
