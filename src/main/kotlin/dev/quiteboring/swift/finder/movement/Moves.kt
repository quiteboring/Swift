package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.finder.movement.movements.*

enum class Moves(val offsetX: Int, val offsetZ: Int) {

  TRAVERSE_NORTH(0, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementTraverse.calculateCost(ctx, px, py, pz, px, pz - 1, res)
  },
  TRAVERSE_SOUTH(0, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementTraverse.calculateCost(ctx, px, py, pz, px, pz + 1, res)
  },
  TRAVERSE_EAST(1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementTraverse.calculateCost(ctx, px, py, pz, px + 1, pz, res)
  },
  TRAVERSE_WEST(-1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementTraverse.calculateCost(ctx, px, py, pz, px - 1, pz, res)
  },

  DIAGONAL_NORTHEAST(1, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDiagonal.calculateCost(ctx, px, py, pz, px + 1, pz - 1, res)
  },
  DIAGONAL_NORTHWEST(-1, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDiagonal.calculateCost(ctx, px, py, pz, px - 1, pz - 1, res)
  },
  DIAGONAL_SOUTHEAST(1, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDiagonal.calculateCost(ctx, px, py, pz, px + 1, pz + 1, res)
  },
  DIAGONAL_SOUTHWEST(-1, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDiagonal.calculateCost(ctx, px, py, pz, px - 1, pz + 1, res)
  },

  // Vertical movements
  ASCEND_NORTH(0, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementAscend.calculateCost(ctx, px, py, pz, px, pz - 1, res)
  },
  ASCEND_SOUTH(0, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementAscend.calculateCost(ctx, px, py, pz, px, pz + 1, res)
  },
  ASCEND_EAST(1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementAscend.calculateCost(ctx, px, py, pz, px + 1, pz, res)
  },
  ASCEND_WEST(-1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementAscend.calculateCost(ctx, px, py, pz, px - 1, pz, res)
  },

  DESCEND_NORTH(0, -1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDescend.calculateCost(ctx, px, py, pz, px, pz - 1, res)
  },
  DESCEND_SOUTH(0, 1) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDescend.calculateCost(ctx, px, py, pz, px, pz + 1, res)
  },
  DESCEND_EAST(1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDescend.calculateCost(ctx, px, py, pz, px + 1, pz, res)
  },
  DESCEND_WEST(-1, 0) {
    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
      MovementDescend.calculateCost(ctx, px, py, pz, px - 1, pz, res)
  };

//  JUMP_GAP_1_NORTH(0, -2) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px, pz - 2, res)
//  },
//  JUMP_GAP_1_SOUTH(0, 2) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px, pz + 2, res)
//  },
//  JUMP_GAP_1_EAST(2, 0) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px + 2, pz, res)
//  },
//  JUMP_GAP_1_WEST(-2, 0) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px - 2, pz, res)
//  },
//
//  JUMP_GAP_2_NORTH(0, -3) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px, pz - 3, res)
//  },
//  JUMP_GAP_2_SOUTH(0, 3) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px, pz + 3, res)
//  },
//  JUMP_GAP_2_EAST(3, 0) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px + 3, pz, res)
//  },
//  JUMP_GAP_2_WEST(-3, 0) {
//    override fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult) =
//      MovementJumpGap.calculateCost(ctx, px, py, pz, px - 3, pz, res)
//  };

  abstract fun calculate(ctx: CalculationContext, px: Int, py: Int, pz: Int, res: MovementResult)
}
