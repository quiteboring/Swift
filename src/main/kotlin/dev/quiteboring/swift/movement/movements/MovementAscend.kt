package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.Movement
import dev.quiteboring.swift.movement.MovementResult
import dev.quiteboring.swift.movement.MovementHelper
import net.minecraft.util.math.BlockPos

class MovementAscend(val from: BlockPos, to: BlockPos) : Movement(from, to) {

  override fun calculateCost(ctx: CalculationContext, res: MovementResult) {
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
      if(!MovementHelper.isSafe(ctx, destX, y + 1, destZ)) return

      if(!MovementHelper.isPassable(ctx, x, y + 2, z)) return

      res.set(destX, y + 1, destZ)

      res.cost = ctx.cost.JUMP_ONE_BLOCK_COST + ctx.cost.JUMP_PENALTY
    }
  }
}
