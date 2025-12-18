package dev.quiteboring.swift.api

import dev.quiteboring.swift.calc.KeyNode
import dev.quiteboring.swift.calc.KeyNodeExtractor
import dev.quiteboring.swift.calc.Path
import dev.quiteboring.swift.calc.path.AStarPathfinder
import dev.quiteboring.swift.goal.Goal
import dev.quiteboring.swift.movement.CalculationContext
import net.minecraft.client.MinecraftClient

object PathManager {

  @JvmStatic
  var lastPath: Path? = null
    private set

  @JvmStatic
  var lastKeyNodes: List<KeyNode> = emptyList()
    private set

  @JvmStatic
  var lastError: String? = null
    private set

  @JvmStatic
  var lastTimeMs: Long = 0
    private set

  @JvmStatic
  fun findPath(startX: Int, startY: Int, startZ: Int, endX: Int, endY: Int, endZ: Int): Boolean {
    val mc = MinecraftClient.getInstance()

    if (mc.world == null || mc.player == null) {
      lastError = "Not in world"
      lastPath = null
      lastKeyNodes = emptyList()
      return false
    }

    return try {
      val ctx = CalculationContext()
      val goal = Goal(endX, endY, endZ, ctx)
      val pathfinder = AStarPathfinder(startX, startY, startZ, goal, ctx)
      val path = pathfinder.findPath()

      if (path == null) {
        lastError = "No path found"
        lastPath = null
        lastKeyNodes = emptyList()
        return false
      }

      lastPath = path
      lastKeyNodes = KeyNodeExtractor.extract(path.points)
      lastTimeMs = path.timeTaken
      lastError = null

      println("[Swift] Path: ${path.points.size} nodes, ${lastKeyNodes.size} keynodes, ${path.timeTaken}ms")
      true

    } catch (e: Exception) {
      lastError = e.message ?: "Unknown error"
      lastPath = null
      lastKeyNodes = emptyList()
      e.printStackTrace()
      false
    }
  }

  @JvmStatic
  fun getPathArray(): IntArray {
    val path = lastPath ?: return IntArray(0)
    val result = IntArray(path.points.size * 3)
    var i = 0
    for (p in path.points) {
      result[i++] = p.x
      result[i++] = p.y - 1
      result[i++] = p.z
    }
    return result
  }

  @JvmStatic
  fun getKeyNodesArray(): IntArray {
    val result = IntArray(lastKeyNodes.size * 3)
    var i = 0
    for (n in lastKeyNodes) {
      result[i++] = n.x
      result[i++] = n.y - 1
      result[i++] = n.z
    }
    return result
  }

  @JvmStatic
  fun getPathSize(): Int = lastPath?.points?.size ?: 0

  @JvmStatic
  fun getKeyNodeCount(): Int = lastKeyNodes.size

  @JvmStatic
  fun clear() {
    lastPath = null
    lastKeyNodes = emptyList()
    lastError = null
    lastTimeMs = 0
  }
}
