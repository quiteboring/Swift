package dev.quiteboring.swift.finder.movement.movements

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult
import net.minecraft.block.*

object MovementAscend {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destZ: Int,
    res: MovementResult
  ) {
    val bsa = ctx.bsa

    if (!MovementHelper.isSafe(bsa, destX, y + 1, destZ)) return
    if (!MovementHelper.isPassable(bsa, x, y + 2, z)) return

    val groundState = ctx.get(destX, y, destZ)
    val block = groundState.block

    if (block is FenceBlock || block is FenceGateBlock || block is WallBlock) return

    res.x = destX
    res.y = y + 1
    res.z = destZ

    res.cost = if (block is SlabBlock || block is StairsBlock) {
      ctx.cost.SLAB_ASCENT_TIME
    } else {
      ctx.cost.JUMP_UP_ONE_BLOCK_TIME
    } + ctx.wdc.getPathPenalty(destX, y + 1, destZ)
  }
}
