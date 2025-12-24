package dev.quiteboring.swift.finder.movement.movements.fly

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult

object MovementFlyAscend {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destY: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isPassable(ctx, x, destY, z)) return
    if (!MovementHelper.isPassable(ctx, x, destY + 1, z)) return

    val neighbors = arrayOf(intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1))
    var solidClose = 0

    for (n in neighbors) {
      if (
        MovementHelper.isSolid(ctx, destX + n[0], destY, destZ + n[1]) ||
        MovementHelper.isSolid(ctx, destX + n[0], destY + 1, destZ + n[1])
      ) solidClose++
    }

    val tightPenalty = 0.05 * solidClose.coerceAtMost(4)
    val lowCeiling = MovementHelper.isSolid(ctx, destX, destY + 2, destZ)

    var groundPenalty =
      when {
        MovementHelper.isSolid(ctx, destX, destY - 1, destZ) -> 0.5
        MovementHelper.isSolid(ctx, destX, destY - 2, destZ) -> 0.3
        MovementHelper.isSolid(ctx, destX, destY - 3, destZ) -> 0.1
        else -> 0.0
      }

    if (lowCeiling || solidClose > 0) groundPenalty *= 0.3

    res.set(destX, destY, destZ)
    res.cost = 1.0 + tightPenalty + groundPenalty
  }

}
