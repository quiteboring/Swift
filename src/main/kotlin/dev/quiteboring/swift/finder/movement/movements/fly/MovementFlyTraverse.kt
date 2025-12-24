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

    res.set(destX, destY, destZ)
    res.cost = 1.0
  }

}
