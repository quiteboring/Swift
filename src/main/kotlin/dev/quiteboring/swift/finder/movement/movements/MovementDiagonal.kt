package dev.quiteboring.swift.finder.movement.movements

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult

object MovementDiagonal {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isSafe(ctx, destX, y, destZ)) return

    if (MovementHelper.isSolid(ctx, x, y, destZ) || MovementHelper.isSolid(ctx, destX, y, z)) return
    if (MovementHelper.isSolid(ctx, x, y + 1, destZ) || MovementHelper.isSolid(ctx, destX, y + 1, z)) return

    res.set(destX, y, destZ)
    res.cost = ctx.cost.SPRINT_DIAGONAL_TIME + ctx.wdc.getPathPenalty(destX, y, destZ)
  }
}
