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
      x: Int, y: Int, z: Int,
      destX: Int, destZ: Int,
      res: MovementResult
    ) {
      if (!MovementHelper.canWalkOn(ctx.bsa, destX, y, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 1, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ)) return

      if (!MovementHelper.canWalkThrough(ctx.bsa, x, y + 1, destZ) || !MovementHelper.canWalkThrough(ctx.bsa, destX, y + 1, z)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, x, y + 2, destZ) || !MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, z)) return

      res.set(destX, y, destZ)
      res.cost = ctx.cost.SPRINT_DIAGONAL_TIME + ctx.wallDistance.getPathPenalty(destX, y + 1, destZ)
    }
  }
}
