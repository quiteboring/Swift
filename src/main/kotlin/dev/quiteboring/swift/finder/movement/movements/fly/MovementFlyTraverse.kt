package dev.quiteboring.swift.finder.movement.movements.fly

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult

object MovementFlyTraverse {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destY: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isPassable(ctx, x, y, z)) return
    if (!MovementHelper.isPassable(ctx, x, y + 1, z)) return

    val neighbors = arrayOf(
      // Right
      intArrayOf(1, 0),
      // Left
      intArrayOf(-1, 0),
      // Forward
      intArrayOf(0, 1),

      // right 2
      intArrayOf(2, 0),
      // left 2
      intArrayOf(-2, 0),
    )
    var solidClose = 0

    for (n in neighbors) {
      if (
        MovementHelper.isSolid(ctx, destX + n[0], destY, destZ + n[1]) ||
        MovementHelper.isSolid(ctx, destX + n[0], destY + 1, destZ + n[1])
      ) solidClose++
    }

    val tightPenalty = 1.4 * solidClose.coerceAtMost(8)

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
