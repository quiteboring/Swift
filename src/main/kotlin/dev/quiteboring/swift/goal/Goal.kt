package dev.quiteboring.swift.goal

import dev.quiteboring.swift.movement.CalculationContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Goal(
  val goalX: Int,
  val goalY: Int,
  val goalZ: Int,
  val ctx: CalculationContext
) : IGoal {

  private val sqrt2 = 1.4142135623730951  // sqrt(2)

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return x == goalX && y == goalY && z == goalZ
  }

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(goalX - x)
    val dz = abs(goalZ - z)
    val straight = abs(dx - dz).toDouble()
    var vertical = abs(goalY - y).toDouble()
    val diagonal = min(dx, dz).toDouble()

    if (goalY > y) {
      vertical *= 6.234399666206506
    } else {
      vertical *= ctx.cost.getFallTime(2) / 2.0
    }

    return (straight + diagonal * sqrt2) * ctx.cost.SPRINT_ONE_BLOCK_TIME + vertical
  }

}
