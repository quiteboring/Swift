package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.*
import net.minecraft.util.math.BlockPos

class MovementDiagonal(val from: BlockPos, to: BlockPos) : Movement(from, to) {

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
      if(!MovementHelper.isSafe(ctx, destX, y, destZ)) return

      if (!MovementHelper.isPassable(ctx, destX, y, z) || !MovementHelper.isPassable(ctx, destX, y + 1, z)) return
      if (!MovementHelper.isPassable(ctx, x, y, destZ) || !MovementHelper.isPassable(ctx, x, y + 1, destZ)) return

      res.set(destX, y, destZ)
      res.cost = ctx.cost.DIAGONAL_SPRINT_COST
    }
  }
}
