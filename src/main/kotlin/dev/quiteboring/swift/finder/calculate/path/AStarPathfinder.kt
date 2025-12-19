package dev.quiteboring.swift.finder.calculate.path

import dev.quiteboring.swift.finder.calculate.Path
import dev.quiteboring.swift.finder.calculate.PathNode
import dev.quiteboring.swift.finder.calculate.openset.BinaryHeapOpenSet
import dev.quiteboring.swift.finder.goal.IGoal
import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementResult
import dev.quiteboring.swift.finder.movement.Moves
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

class AStarPathfinder(
  private val startX: Int,
  private val startY: Int,
  private val startZ: Int,
  private val goal: IGoal,
  private val ctx: CalculationContext,
  private val heuristicWeight: Double = 1.1  // weighted a*, change this if it messes up the paths. but it's faster at 1.1 i think.
) {

  private val closedSet: Long2ObjectMap<PathNode> = Long2ObjectOpenHashMap()
  private val moves = Moves.entries.toTypedArray()
  private val res = MovementResult()

  fun findPath(): Path? {
    val openSet = BinaryHeapOpenSet()
    val startNode = PathNode(startX, startY, startZ, goal)

    startNode.gCost = 0.0
    startNode.fCost = startNode.hCost * heuristicWeight
    openSet.add(startNode)

    val startTime = System.currentTimeMillis()

    while (!openSet.isEmpty()) {
      val currentNode = openSet.poll()

      if (goal.isAtGoal(currentNode.x, currentNode.y, currentNode.z)) {
        return Path(currentNode, System.currentTimeMillis() - startTime)
      }

      for (move in moves) {
        res.reset()
        move.calculate(ctx, currentNode.x, currentNode.y, currentNode.z, res)
        val cost = res.cost
        if (cost >= ctx.cost.INF_COST) continue
        val neighbourNode = getNode(res.x, res.y, res.z, PathNode.coordKey(res.x, res.y, res.z))
        val neighbourCostSoFar = currentNode.gCost + cost

        if (neighbourNode.gCost > neighbourCostSoFar) {
          neighbourNode.parent = currentNode
          neighbourNode.gCost = neighbourCostSoFar
          neighbourNode.fCost = neighbourCostSoFar + neighbourNode.hCost * heuristicWeight

          if (neighbourNode.heapPosition == -1) {
            openSet.add(neighbourNode)
          } else {
            openSet.relocate(neighbourNode)
          }
        }
      }
    }

    return null
  }

  private fun getNode(x: Int, y: Int, z: Int, hash: Long): PathNode {
    var n: PathNode? = closedSet.get(hash)

    if (n == null) {
      n = PathNode(x, y, z, goal)
      closedSet.put(hash, n)
    }

    return n
  }

}
