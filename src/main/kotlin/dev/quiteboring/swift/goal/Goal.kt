package dev.quiteboring.swift.goal

import dev.quiteboring.swift.movement.CalculationContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Goal(val goalX: Int, val goalY: Int, val goalZ: Int, val ctx: CalculationContext) : IGoal {

  private val sqrt2 = 1.41421356

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return x == goalX && y == goalY && z == goalZ
  }

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(x - goalX).toDouble()
    val dy = abs(y - goalY).toDouble()
    val dz = abs(z - goalZ).toDouble()

    val minHorizontal = min(dx, dz)
    val maxHorizontal = max(dx, dz)

    val horizontalDist = (maxHorizontal - minHorizontal) + (minHorizontal * sqrt2)
    val verticalCost = if (y < goalY) {
      dy * ctx.cost.JUMP_ONE_BLOCK_COST
    } else {
      dy * ctx.cost.ONE_BLOCK_WALK_COST
    }

    return (horizontalDist * ctx.cost.ONE_BLOCK_SPRINT_COST) + verticalCost
  }
}
