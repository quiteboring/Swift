package dev.quiteboring.swift.calc

import net.minecraft.util.math.BlockPos

object KeyNodeExtractor {

  private const val MAX_SEGMENT_LENGTH = 24

  fun extract(points: List<BlockPos>): List<KeyNode> {
    if (points.isEmpty()) return emptyList()

    if (points.size == 1) {
      return listOf(KeyNode(points[0].x, points[0].y, points[0].z, KeyNodeType.START))
    }

    if (points.size == 2) {
      return listOf(
        KeyNode(points[0].x, points[0].y, points[0].z, KeyNodeType.START),
        KeyNode(points[1].x, points[1].y, points[1].z, KeyNodeType.END)
      )
    }

    val result = ArrayList<KeyNode>(points.size / 4 + 2)
    result.add(KeyNode(points[0].x, points[0].y, points[0].z, KeyNodeType.START))

    var lastKeyIdx = 0
    var prevDx = points[1].x - points[0].x
    var prevDy = points[1].y - points[0].y
    var prevDz = points[1].z - points[0].z

    for (i in 1 until points.size - 1) {
      val curr = points[i]
      val next = points[i + 1]
      val dx = next.x - curr.x
      val dy = next.y - curr.y
      val dz = next.z - curr.z

      var type: KeyNodeType? = null

      when {
        prevDy >= 0 && dy < 0 -> type = KeyNodeType.DESCEND_START
        prevDy < 0 && dy >= 0 -> type = KeyNodeType.LANDING
        prevDy <= 0 && dy > 0 -> type = KeyNodeType.ASCEND_START
      }

      if (type == null && (dx != prevDx || dz != prevDz)) {
        type = KeyNodeType.TURN
      }

      if (type == null && i - lastKeyIdx >= MAX_SEGMENT_LENGTH) {
        type = KeyNodeType.WAYPOINT
      }

      if (type != null) {
        result.add(KeyNode(curr.x, curr.y, curr.z, type))
        lastKeyIdx = i
      }

      prevDx = dx
      prevDy = dy
      prevDz = dz
    }

    val last = points.last()
    result.add(KeyNode(last.x, last.y, last.z, KeyNodeType.END))

    return result
  }

}
