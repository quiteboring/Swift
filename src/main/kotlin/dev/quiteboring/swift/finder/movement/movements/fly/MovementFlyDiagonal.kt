package dev.quiteboring.swift.finder.movement.movements.fly

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import dev.quiteboring.swift.finder.movement.MovementResult

object MovementFlyDiagonal {

  @JvmStatic
  fun calculateCost(
    ctx: CalculationContext,
    x: Int, y: Int, z: Int,
    destX: Int, destY: Int, destZ: Int,
    res: MovementResult
  ) {
    if (!MovementHelper.isPassable(ctx, x, destY, z)) return
    if (!MovementHelper.isPassable(ctx, x, destY + 1, z)) return

    if (MovementHelper.isSolid(ctx, x, destY, destZ) || MovementHelper.isSolid(ctx, destX, destY, z)) return
    if (MovementHelper.isSolid(ctx, x, destY + 1, destZ) || MovementHelper.isSolid(ctx, destX, destY + 1, z)) return

    res.set(destX, destY, destZ)
    res.cost = 1.0
  }

}
