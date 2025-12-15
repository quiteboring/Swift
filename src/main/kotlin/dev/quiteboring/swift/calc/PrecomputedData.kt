package dev.quiteboring.swift.calc

import net.minecraft.block.*
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EmptyBlockView

class PrecomputedData {
  private val cache = IntArray(Block.STATE_IDS.size() + 1024)

  companion object {
    const val MASK_COMPUTED = 1
    const val MASK_WALKABLE = 2
    const val MASK_SOLID = 4
    const val MASK_LIQUID = 8
    const val MASK_AVOID = 16
    const val MASK_CLIMB = 32
  }

  fun get(state: BlockState): Int {
    val id = Block.STATE_IDS.getRawId(state)
    if ((cache[id] and MASK_COMPUTED) == 0) {
      cache[id] = compute(state)
    }
    return cache[id]
  }

  private fun compute(state: BlockState): Int {
    var flags = MASK_COMPUTED

    if (state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN).isEmpty) {
      flags = flags or MASK_WALKABLE
    }

    if ((flags and MASK_WALKABLE) == 0) {
      flags = flags or MASK_SOLID
    }

    if (!state.fluidState.isEmpty) {
      flags = flags or MASK_LIQUID
      flags = flags and MASK_SOLID.inv()
      flags = flags and MASK_WALKABLE.inv()
    }

    if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE)
      || state.isOf(Blocks.MAGMA_BLOCK) || state.isOf(Blocks.CACTUS)
      || state.isOf(Blocks.SWEET_BERRY_BUSH)) {
      flags = flags or MASK_AVOID
    }

    if (state.isIn(BlockTags.CLIMBABLE)) {
      flags = flags or MASK_CLIMB
      flags = flags or MASK_WALKABLE
    }

    return flags
  }
}
