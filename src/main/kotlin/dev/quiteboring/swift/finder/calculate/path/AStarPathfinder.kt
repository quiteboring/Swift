package dev.quiteboring.swift.finder.calculate.path

import dev.quiteboring.swift.finder.calculate.Path
import dev.quiteboring.swift.finder.calculate.PathNode
import dev.quiteboring.swift.finder.calculate.openset.BinaryHeapOpenSet
import dev.quiteboring.swift.finder.goal.IGoal
import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementResult
import dev.quiteboring.swift.finder.movement.Moves
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

/**
 * Thank you EpsilonPhoenix for this superb class!
 */
class AStarPathfinder(
  private val startX: Int,
  private val startY: Int,
  private val startZ: Int,
  private val goal: IGoal,
  private val ctx: CalculationContext,
  private val maxIterations: Int = 500_000,
  private val heuristicWeight: Double = 1.05 // was 1.1. made it lower because maybe it's better on long path?
) {

  private val nodeMap = Long2ObjectOpenHashMap<PathNode>(4096)
  private val openSet = BinaryHeapOpenSet(4096)
  private val res = MovementResult()
  private val infCost = ctx.cost.INF_COST

  companion object {
    @JvmField
    val MOVES = Moves.entries.toTypedArray()
  }

  fun findPath(): Path? {
    val startNode = getOrCreateNode(startX, startY, startZ)
    startNode.gCost = 0.0
    startNode.fCost = startNode.hCost * heuristicWeight
    openSet.add(startNode)

    val startTime = System.nanoTime()
    var iterations = 0

    while (openSet.isNotEmpty() && iterations < maxIterations) {
      iterations++

      val current = openSet.poll()
      val cx = current.x
      val cy = current.y
      val cz = current.z

      if (goal.isAtGoal(cx, cy, cz)) {
        val elapsed = (System.nanoTime() - startTime) / 1_000_000
        return Path(current, elapsed)
      }

      val currentCost = current.gCost

      for (move in MOVES) {
        res.reset()
        move.calculate(ctx, cx, cy, cz, res)

        val moveCost = res.cost
        if (moveCost >= infCost) continue

        val nx = res.x
        val ny = res.y
        val nz = res.z

        val neighbourKey = PathNode.coordKey(nx, ny, nz)
        var neighbour = nodeMap.get(neighbourKey)

        val newCost = currentCost + moveCost

        if (neighbour == null) {
          neighbour = PathNode(nx, ny, nz, goal)
          nodeMap.put(neighbourKey, neighbour)
          neighbour.parent = current
          neighbour.gCost = newCost
          neighbour.fCost = newCost + neighbour.hCost * heuristicWeight
          openSet.add(neighbour)
        } else if (newCost < neighbour.gCost) {
          neighbour.parent = current
          neighbour.gCost = newCost
          neighbour.fCost = newCost + neighbour.hCost * heuristicWeight

          if (neighbour.heapPosition != -1) {
            openSet.relocate(neighbour)
          } else {
            openSet.add(neighbour)
          }
        }
      }
    }

    return null
  }

  private fun getOrCreateNode(x: Int, y: Int, z: Int): PathNode {
    val key = PathNode.coordKey(x, y, z)
    var node = nodeMap.get(key)

    if (node == null) {
      node = PathNode(x, y, z, goal)
      nodeMap.put(key, node)
    }

    return node
  }

}
