package dev.quiteboring.swift.calc

import dev.quiteboring.swift.goal.IGoal

class PathNode(
  val x: Int,
  val y: Int,
  val z: Int,
  goal: IGoal,
) {

  var gCost: Double = 1e6
  var hCost: Double = goal.heuristic(x, y, z)
  var fCost: Double = 1.0
  var heapPosition = -1
  var parent: PathNode? = null

  override fun equals(other: Any?): Boolean {
    val node = other as PathNode
    return node.x == this.x && node.y == this.y && node.z == this.z
  }

  override fun hashCode(): Int {
    return longHash(this.x, this.y, this.z).toInt()
  }

  companion object {
    fun longHash(x: Int, y: Int, z: Int): Long {
      var hash = 3241L
      hash = 3457689L * hash + x
      hash = 8734625L * hash + y
      hash = 2873465L * hash + z
      return hash
    }
  }

}
