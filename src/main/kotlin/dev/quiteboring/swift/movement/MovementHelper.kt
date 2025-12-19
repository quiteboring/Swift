/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.quiteboring.swift.movement

import dev.quiteboring.swift.util.BlockStateAccessor
import dev.quiteboring.swift.util.Ternary
import net.minecraft.block.*
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.fluid.Fluids
import net.minecraft.fluid.WaterFluid

/*
 * Source code copied from the Baritone project.
 * https://github.com/cabaletta/baritone
 */
object MovementHelper {

  fun canWalkOn(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState? = bsa.get(x, y, z)): Boolean {
    val canWalkOn = canWalkOnBlockState(state)

    if (canWalkOn == Ternary.YES) {
      return true
    }

    if (canWalkOn == Ternary.NO) {
      return false
    }

    return canWalkOnPosition(bsa, x, y, z, state)
  }

  fun canWalkThrough(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState? = bsa.get(x, y, z)): Boolean {
    val canWalkThrough = canWalkThroughBlockState(state)

    if (canWalkThrough == Ternary.YES) {
      return true
    }

    if (canWalkThrough == Ternary.NO) {
      return false
    }

    return canWalkThroughPosition(bsa, x, y, z, state)
  }

  fun canWalkThroughPosition(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState?): Boolean {
    val block = state?.block ?: return false

    return when (block) {
      is CarpetBlock ->
        canWalkOn(bsa, x, y - 1, z)

      is SnowBlock -> {
        if (!bsa.isChunkLoaded(x, z)) return true
        if (state.get(SnowBlock.LAYERS) >= 3) return false
        canWalkOn(bsa, x, y - 1, z)
      }

      else -> {
        val fluidState = state.fluidState
        if (!fluidState.isEmpty) {
          if (isFlowing(bsa, x, y, z, state)) return false

          val up = bsa.get(x, y + 1, z) ?: return false
          if (!up.fluidState.isEmpty || up.block is LilyPadBlock) return false

          return fluidState.fluid is WaterFluid
        }

        state.canPathfindThrough(NavigationType.LAND)
      }
    }
  }

  fun canWalkThroughBlockState(state: BlockState?): Ternary {
    val block = state?.block ?: return Ternary.NO

    if (block is AirBlock) {
      return Ternary.YES
    }

    val fluidState = state.fluidState

    if (!fluidState.isEmpty) {
      return if (fluidState.level != 8) Ternary.NO else Ternary.MAYBE
    }

    return when (block) {
      in setOf(
        Blocks.COBWEB,
        Blocks.END_PORTAL,
        Blocks.COCOA,
        Blocks.BUBBLE_COLUMN,
        Blocks.HONEY_BLOCK,
        Blocks.END_ROD,
        Blocks.SWEET_BERRY_BUSH,
        Blocks.POINTED_DRIPSTONE,
        Blocks.BIG_DRIPLEAF,
        Blocks.POWDER_SNOW
      ),
        -> Ternary.NO

      is FireBlock -> Ternary.NO
      is AbstractSkullBlock -> Ternary.NO
      is ShulkerBoxBlock -> Ternary.NO
      is SlabBlock -> Ternary.NO
      is TrapdoorBlock -> Ternary.NO
      is AmethystClusterBlock -> Ternary.NO
      is AzaleaBlock -> Ternary.NO
      is CauldronBlock -> Ternary.NO
      is DoorBlock, is FenceGateBlock -> {
        // TODO this assumes that all doors in all mods are openable
        if (block === Blocks.IRON_DOOR) Ternary.NO else Ternary.YES
      }

      is CarpetBlock -> Ternary.MAYBE
      is SnowBlock -> Ternary.MAYBE
      else -> try {
        if (state.canPathfindThrough(NavigationType.LAND)) {
          Ternary.YES
        } else {
          Ternary.NO
        }
      } catch (_: Throwable) {
        Ternary.MAYBE
      }
    }
  }

  fun canWalkOnBlockState(state: BlockState?): Ternary {
    val block = state?.block ?: return Ternary.NO

    if (isWater(state) || isLava(state)) {
      return Ternary.MAYBE
    }

    return when {
      isBlockNormalCube(state) && block !in setOf(
        Blocks.MAGMA_BLOCK,
        Blocks.BUBBLE_COLUMN,
        Blocks.HONEY_BLOCK
      ) -> Ternary.YES

      block is AzaleaBlock -> Ternary.YES

      block in setOf(
        Blocks.LADDER,
        Blocks.VINE,
        Blocks.FARMLAND,
        Blocks.DIRT_PATH,
        Blocks.SOUL_SAND,
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.GLASS
      ) -> Ternary.YES

      block is StainedGlassBlock -> Ternary.YES
      block is StairsBlock -> Ternary.YES
      block is SlabBlock -> Ternary.YES

      else -> Ternary.NO
    }
  }

  fun isBlockNormalCube(state: BlockState): Boolean {
    val block = state.block

    if (
      block is BambooShootBlock
      || block is PistonExtensionBlock
      || block is ScaffoldingBlock
      || block is ShulkerBoxBlock
      || block is PointedDripstoneBlock
      || block is AmethystClusterBlock
    ) {
      return false
    }

    try {
      return Block.isShapeFullCube(state.getCollisionShape(null, null))
    } catch (_: Exception) {
    }

    return false
  }

  fun canWalkOnPosition(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState?): Boolean {
    if (isWater(state)) {
      val upState = bsa.get(x, y + 1, z) ?: return false
      val up = upState.block

      if (up === Blocks.LILY_PAD || up is CarpetBlock) {
        return true
      }

      if (isFlowing(bsa, x, y, z, state) || upState.fluidState.fluid === Fluids.FLOWING_WATER) {
        return isWater(upState)
      }

      return isWater(upState)
    }

    return false
  }

  fun isWater(state: BlockState?): Boolean {
    val fluid = state?.fluidState?.fluid ?: return false
    return fluid === Fluids.WATER || fluid === Fluids.FLOWING_WATER
  }

  fun isLava(state: BlockState?): Boolean {
    val fluid = state?.fluidState?.fluid ?: return false
    return fluid === Fluids.LAVA || fluid === Fluids.FLOWING_LAVA
  }

  fun possiblyFlowing(state: BlockState?): Boolean {
    val fluidState = state?.fluidState ?: return false

    return !fluidState.isStill
      && fluidState.fluid.getLevel(fluidState) != 8
  }

  fun isFlowing(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: BlockState? = bsa.get(x, y, z)): Boolean {
    val fluidState = state?.fluidState ?: return false

    if (!fluidState.isStill) return true
    if (fluidState.fluid.getLevel(fluidState) != 8) return true

    return possiblyFlowing(bsa.get(x + 1, y, z)) ||
      possiblyFlowing(bsa.get(x - 1, y, z)) ||
      possiblyFlowing(bsa.get(x, y, z + 1)) ||
      possiblyFlowing(bsa.get(x, y, z - 1))
  }

}
