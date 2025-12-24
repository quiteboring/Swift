package dev.quiteboring.swift.finder.movement.movements

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult
import kotlin.math.abs

object MovementJumpGap {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isSafe(ctx, destX, y, destZ)) return
    if (!MovementHelper.isPassable(ctx, x, y + 2, z)) return

    val dx = destX - x
    val dz = destZ - z
    val dist = if (dx != 0) abs(dx) else abs(dz)

    if (dx != 0 && dz != 0) return

    val dirX = if (dx != 0) dx / dist else 0
    val dirZ = if (dz != 0) dz / dist else 0

    for (i in 1 until dist) {
      val checkX = x + (dirX * i)
      val checkZ = z + (dirZ * i)

      if (!MovementHelper.isPassable(ctx, checkX, y, checkZ)) return
      if (!MovementHelper.isPassable(ctx, checkX, y + 1, checkZ)) return
      if (!MovementHelper.isPassable(ctx, checkX, y + 2, checkZ)) return
    }

    res.set(destX, y, destZ)

    val cost = ctx.cost
    res.cost = cost.JUMP_PENALTY +
      (cost.SPRINT_ONE_BLOCK_TIME * dist) +
      cost.GAP_JUMP_REWARD_OFFSET +
      ctx.wdc.getPathPenalty(destX, y, destZ)
  }
}
