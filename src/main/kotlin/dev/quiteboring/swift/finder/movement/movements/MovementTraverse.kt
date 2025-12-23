package dev.quiteboring.swift.finder.movement.movements

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult

object MovementTraverse {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isSafe(ctx.bsa, destX, y, destZ)) return

    res.x = destX
    res.y = y
    res.z = destZ
    res.cost = ctx.cost.SPRINT_ONE_BLOCK_TIME + ctx.wdc.getPathPenalty(destX, y, destZ)
  }

}
