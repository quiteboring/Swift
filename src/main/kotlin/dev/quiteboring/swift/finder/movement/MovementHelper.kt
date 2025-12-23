package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.util.BlockStateAccessor
import net.minecraft.block.BlockState
import net.minecraft.block.CarpetBlock
import net.minecraft.block.ShapeContext

object MovementHelper {

  @JvmField
  val SHAPE_CONTEXT: ShapeContext = ShapeContext.absent()

  @JvmStatic
  fun isSafe(bsa: BlockStateAccessor, x: Int, y: Int, z: Int): Boolean {
    if (!isSolid(bsa, x, y - 1, z)) return false
    if (!isPassable(bsa, x, y, z)) return false
    if (!isPassable(bsa, x, y + 1, z)) return false
    return true
  }

  @JvmStatic
  fun isSolid(bsa: BlockStateAccessor, x: Int, y: Int, z: Int): Boolean {
    val state = bsa.get(x, y, z)
    return isSolidState(bsa, x, y, z, state)
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
  fun isPassable(bsa: BlockStateAccessor, x: Int, y: Int, z: Int): Boolean {
    val state = bsa.get(x, y, z)
    return isPassableState(bsa, x, y, z, state)
  }

  @JvmStatic
  fun isPassableState(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState): Boolean {
    if (state.isAir) return true
    if (state.block is CarpetBlock) return true

    bsa.mutablePos.set(x, y, z)
    return state.getCollisionShape(bsa.access, bsa.mutablePos, SHAPE_CONTEXT).isEmpty
  }
}
