package dev.quiteboring.swift.finder.goal

import dev.quiteboring.swift.finder.movement.CalculationContext
import kotlin.math.abs

class GoalFly(
  @JvmField val goalX: Int,
  @JvmField val goalY: Int,
  @JvmField val goalZ: Int,
  ctx: CalculationContext,
) : IGoal {

  private val flyOneBlockCost = ctx.cost.FLY_ONE_BLOCK_TIME
  private val altitudeReluctance = ctx.cost.ALTITUDE_RELUCTANCE

  override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
    return x == goalX && y == goalY && z == goalZ
  }

  override fun heuristic(x: Int, y: Int, z: Int): Double {
    val dx = abs(x - goalX)
    val dy = abs(y - goalY)
    val dz = abs(z - goalZ)

    val distance = kotlin.math.sqrt(
      (dx * dx + dy * dy + dz * dz).toDouble()
    )

    val base = distance * flyOneBlockCost
    val yBias = dy * altitudeReluctance

    return distance * flyOneBlockCost
  }
}
