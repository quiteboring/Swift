package dev.quiteboring.swift.movement

import dev.quiteboring.swift.calc.PathNode
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import net.minecraft.block.*
import kotlin.math.abs
import kotlin.math.min

class WallDistanceCalculator(private val ctx: CalculationContext) {

  companion object {
    const val MAX_DIST = 6  // Reduced from 8
    private val EDGE_PENALTIES = doubleArrayOf(50.0, 20.0, 8.0, 2.0, 0.0, 0.0, 0.0)
    private val WALL_PENALTIES = doubleArrayOf(20.0, 8.0, 3.0, 1.0, 0.0, 0.0, 0.0)
  }

  // Single cache for the final penalty - most important optimization
  private val penaltyCache = Long2DoubleOpenHashMap().apply {
    defaultReturnValue(Double.NaN)
  }

  // Keep separate caches only for debug/heatmap
  private val edgeCache = Long2IntOpenHashMap().apply { defaultReturnValue(-1) }
  private val wallCache = Long2IntOpenHashMap().apply { defaultReturnValue(-1) }

  fun getPathPenalty(x: Int, y: Int, z: Int): Double {
    val key = PathNode.coordKey(x, y, z)
    var penalty = penaltyCache.get(key)
    if (java.lang.Double.isNaN(penalty)) {
      penalty = computePenaltyFast(x, y, z)
      penaltyCache.put(key, penalty)
    }
    return penalty
  }

  private fun computePenaltyFast(x: Int, y: Int, z: Int): Double {
    var minEdge = MAX_DIST
    var wallN = MAX_DIST
    var wallS = MAX_DIST
    var wallE = MAX_DIST
    var wallW = MAX_DIST

    // North - combined edge + wall scan
    for (d in 1..MAX_DIST) {
      val nz = z - d
      val foundEdge = minEdge == MAX_DIST && isEdgeFast(x, y, nz)
      val foundWall = wallN == MAX_DIST && isWallFast(x, y, nz)

      if (foundEdge) minEdge = d - 1
      if (foundWall) wallN = d - 1

      if (minEdge == 0) return 70.0  // Early exit - maximum penalty
      if (foundWall) break
    }

    // South
    for (d in 1..MAX_DIST) {
      val nz = z + d
      if (minEdge == MAX_DIST && isEdgeFast(x, y, nz)) {
        minEdge = d - 1
        if (minEdge == 0) return 70.0
      }
      if (wallS == MAX_DIST && isWallFast(x, y, nz)) {
        wallS = d - 1
        break
      }
    }

    // East
    for (d in 1..MAX_DIST) {
      val nx = x + d
      if (minEdge == MAX_DIST && isEdgeFast(nx, y, z)) {
        minEdge = d - 1
        if (minEdge == 0) return 70.0
      }
      if (wallE == MAX_DIST && isWallFast(nx, y, z)) {
        wallE = d - 1
        break
      }
    }

    // West
    for (d in 1..MAX_DIST) {
      val nx = x - d
      if (minEdge == MAX_DIST && isEdgeFast(nx, y, z)) {
        minEdge = d - 1
        if (minEdge == 0) return 70.0
      }
      if (wallW == MAX_DIST && isWallFast(nx, y, z)) {
        wallW = d - 1
        break
      }
    }

    // Calculate penalties
    val edgePenalty = EDGE_PENALTIES[minEdge.coerceIn(0, 6)]

    // Corridor detection
    val nsWidth = if (wallN < MAX_DIST && wallS < MAX_DIST) wallN + wallS + 1 else 0
    val ewWidth = if (wallE < MAX_DIST && wallW < MAX_DIST) wallE + wallW + 1 else 0

    val wallPenalty = when {
      nsWidth > 0 && (ewWidth == 0 || nsWidth <= ewWidth) ->
        corridorPenalty(nsWidth, abs(wallN - wallS))
      ewWidth > 0 ->
        corridorPenalty(ewWidth, abs(wallE - wallW))
      else ->
        WALL_PENALTIES[minOf(wallN, wallS, wallE, wallW).coerceIn(0, 6)]
    }

    return edgePenalty + wallPenalty
  }

  private inline fun corridorPenalty(width: Int, offset: Int): Double {
    val ratio = offset.toDouble() / (width / 2.0).coerceAtLeast(0.001)
    return when {
      ratio <= 0.3 -> 0.0
      ratio <= 0.5 -> 2.0
      ratio <= 0.7 -> 6.0
      ratio <= 0.85 -> 12.0
      else -> 25.0
    }
  }

  private fun isEdgeFast(x: Int, y: Int, z: Int): Boolean {
    // Quick check: solid ground at foot level = no edge
    val below = ctx.get(x, y - 1, z)
    if (below != null && !below.isAir) {
      val block = below.block
      if (block !is CarpetBlock && MovementHelper.isSolidState(ctx, below, x, y - 1, z)) {
        return false
      }
    }

    // Scan down for ground (reduced range)
    for (cy in (y - 2) downTo (y - 4)) {
      val state = ctx.get(x, cy, z) ?: continue
      if (state.isAir) continue
      if (state.block is CarpetBlock) continue
      if (MovementHelper.isSolidState(ctx, state, x, cy, z)) {
        return (y - 1) - cy >= 3
      }
    }
    return true  // No ground found = edge
  }

