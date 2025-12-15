package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.Movement
import dev.quiteboring.swift.movement.MovementResult
import dev.quiteboring.swift.movement.MovementHelper
import net.minecraft.util.math.BlockPos

class MovementDescend(val from: BlockPos, to: BlockPos) : Movement(from, to) {

  override fun calculateCost(
    ctx: CalculationContext,
    res: MovementResult,
  ) {
    calculateCost(ctx, source.x, source.y, source.z, target.x, target.z, res)
    costs = res.cost
  }

  companion object {
    fun calculateCost(
      ctx: CalculationContext,
      x: Int,
      y: Int,
      z: Int,
      destX: Int,
      destZ: Int,
      res: MovementResult
    ) {
      for (i in 1..ctx.maxFallHeight) {
          val dy = y - i
          if (!MovementHelper.isPassable(ctx, destX, dy, destZ)) {
              return
          }
           
          if (MovementHelper.isSafe(ctx, destX, dy, destZ)) {
              res.set(destX, dy, destZ)
              res.cost = ctx.cost.WALK_OFF_ONE_BLOCK_COST + ctx.cost.N_BLOCK_FALL_COST[i]
              return
          }
      }
    }
  }

}
