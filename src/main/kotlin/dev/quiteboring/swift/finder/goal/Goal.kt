package dev.quiteboring.swift.finder.goal

import dev.quiteboring.swift.finder.movement.CalculationContext
import kotlin.math.abs

class Goal(
  @JvmField val goalX: Int,
  @JvmField val goalY: Int,
  @JvmField val goalZ: Int,
  ctx: CalculationContext
) : IGoal {

  // precompute cuz it's slightly faster ig.
  private val sprintCost = ctx.cost.SPRINT_ONE_BLOCK_TIME
  private val diagonalCost = sprintCost * 1.4142135623730951 // sqrt(2) PRECALCULATED IS FASTER NATHAN.
  private val fallCostPerBlock = ctx.cost.getFallTime(2) * 0.5
  private val jumpCostPerBlock = ctx.cost.JUMP_UP_ONE_BLOCK_TIME

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return x == goalX && y == goalY && z == goalZ
  }

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(x - goalX)
    val dy = y - goalY
    val dz = abs(z - goalZ)

    val minHoriz = if (dx < dz) dx else dz
    val maxHoriz = if (dx > dz) dx else dz
    val horizontal = minHoriz * diagonalCost + (maxHoriz - minHoriz) * sprintCost

    val vertical = if (dy > 0) {
      dy * fallCostPerBlock
    } else if (dy < 0) {
      -dy * jumpCostPerBlock
    } else {
      0.0
    }

    return horizontal + vertical
  }
}