  private fun isWallFast(x: Int, y: Int, z: Int): Boolean {
    return isBlockingWallFast(x, y, z) || isBlockingWallFast(x, y + 1, z)
  }

  private fun isBlockingWallFast(x: Int, y: Int, z: Int): Boolean {
    val state = ctx.get(x, y, z) ?: return false
    if (state.isAir) return false

    val block = state.block

    // Fast rejection of common non-blocking blocks (single when expression)
    when (block) {
      is CarpetBlock, is SlabBlock, is StairsBlock,
      is DoorBlock, is TrapdoorBlock, is TorchBlock,
      is SignBlock, is WallSignBlock, is PlantBlock,
      is TallPlantBlock, is AbstractRailBlock, is VineBlock,
      is LadderBlock, is SnowBlock, is PressurePlateBlock,
      is ButtonBlock, is RedstoneWireBlock, is LeverBlock,
      is BannerBlock, is WallBannerBlock, is TripwireBlock,
      is TripwireHookBlock, is FlowerBlock -> return false

      is FenceBlock, is FenceGateBlock, is WallBlock -> return true
    }

    // Check collision shape
    if (!MovementHelper.isSolidState(ctx, state, x, y, z)) return false

    ctx.bsa.mutablePos.set(x, y, z)
    val shape = state.getCollisionShape(ctx.world, ctx.bsa.mutablePos)
    if (shape.isEmpty) return false

    val bounds = shape.boundingBox
    return (bounds.maxY - bounds.minY) >= 0.5
  }

  // Debug methods - keep for heatmap
  fun getEdgeDistance(x: Int, y: Int, z: Int): Int {
    val key = PathNode.coordKey(x, y, z)
    var dist = edgeCache.get(key)
    if (dist == -1) {
      dist = calculateEdgeDistanceDebug(x, y, z)
      edgeCache.put(key, dist)
    }
    return dist
  }

  fun getWallDistance(x: Int, y: Int, z: Int): Int {
    val key = PathNode.coordKey(x, y, z)
    var dist = wallCache.get(key)
    if (dist == -1) {
      dist = calculateWallDistanceDebug(x, y, z)
      wallCache.put(key, dist)
    }
    return dist
  }

  private fun calculateEdgeDistanceDebug(x: Int, y: Int, z: Int): Int {
    var minDist = MAX_DIST
    val dirs = arrayOf(intArrayOf(0, -1), intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(-1, 0))
    for (dir in dirs) {
      for (d in 1..MAX_DIST) {
        if (isEdgeFast(x + dir[0] * d, y, z + dir[1] * d)) {
          minDist = min(minDist, d - 1)
          break
        }
      }
      if (minDist == 0) return 0
    }
    return minDist
  }

  private fun calculateWallDistanceDebug(x: Int, y: Int, z: Int): Int {
    var minDist = MAX_DIST
    val dirs = arrayOf(intArrayOf(0, -1), intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(-1, 0))
    for (dir in dirs) {
      for (d in 1..MAX_DIST) {
        if (isWallFast(x + dir[0] * d, y, z + dir[1] * d)) {
          minDist = min(minDist, d - 1)
          break
        }
      }
      if (minDist == 0) return 0
    }
    return minDist
  }

  fun getCorridorInfo(x: Int, y: Int, z: Int): IntArray {
    val dists = getDirectionalWallDistances(x, y, z)
    val nsWidth = if (dists[0] < MAX_DIST && dists[1] < MAX_DIST) dists[0] + dists[1] + 1 else 0
    val ewWidth = if (dists[2] < MAX_DIST && dists[3] < MAX_DIST) dists[2] + dists[3] + 1 else 0

    return if (nsWidth > 0 && (ewWidth == 0 || nsWidth <= ewWidth)) {
      intArrayOf(minOf(dists[0], dists[1]), nsWidth, abs(dists[0] - dists[1]))
    } else if (ewWidth > 0) {
      intArrayOf(minOf(dists[2], dists[3]), ewWidth, abs(dists[2] - dists[3]))
    } else {
      intArrayOf(dists.min(), 0, 0)
    }
  }

  fun getDirectionalWallDistances(x: Int, y: Int, z: Int): IntArray {
    val result = IntArray(4) { MAX_DIST }
    val dirs = arrayOf(intArrayOf(0, -1), intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(-1, 0))
    for (i in dirs.indices) {
      for (d in 1..MAX_DIST) {
        if (isWallFast(x + dirs[i][0] * d, y, z + dirs[i][1] * d)) {
          result[i] = d - 1
          break
        }
      }
    }
    return result
  }

  fun clearCache() {
    penaltyCache.clear()
    edgeCache.clear()
    wallCache.clear()
  }

  fun isWallBlockDebug(x: Int, y: Int, z: Int) = isBlockingWallFast(x, y, z)
}
