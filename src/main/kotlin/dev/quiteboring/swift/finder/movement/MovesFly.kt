package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.finder.movement.movements.fly.MovementFlyAscend
import dev.quiteboring.swift.finder.movement.movements.fly.MovementFlyDescend
import dev.quiteboring.swift.finder.movement.movements.fly.MovementFlyDiagonal
import dev.quiteboring.swift.finder.movement.movements.fly.MovementFlyTraverse

enum class MovesFly(val offsetX: Int, val offsetY: Int, val offsetZ: Int) : IMove {

  FLY_TRAVERSE_NORTH(0, 0, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyTraverse.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  FLY_TRAVERSE_SOUTH(0, 0, +1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyTraverse.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  FLY_TRAVERSE_EAST(+1, 0, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyTraverse.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  FLY_TRAVERSE_WEST(-1, 0, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyTraverse.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },


  DIAGONAL_NORTHEAST(1, 0, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyDiagonal.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  DIAGONAL_NORTHWEST(-1, 0, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyDiagonal.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  DIAGONAL_SOUTHEAST(1, 0, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyDiagonal.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  DIAGONAL_SOUTHWEST(-1, 0, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyDiagonal.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },

  FLY_ASCEND(0, +1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyAscend.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  },
  FLY_DESCEND(0, -1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementFlyDescend.calculateCost(ctx, px, py, pz, px + offsetX, py + offsetY, pz + offsetZ, res)
  };

}
