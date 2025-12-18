package dev.quiteboring.swift.calc

enum class KeyNodeType {
  START,
  TURN,
  ASCEND_START,
  DESCEND_START,
  LANDING,
  WAYPOINT,
  END
}

data class KeyNode(
  val x: Int,
  val y: Int,
  val z: Int,
  val type: KeyNodeType
)
