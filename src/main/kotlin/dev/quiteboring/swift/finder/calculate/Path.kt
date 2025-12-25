package dev.quiteboring.swift.finder.calculate

import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.MovementHelper
import net.minecraft.util.math.BlockPos

class Path(private val ctx: CalculationContext, endNode: PathNode, val timeTaken: Long) {

  val points: List<BlockPos>
  val keyNodes: List<BlockPos>

  init {
    var curr: PathNode? = endNode
    val list = mutableListOf<BlockPos>()

    while (curr != null) {
      list.addFirst(BlockPos(curr.x, curr.y, curr.z))
      curr = curr.parent
    }

    points = list
    keyNodes = extractKeyPoints(points)
  }

  private fun extractKeyPoints(
    points: List<BlockPos>,
    epsilon: Double = 1.5,
    maxSegmentLength: Int = 15
  ): List<BlockPos> {
    if (points.size <= 2) return points

    val result = ArrayList<BlockPos>()

    fun simplify(from: Int, to: Int) {
      if (to - from <= 1) {
        result.add(points[from])
        return
      }

      val start = points[from]
      val end = points[to]

      val dx = (end.x - start.x).toDouble()
      val dy = (end.y - start.y).toDouble()
      val dz = (end.z - start.z).toDouble()
      val lenSq = dx * dx + dy * dy + dz * dz

      var maxDist = 0.0
      var maxIndex = -1

      for (i in from + 1 until to) {
        val px = (points[i].x - start.x).toDouble()
        val py = (points[i].y - start.y).toDouble()
        val pz = (points[i].z - start.z).toDouble()

        val t = if (lenSq == 0.0) 0.0
        else ((px * dx + py * dy + pz * dz) / lenSq).coerceIn(0.0, 1.0)

        val cx = start.x + dx * t
        val cy = start.y + dy * t
        val cz = start.z + dz * t

        val distSq =
          (points[i].x - cx) * (points[i].x - cx) +
            (points[i].y - cy) * (points[i].y - cy) +
            (points[i].z - cz) * (points[i].z - cz)

        if (distSq > maxDist) {
          maxDist = distSq
          maxIndex = i
        }
      }

      var walkable = true
      var i = from + 1

      while (i < to) {
        val p = points[i]
        if (!MovementHelper.isSafe(ctx, p.x, p.y, p.z)) {
          walkable = false
          break
        }
        i++
      }

      val segmentLength = to - from

      if (maxIndex == -1) {
        maxIndex = (from + to) ushr 1
      }

      if (
        maxDist > epsilon * epsilon ||
        segmentLength > maxSegmentLength ||
        !walkable
      ) {
        simplify(from, maxIndex)
        simplify(maxIndex, to)
      } else {
        result.add(start)
      }
    }

    simplify(0, points.lastIndex)
    result.add(points.last())
    return result
  }

}
