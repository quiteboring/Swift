package dev.quiteboring.swift.costs

import kotlin.math.pow

class ActionCosts(
  sprintMovementFactor: Double = 0.13,
  walkingMovementFactor: Double = 0.1,
  sneakingMovementFactor: Double = 0.03,
  jumpBoostAmplifier: Int
) {

  val INF_COST = 1e6

  private fun getWalkingFriction(landMovementFactor: Double): Double {
    return landMovementFactor * ((0.16277136) / (0.91 * 0.91 * 0.91))
  }

  private fun actionTime(friction: Double): Double {
    return friction * 10
  }

  val ONE_BLOCK_WALK_COST = 1 / actionTime(getWalkingFriction(walkingMovementFactor))
  val ONE_BLOCK_SPRINT_COST = 1 / actionTime(getWalkingFriction(sprintMovementFactor))
  val DIAGONAL_SPRINT_COST = ONE_BLOCK_SPRINT_COST * 1.41421356
  val JUMP_ONE_BLOCK_COST: Double = ONE_BLOCK_SPRINT_COST + 0.6
  val JUMP_PENALTY = 0.2
  val N_BLOCK_FALL_COST: DoubleArray = generateNBlocksFallCost()
  val WALK_OFF_ONE_BLOCK_COST = ONE_BLOCK_WALK_COST * 0.8

  private fun downwardMotionAtTick(tick: Int): Double {
    return (0.98.pow(tick.toDouble()) - 1) * -3.92
  }

  private fun generateNBlocksFallCost(): DoubleArray {
    val timeCost = DoubleArray(257)
    var currentDistance = 0.0
    var targetDistance = 1
    var tickCount = 0

    while (true) {
      val velocityAtTick = downwardMotionAtTick(tickCount)
      if (currentDistance + velocityAtTick >= targetDistance) {
        timeCost[targetDistance] = tickCount + (targetDistance - currentDistance) / velocityAtTick
        targetDistance++
        if (targetDistance > 256) break
        continue
      }
      currentDistance += velocityAtTick
      tickCount++
    }
    return timeCost
  }
}
