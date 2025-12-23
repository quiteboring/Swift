package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.util.BlockStateAccessor
import net.minecraft.block.BlockState
import net.minecraft.block.CarpetBlock

object MovementHelper {

  fun isSafe(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState? = bsa.get(x, y, z)): Boolean {
    if (!isSolid(bsa, x, y - 1, z)) return false
    if (!isPassable(bsa, x, y, z)) return false
    if (!isPassable(bsa, x, y + 1, z)) return false
    return true
  }

  fun isSolid(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState? = bsa.get(x, y, z)): Boolean {
    if (state == null) return false
    if (state.isAir) return false
    if (state.block is CarpetBlock) return false

    return !state.getCollisionShape(null, null).isEmpty
  }

  fun isPassable(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState? = bsa.get(x, y, z)): Boolean {
    return !isSolid(bsa, x, y, z, state)
  }

}
