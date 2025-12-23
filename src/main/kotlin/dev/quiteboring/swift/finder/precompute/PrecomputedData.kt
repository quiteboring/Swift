package dev.quiteboring.swift.finder.precompute

import dev.quiteboring.swift.finder.helper.BlockStateAccessor
import dev.quiteboring.swift.finder.movement.MovementHelper
import net.minecraft.block.Block
import net.minecraft.block.BlockState

class PrecomputedData(private val bsa: BlockStateAccessor) {

  private val states = IntArray(Block.STATE_IDS.size())

  companion object {
    private const val COMPLETED_MASK = 1 shl 0
    private const val SOLID_MASK = 1 shl 1
    private const val PASSABLE_MASK = 1 shl 2
  }

  private fun fillData(id: Int, state: BlockState, x: Int, y: Int, z: Int): Int {
    var data = 0

    if (MovementHelper.isSolidState(bsa, x, y, z, state)) {
      data = data or SOLID_MASK
    }

    if (MovementHelper.isPassableState(bsa, x, y, z, state)) {
      data = data or PASSABLE_MASK
    }

    data = data or COMPLETED_MASK
    states[id] = data
    return data
  }

  fun isSolid(x: Int, y: Int, z: Int, state: BlockState = bsa.get(x, y, z)): Boolean {
    val id = Block.STATE_IDS.getRawId(state)
    var data = states[id]

    if ((data and COMPLETED_MASK) == 0) {
      data = fillData(id, state, x, y, z)
    }

    return (data and SOLID_MASK) != 0
  }

  fun isPassable(x: Int, y: Int, z: Int, state: BlockState = bsa.get(x, y, z)): Boolean {
    val id = Block.STATE_IDS.getRawId(state)
    var data = states[id]

    if ((data and COMPLETED_MASK) == 0) {
      data = fillData(id, state, x, y, z)
    }

    return (data and PASSABLE_MASK) != 0
  }

}
