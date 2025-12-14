package dev.quiteboring.swift.goal

import dev.quiteboring.swift.movement.CalculationContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Goal(val goalX: Int, val goalY: Int, val goalZ: Int, val ctx: CalculationContext) : IGoal {

  private val sqrt2 = sqrt(2.0)

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return x == goalX && y == goalY && z == goalZ
  }

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = x - goalX
    val dy = y - goalY
    val dz = z - goalZ

    val vertical = when {
      dy > 0 -> ctx.cost.N_BLOCK_FALL_COST[2] / 2 * dy
      dy < 0 -> -dy * ctx.cost.ONE_BLOCK_SPRINT_COST
      else -> 0.0
    }

    val absX = abs(dx)
    val absZ = abs(dz)
    val diag = min(absX, absZ)
    val straight = max(absX, absZ) - diag

    return vertical + (diag * sqrt2 + straight) * 3.563
  }

}
