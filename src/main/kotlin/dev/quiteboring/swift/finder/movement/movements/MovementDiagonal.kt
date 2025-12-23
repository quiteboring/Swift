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
    if (!MovementHelper.isSafe(ctx.bsa, destX, y, destZ)) return

    val bsa = ctx.bsa

    if (MovementHelper.isSolid(bsa, x, y, destZ) || MovementHelper.isSolid(bsa, destX, y, z)) return
    if (MovementHelper.isSolid(bsa, x, y + 1, destZ) || MovementHelper.isSolid(bsa, destX, y + 1, z)) return

    res.x = destX
    res.y = y
    res.z = destZ
    res.cost = ctx.cost.SPRINT_DIAGONAL_TIME + ctx.wdc.getPathPenalty(destX, y, destZ)
  }
}
