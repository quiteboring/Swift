package dev.quiteboring.swift.movement

import dev.quiteboring.swift.calc.PathNode
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import java.lang.Double.isNaN
import kotlin.math.min
import net.minecraft.block.*

/**
 * Thank you EpsilonPhoenix for this superb class!
 */
class WallDistanceCalculator(private val ctx: CalculationContext) {

  companion object {
    const val MAX_DIST = 6

    private val EDGE_PENALTIES = doubleArrayOf(
      50.0,  // edge
      18.0,  // 1 away
      7.0,   // 2 away
      2.5,   // 3 away
      0.5,   // 4 away
      0.0,   // 5+ away
      0.0
    )

    private val WALL_PENALTIES = doubleArrayOf(
      15.0,  // touching
      8.0,   // 1 away
      3.0,   // 2 away
      1.0,   // 3 away
      0.0,   // 4+ away
      0.0,
      0.0
    )
  }

  private val penaltyCache = Long2DoubleOpenHashMap().apply {
    defaultReturnValue(Double.NaN)
  }

  private val edgeCache = Long2IntOpenHashMap().apply { defaultReturnValue(-1) }
  private val wallCache = Long2IntOpenHashMap().apply { defaultReturnValue(-1) }

  fun getPathPenalty(x: Int, y: Int, z: Int): Double {
    val key = PathNode.coordKey(x, y, z)
    var penalty = penaltyCache.get(key)

    if (isNaN(penalty)) {
      penalty = computePenalty(x, y, z)
      penaltyCache.put(key, penalty)
    }

    return penalty
  }

  private fun computePenalty(x: Int, y: Int, z: Int): Double {
    val minEdge = min(
      min(scanForEdge(x, y, z, 0, -1), scanForEdge(x, y, z, 0, 1)),
      min(scanForEdge(x, y, z, 1, 0), scanForEdge(x, y, z, -1, 0))
    )

    val minWall = min(
      min(scanForWall(x, y, z, 0, -1), scanForWall(x, y, z, 0, 1)),
      min(scanForWall(x, y, z, 1, 0), scanForWall(x, y, z, -1, 0))
    )

    return EDGE_PENALTIES[minEdge.coerceIn(0, 6)] + WALL_PENALTIES[minWall.coerceIn(0, 6)]
  }

  private fun scanForEdge(x: Int, y: Int, z: Int, dx: Int, dz: Int): Int {
    for (d in 1..MAX_DIST) {
      if (isEdge(x + dx * d, y, z + dz * d)) {
        return d - 1
      }
    }

    return MAX_DIST
  }

  private fun scanForWall(x: Int, y: Int, z: Int, dx: Int, dz: Int): Int {
    for (d in 1..MAX_DIST) {
      if (isWall(x + dx * d, y, z + dz * d)) {
        return d - 1
      }
    }

    return MAX_DIST
  }

  private fun isEdge(x: Int, y: Int, z: Int): Boolean {
    val below = ctx.get(x, y - 1, z) ?: return true

    if (below.isAir) {
      for (depth in 2..4) {
        val state = ctx.get(x, y - depth, z) ?: continue
        if (state.isAir || state.block is CarpetBlock) continue

        if (MovementHelper.canWalkOn(ctx.bsa, x, y - depth, z, state)) {
          return depth >= 3
        }
      }

      return true
    }

    if (below.block is CarpetBlock) {
      return !MovementHelper.canWalkOn(ctx.bsa, x, y - 2, z)
    }

    return !MovementHelper.canWalkOn(ctx.bsa, x, y - 1, z, below)
  }

  private fun isWall(x: Int, y: Int, z: Int): Boolean {
    return isBlockingWall(x, y, z) || isBlockingWall(x, y + 1, z)
  }

  private fun isBlockingWall(x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z) ?: return false
    val block = state.block

    if (state.isAir) {
      return false
    }

    if (
      block is CarpetBlock || block is SlabBlock ||
      block is StairsBlock || block is DoorBlock ||
      block is TrapdoorBlock || block is TorchBlock ||
      block is SignBlock || block is WallSignBlock ||
      block is PlantBlock || block is AbstractRailBlock ||
      block is VineBlock || block is LadderBlock ||
      block is SnowBlock || block is PressurePlateBlock ||
      block is ButtonBlock || block is RedstoneWireBlock ||
      block is LeverBlock || block is BannerBlock ||
      block is WallBannerBlock || block is TripwireBlock ||
      block is TripwireHookBlock
    ) {
      return false
    }

    if (block is FenceBlock || block is FenceGateBlock || block is WallBlock) {
      return true
    }

    if (MovementHelper.canWalkThrough(ctx.bsa, x, y, z, state)) {
      return false
    }

    val shape = state.getCollisionShape(null, null)
    if (shape.isEmpty) return false

    return shape.boundingBox.let { it.maxY - it.minY >= 0.5 }
  }

  fun getEdgeDistance(x: Int, y: Int, z: Int): Int {
    val key = PathNode.coordKey(x, y, z)
    var dist = edgeCache.get(key)

    if (dist == -1) {
      dist = min(
        min(scanForEdge(x, y, z, 0, -1), scanForEdge(x, y, z, 0, 1)),
        min(scanForEdge(x, y, z, 1, 0), scanForEdge(x, y, z, -1, 0))
      )

      edgeCache.put(key, dist)
    }

    return dist
  }

  fun getWallDistance(x: Int, y: Int, z: Int): Int {
    val key = PathNode.coordKey(x, y, z)
    var dist = wallCache.get(key)

    if (dist == -1) {
      dist = min(
        min(scanForWall(x, y, z, 0, -1), scanForWall(x, y, z, 0, 1)),
        min(scanForWall(x, y, z, 1, 0), scanForWall(x, y, z, -1, 0))
      )

      wallCache.put(key, dist)
    }

    return dist
  }

}
