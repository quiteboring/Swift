package dev.quiteboring.swift.movement.movements

import dev.quiteboring.swift.movement.*
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.util.math.BlockPos

class MovementAscend(val from: BlockPos, to: BlockPos) : Movement(from, to) {

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
      if (!MovementHelper.canWalkOn(ctx.bsa, destX, y + 1, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 3, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, x, y + 3, z)) return

      res.set(destX, y + 1, destZ)

      val groundState = ctx.get(destX, y, destZ)
      val block = groundState?.block

      res.cost = if (block is SlabBlock || block is StairsBlock) {
        ctx.cost.SLAB_ASCENT_TIME
      } else {
        ctx.cost.JUMP_UP_ONE_BLOCK_TIME
      } + ctx.wallDistance.getPathPenalty(destX, y + 2, destZ)
    }
  }

}
