package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.*
import net.minecraft.util.math.BlockPos

class MovementTraverse(val from: BlockPos, to: BlockPos) : Movement(from, to) {
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
      if (!MovementHelper.isSolid(ctx, destX, y - 1, destZ)) return
      if (!MovementHelper.isPassable(ctx, destX, y, destZ)) return
      if (!MovementHelper.isPassable(ctx, destX, y + 1, destZ)) return

      res.set(destX, y, destZ)
      res.cost = ctx.cost.SPRINT_ONE_BLOCK_TIME
    }
  }
}
