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

  private fun extractKeyPoints(points: List<BlockPos>): List<BlockPos> = points

}
