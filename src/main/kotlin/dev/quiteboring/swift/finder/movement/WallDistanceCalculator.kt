package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.finder.calculate.PathNode
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import net.minecraft.block.*
import kotlin.math.min

/**
 * Thank you EpsilonPhoenix for this superb class!
 */
class WallDistanceCalculator(private val ctx: CalculationContext) {

  companion object {
    const val MAX_DIST = 6

    @JvmField
    val EDGE_PENALTIES = doubleArrayOf(
      50.0,  // edge
      18.0,  // 1 away
      7.0,   // 2 away
      2.5,   // 3 away
      0.5,   // 4 away
      0.0,   // 5+ away
      0.0
    )

    @JvmField
    val WALL_PENALTIES = doubleArrayOf(
      15.0,  // touching
      8.0,   // 1 away
      3.0,   // 2 away
      1.0,   // 3 away
      0.0,   // 4+ away
      0.0,
      0.0
    )

    @JvmField
    val DIRECTIONS = arrayOf(
      intArrayOf(0, -1),
      intArrayOf(0, 1),
      intArrayOf(1, 0),
      intArrayOf(-1, 0)
    )
  }

  private val penaltyCache = Long2DoubleOpenHashMap(8192).apply {
    defaultReturnValue(Double.NaN)
  }

  private val edgeCache = Long2IntOpenHashMap(4096).apply {
    defaultReturnValue(-1)
  }

  private val wallCache = Long2IntOpenHashMap(4096).apply {
    defaultReturnValue(-1)
  }

  fun getPathPenalty(x: Int, y: Int, z: Int): Double {
    val key = PathNode.coordKey(x, y, z)
    var penalty = penaltyCache.get(key)

    if (penalty.isNaN()) {
      penalty = computePenalty(x, y, z)
      penaltyCache.put(key, penalty)
    }

    return penalty
  }

  private fun computePenalty(x: Int, y: Int, z: Int): Double {
    var minEdge = MAX_DIST
    var minWall = MAX_DIST

    for (dir in DIRECTIONS) {
      val dx = dir[0]
      val dz = dir[1]

      val edge = scanForEdge(x, y, z, dx, dz)
      if (edge < minEdge) minEdge = edge

      val wall = scanForWall(x, y, z, dx, dz)
      if (wall < minWall) minWall = wall

      if (minEdge == 0 && minWall == 0) break
    }

    return EDGE_PENALTIES[minEdge.coerceIn(0, 6)] + WALL_PENALTIES[minWall.coerceIn(0, 6)]
  }

  private fun scanForEdge(x: Int, y: Int, z: Int, dx: Int, dz: Int): Int {
    var cx = x + dx
    var cz = z + dz

    for (d in 1..MAX_DIST) {
      if (isEdge(cx, y, cz)) {
        return d - 1
      }
      cx += dx
      cz += dz
    }
    return MAX_DIST
  }

  private fun scanForWall(x: Int, y: Int, z: Int, dx: Int, dz: Int): Int {
    var cx = x + dx
    var cz = z + dz

    for (d in 1..MAX_DIST) {
      if (isWall(cx, y, cz)) {
        return d - 1
      }
      cx += dx
      cz += dz
    }
    return MAX_DIST
  }

  private fun isEdge(x: Int, y: Int, z: Int): Boolean {
    val below = ctx.get(x, y - 1, z)

    if (below.isAir) {
      for (depth in 2..4) {
        val state = ctx.get(x, y - depth, z)
        if (!state.isAir && state.block !is CarpetBlock) {
          if (MovementHelper.isSolidState(ctx.bsa, x, y - depth, z, state)) {
            return depth >= 3
          }
        }
      }
      return true
    }

    val block = below.block
    if (block is CarpetBlock) {
      return !MovementHelper.isSolid(ctx.bsa, x, y - 2, z)
    }

    return !MovementHelper.isSolidState(ctx.bsa, x, y - 1, z, below)
  }

  private fun isWall(x: Int, y: Int, z: Int): Boolean {
    return isBlockingWall(x, y, z) || isBlockingWall(x, y + 1, z)
  }

  private fun isBlockingWall(x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z)
    if (state.isAir) return false

    val block = state.block

    if (block is CarpetBlock || block is SlabBlock ||
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

    if (!MovementHelper.isSolidState(ctx.bsa, x, y, z, state)) return false

    val shape = state.getCollisionShape(ctx.bsa.access, ctx.bsa.mutablePos.set(x, y, z))
    if (shape.isEmpty) return false

    val box = shape.boundingBox
    return box.maxY - box.minY >= 0.5
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

  fun clearCache() {
    penaltyCache.clear()
    edgeCache.clear()
    wallCache.clear()
  }
}
