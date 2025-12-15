package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.Movement
import dev.quiteboring.swift.movement.MovementResult
import dev.quiteboring.swift.movement.MovementHelper
import net.minecraft.util.math.BlockPos

class MovementDiagonal(val from: BlockPos, to: BlockPos) : Movement(from, to) {

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
      if(!MovementHelper.isSafe(ctx, destX, y, destZ)) return

      if(MovementHelper.isSolid(ctx, x, y, destZ) || MovementHelper.isSolid(ctx, destX, y, z)) return
      if(MovementHelper.isSolid(ctx, x, y + 1, destZ) || MovementHelper.isSolid(ctx, destX, y + 1, z)) return

      res.set(destX, y, destZ)
      res.cost = ctx.cost.ONE_BLOCK_WALK_COST * 1.414
    }
  }

}
