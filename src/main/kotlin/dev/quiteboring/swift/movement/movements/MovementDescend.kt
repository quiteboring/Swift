package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.Movement
import dev.quiteboring.swift.movement.MovementHelper
import dev.quiteboring.swift.movement.MovementResult
import net.minecraft.util.math.BlockPos

class MovementDescend(val from: BlockPos, to: BlockPos) : Movement(from, to) {
  override fun calculateCost(ctx: CalculationContext, res: MovementResult) {
    calculateCost(ctx, source.x, source.y, source.z, target.x, target.z, res)
    costs = res.cost
  }

  companion object {
    fun calculateCost(
      ctx: CalculationContext,
      x: Int, y: Int, z: Int,
      destX: Int, destZ: Int,
      res: MovementResult
    ) {
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 1, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ)) return

      for (fallDist in 1..ctx.maxFallHeight) {
        val destY = y - fallDist

        if (!MovementHelper.canWalkThrough(ctx.bsa, destX, destY + 2, destZ)) return
        if (!MovementHelper.canWalkThrough(ctx.bsa, destX, destY + 1, destZ)) return

        if (MovementHelper.canWalkOn(ctx.bsa, destX, destY, destZ)) {
          res.set(destX, destY, destZ)

          var cost = ctx.cost.WALK_OFF_EDGE_TIME + ctx.cost.getFallTime(fallDist)

          if (fallDist > 3) {
            cost += (fallDist - 3) * (fallDist - 3) * 2.0
          }

          cost += ctx.wallDistance.getPathPenalty(destX, destY, destZ)

          res.cost = cost
          return
        }
      }
    }
  }
}
