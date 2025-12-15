package dev.quiteboring.swift.calc.path

import dev.quiteboring.swift.calc.Path
import dev.quiteboring.swift.calc.PathNode
import dev.quiteboring.swift.calc.openset.BinaryHeapOpenSet
import dev.quiteboring.swift.goal.IGoal
import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.MovementResult
import dev.quiteboring.swift.movement.Moves
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

class AStarPathfinder(
    private val startX: Int,
    private val startY: Int,
    private val startZ: Int,
    private val goal: IGoal,
    private val ctx: CalculationContext
) {

  private val closedSet: Long2ObjectMap<PathNode> = Long2ObjectOpenHashMap()

  fun findPath(): Path? {
    val openSet = BinaryHeapOpenSet()
    val startNode = PathNode(startX, startY, startZ, goal)
    val res = MovementResult()
    val moves = Moves.entries

    startNode.gCost = 0.0
    startNode.fCost = startNode.hCost
    openSet.add(startNode)

    val startTime = System.currentTimeMillis()

    while (!openSet.isEmpty()) {
      val currentNode = openSet.poll()
      // println("Popped node: ${currentNode.x}, ${currentNode.y}, ${currentNode.z} (Cost: ${currentNode.gCost})")

      if (goal.isAtGoal(currentNode.x, currentNode.y, currentNode.z)) {
        return Path(currentNode, System.currentTimeMillis() - startTime)
      }

      for (move in moves) {
        res.reset()
        move.calculate(ctx, currentNode.x, currentNode.y, currentNode.z, res)

        val cost = res.cost

        if (cost >= ctx.cost.INF_COST) {
            // println("Move ${move} failed. Cost infinite.")
            continue
        }
        // println("Move ${move} succeeded to ${res.x}, ${res.y}, ${res.z} cost $cost")

        val neighbourNode = getNode(res.x, res.y, res.z, PathNode.Companion.longHash(res.x, res.y, res.z))
        val neighbourCostSoFar = currentNode.gCost + cost

        if (neighbourNode.gCost > neighbourCostSoFar) {
          neighbourNode.parent = currentNode
          neighbourNode.gCost = neighbourCostSoFar
          neighbourNode.fCost = neighbourCostSoFar + neighbourNode.hCost

          if (neighbourNode.heapPosition == -1) {
            openSet.add(neighbourNode)
          } else {
            openSet.relocate(neighbourNode)
          }
        }
      }
    }

    println("Unable to generate a path. OpenSet empty. ClosedSet size: ${closedSet.size}")
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
