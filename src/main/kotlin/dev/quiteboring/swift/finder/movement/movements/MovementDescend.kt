package dev.quiteboring.swift.finder.movement.movements

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult

object MovementDescend {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isPassable(ctx, destX, y, destZ)) return
    if (!MovementHelper.isPassable(ctx, destX, y + 1, destZ)) return

    val maxFall = ctx.maxFallHeight
    val cost = ctx.cost

    for (fallDist in 1..maxFall) {
      val destY = y - fallDist

      if (!MovementHelper.isPassable(ctx, destX, destY + 1, destZ)) return
      if (!MovementHelper.isPassable(ctx, destX, destY, destZ)) return

      if (MovementHelper.isSolid(ctx, destX, destY - 1, destZ)) {
        res.x = destX
        res.y = destY
        res.z = destZ

        var totalCost = cost.WALK_OFF_EDGE_TIME + cost.getFallTime(fallDist)

        if (fallDist > 3) {
          val excess = fallDist - 3
          totalCost += excess * excess * 2.0
        }

        totalCost += ctx.wdc.getPathPenalty(destX, destY, destZ)
        res.cost = totalCost
        return
      }
    }
  }

}
