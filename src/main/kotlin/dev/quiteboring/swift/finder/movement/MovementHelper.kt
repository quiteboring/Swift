package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.finder.helper.BlockStateAccessor
import net.minecraft.block.BlockState
import net.minecraft.block.CarpetBlock
import net.minecraft.block.ShapeContext

object MovementHelper {

  @JvmField
  val SHAPE_CONTEXT: ShapeContext = ShapeContext.absent()

  @JvmStatic
  fun isSafe(ctx: CalculationContext, x: Int, y: Int, z: Int): Boolean {
    if (!isSolid(ctx, x, y - 1, z)) return false
    if (!isPassable(ctx, x, y, z)) return false
    if (!isPassable(ctx, x, y + 1, z)) return false
    return true
  }

  @JvmStatic
  fun isSolid(ctx: CalculationContext, x: Int, y: Int, z: Int, state: BlockState = ctx.get(x, y, z)): Boolean {
    return ctx.precomputedData.isSolid(x, y, z, state)
  }

  @JvmStatic
  fun isPassable(ctx: CalculationContext, x: Int, y: Int, z: Int, state: BlockState = ctx.get(x, y, z)): Boolean {
    return ctx.precomputedData.isPassable(x, y, z, state)
  }

  @JvmStatic
  fun isSolidState(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState): Boolean {
    if (state.isAir) return false
    if (state.block is CarpetBlock) return false

    bsa.mutablePos.set(x, y, z)

    if (state.isFullCube(bsa.access, bsa.mutablePos)) return true

    return !state.getCollisionShape(bsa.access, bsa.mutablePos, SHAPE_CONTEXT).isEmpty
  }

  @JvmStatic
  fun isPassableState(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState): Boolean {
    if (state.isAir) return true
    if (state.block is CarpetBlock) return true

    bsa.mutablePos.set(x, y, z)
    return state.getCollisionShape(bsa.access, bsa.mutablePos, SHAPE_CONTEXT).isEmpty
  }

}
