package dev.quiteboring.swift.calc

import net.minecraft.util.math.BlockPos

class Path(endNode: PathNode, val timeTaken: Long) {

  val points: List<BlockPos>

  init {
    var curr: PathNode? = endNode
    val list = mutableListOf<BlockPos>()

    while (curr != null) {
      list.addFirst(BlockPos(curr.x, curr.y, curr.z))
      curr = curr.parent
    }

    points = list
  }

}
