package dev.quiteboring.swift.calc.path

import dev.quiteboring.swift.calc.Path
import dev.quiteboring.swift.calc.PathNode
import dev.quiteboring.swift.calc.openset.BinaryHeapOpenSet
import dev.quiteboring.swift.goal.IGoal
import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.MovementResult
import dev.quiteboring.swift.movement.Moves
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

class AStarPathfinder(
  private val startX: Int,
  private val startY: Int,
  private val startZ: Int,
  private val goal: IGoal,
  private val ctx: CalculationContext,
  private val maxIterations: Int = 500_000,
  private val timeoutMs: Long = 10_000,
  private val heuristicWeight: Double = 1.1  // weighted a*, change this if it messes up the paths. but it's faster at 1.1 i think.
) {
  private val nodeMap = Long2ObjectOpenHashMap<PathNode>()
  private val moves = Moves.entries.toTypedArray()
  private val res = MovementResult()

  fun findPath(): Path? {
    val openSet = BinaryHeapOpenSet()
    val startNode = getOrCreateNode(startX, startY, startZ)

    startNode.gCost = 0.0
    startNode.fCost = startNode.hCost * heuristicWeight
    openSet.add(startNode)

    val startTime = System.currentTimeMillis()
    var iterations = 0
    val timeCheckMask = 0x3FF

    while (!openSet.isEmpty()) {
      iterations++

      if (iterations > maxIterations) {
        println("Pathfinding exceeded max iterations: $iterations, nodes: ${nodeMap.size}")
        return findBestPartialPath(startTime)
      }

      if ((iterations and timeCheckMask) == 0) {
        if (System.currentTimeMillis() - startTime > timeoutMs) {
          println("Pathfinding timed out after ${System.currentTimeMillis() - startTime}ms")
          return findBestPartialPath(startTime)
        }
      }

      val current = openSet.poll()
      val cx = current.x
      val cy = current.y
      val cz = current.z

      if (goal.isAtGoal(cx, cy, cz)) {
        val elapsed = System.currentTimeMillis() - startTime
        println("Path found! ${iterations} iterations, ${elapsed}ms, ${nodeMap.size} nodes")
        return Path(current, elapsed)
      }

      val currentCost = current.gCost

      for (move in moves) {
        res.reset()
        move.calculate(ctx, cx, cy, cz, res)

        val moveCost = res.cost
        if (moveCost >= ctx.cost.INF_COST) continue

        val nx = res.x
        val ny = res.y
        val nz = res.z

        val neighbour = getOrCreateNode(nx, ny, nz)
        val newCost = currentCost + moveCost

        if (newCost < neighbour.gCost) {
          neighbour.parent = current
          neighbour.gCost = newCost
          neighbour.fCost = newCost + neighbour.hCost * heuristicWeight

          if (neighbour.heapPosition == -1) {
            openSet.add(neighbour)
          } else {
            openSet.relocate(neighbour)
          }
        }
      }
    }

    println("No path found. Explored ${nodeMap.size} nodes in ${System.currentTimeMillis() - startTime}ms")
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

  private fun findBestPartialPath(startTime: Long): Path? {
    var bestNode: PathNode? = null
    var bestHCost = Double.MAX_VALUE

    for (node in nodeMap.values) {
      if (node.parent != null && node.hCost < bestHCost) {
        bestHCost = node.hCost
        bestNode = node
      }
    }

    return bestNode?.let {
      println("Returning partial path to (${it.x}, ${it.y}, ${it.z}), hCost: $bestHCost")
      Path(it, System.currentTimeMillis() - startTime)
    }
  }
}
