// VIBECODED HEATMAP. I DON'T WANT TO MAKE THIS MYSELF!
package dev.quiteboring.swift.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import dev.quiteboring.swift.event.Context
import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.movement.MovementHelper
import dev.quiteboring.swift.util.PlayerUtils
import dev.quiteboring.swift.util.render.drawBox
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.block.CarpetBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color

object HeatmapCommand {

  private var heatmapData: HeatmapData? = null
  private var showHeatmap = false
  private var heatmapMode = HeatmapMode.PENALTY

  enum class HeatmapMode {
    PENALTY,
    EDGE,
    WALL,
    CORRIDOR,
    DEBUG
  }

  data class HeatmapData(
    val center: BlockPos,
    val radius: Int,
    val ctx: CalculationContext,
    val points: MutableList<HeatmapPoint> = mutableListOf()
  )

  data class HeatmapPoint(
    val x: Int,
    val y: Int,
    val z: Int,
    val edgeDist: Int,
    val wallDist: Int,
    val corridorWidth: Int,
    val centerOffset: Int,
    val penalty: Double,
    val wallDistNorth: Int,
    val wallDistSouth: Int,
    val wallDistEast: Int,
    val wallDistWest: Int
  )

  fun dispatch() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      val radiusArg = ClientCommandManager.argument("radius", IntegerArgumentType.integer(1, 64))

      val heatmapCommand = ClientCommandManager.literal("heatmap")
        .then(radiusArg.executes { context ->
          val radius = IntegerArgumentType.getInteger(context, "radius")
          generateHeatmap(radius)
          1
        })
        .then(ClientCommandManager.literal("clear").executes {
          heatmapData = null
          showHeatmap = false
          println("Heatmap cleared")
          1
        })
        .then(ClientCommandManager.literal("toggle").executes {
          showHeatmap = !showHeatmap
          println("Heatmap: $showHeatmap")
          1
        })
        .then(ClientCommandManager.literal("mode")
          .then(ClientCommandManager.literal("penalty").executes {
            heatmapMode = HeatmapMode.PENALTY
            println("Mode: PENALTY (total cost)")
            1
          })
          .then(ClientCommandManager.literal("edge").executes {
            heatmapMode = HeatmapMode.EDGE
            println("Mode: EDGE (drop distance)")
            1
          })
          .then(ClientCommandManager.literal("wall").executes {
            heatmapMode = HeatmapMode.WALL
            println("Mode: WALL (wall distance)")
            1
          })
          .then(ClientCommandManager.literal("corridor").executes {
            heatmapMode = HeatmapMode.CORRIDOR
            println("Mode: CORRIDOR (center offset)")
            1
          })
          .then(ClientCommandManager.literal("debug").executes {
            heatmapMode = HeatmapMode.DEBUG
            println("Mode: DEBUG (highlight anomalies)")
            1
          })
        )
        .then(ClientCommandManager.literal("inspect").executes {
          inspectPlayerPosition()
          1
        })
        .then(ClientCommandManager.literal("refresh").executes {
          heatmapData?.let {
            it.ctx.wallDistance.clearCache()
            generateHeatmap(it.radius)
          }
          1
        })

      dispatcher.register(heatmapCommand)
    }
  }

  private fun inspectPlayerPosition() {
    val pos = PlayerUtils.getBlockStandingOn() ?: return
    val ctx = CalculationContext()

    val x = pos.x
    var y = pos.y + 1
    val z = pos.z

    val standingOn = ctx.get(x, y - 1, z)
    if (standingOn?.block is CarpetBlock) {
      println("Standing on carpet, checking block below")
      y -= 1
    }

    val edgeDist = ctx.wallDistance.getEdgeDistance(x, y, z)
    val wallDist = ctx.wallDistance.getWallDistance(x, y, z)
    val (minWall, corridorWidth, centerOffset) = ctx.wallDistance.getCorridorInfo(x, y, z)
    val penalty = ctx.wallDistance.getPathPenalty(x, y, z)

    val dirDists = ctx.wallDistance.getDirectionalWallDistances(x, y, z)

    println("=== Position ($x, $y, $z) ===")
    println("Edge distance: $edgeDist")
    println("Min wall distance: $wallDist")
    println("Corridor width: $corridorWidth, center offset: $centerOffset")
    println("Wall distances: N=${dirDists[0]}, S=${dirDists[1]}, E=${dirDists[2]}, W=${dirDists[3]}")
    println("Total penalty: $penalty")

    println("--- Scanning for walls ---")
    for (dir in arrayOf(
      "North" to intArrayOf(0, -1),
      "South" to intArrayOf(0, 1),
      "East" to intArrayOf(1, 0),
      "West" to intArrayOf(-1, 0)
    )) {
      val (name, delta) = dir
      for (d in 1..8) {
        val nx = x + delta[0] * d
        val nz = z + delta[1] * d

        var foundWall = false
        for (checkY in (y - 1)..(y + 2)) {
          val state = ctx.get(nx, checkY, nz)
          if (state != null && ctx.wallDistance.isWallBlockDebug(nx, checkY, nz)) {
            println("$name @ $d (y=$checkY): WALL - ${state.block.name}")
            foundWall = true
            break
          }
        }
        if (foundWall) break
      }
    }
  }

  private fun generateHeatmap(radius: Int) {
    val playerPos = PlayerUtils.getBlockStandingOn() ?: return
    val ctx = CalculationContext()
    val data = HeatmapData(playerPos, radius, ctx)
    val startTime = System.currentTimeMillis()

    for (dx in -radius..radius) {
      for (dz in -radius..radius) {
        if (dx * dx + dz * dz > radius * radius) continue

        val x = playerPos.x + dx
        val z = playerPos.z + dz

        for (dy in 15 downTo -15) {
          var y = playerPos.y + dy

          if (MovementHelper.isSafe(ctx, x, y, z)) {
            val groundState = ctx.get(x, y - 1, z)
            val renderY = if (groundState?.block is CarpetBlock) {
              y
            } else {
              y
            }

            val edgeDist = ctx.wallDistance.getEdgeDistance(x, y, z)
            val wallDist = ctx.wallDistance.getWallDistance(x, y, z)
            val (_, corridorWidth, centerOffset) = ctx.wallDistance.getCorridorInfo(x, y, z)
            val penalty = ctx.wallDistance.getPathPenalty(x, y, z)
            val dirDists = ctx.wallDistance.getDirectionalWallDistances(x, y, z)

            data.points.add(
              HeatmapPoint(
                x, renderY, z, edgeDist, wallDist, corridorWidth, centerOffset, penalty,
                dirDists[0], dirDists[1], dirDists[2], dirDists[3]
              )
            )
            break
          }
        }
      }
    }

    heatmapData = data
    showHeatmap = true
    println("Heatmap: ${data.points.size} points in ${System.currentTimeMillis() - startTime}ms")

    val anomalies = data.points.filter { point ->
      point.penalty > 10 && point.edgeDist >= 3 && point.wallDist >= 2
    }

    if (anomalies.isNotEmpty()) {
      println("=== Found ${anomalies.size} anomalies ===")
      anomalies.take(5).forEach { p ->
        println("  (${p.x}, ${p.y}, ${p.z}): penalty=${p.penalty}, edge=${p.edgeDist}, wall=${p.wallDist}, corridor=${p.corridorWidth}, offset=${p.centerOffset}")
        println("    Wall dists: N=${p.wallDistNorth}, S=${p.wallDistSouth}, E=${p.wallDistEast}, W=${p.wallDistWest}")
      }
    }
  }

  private fun getColor(point: HeatmapPoint): Color {
    return when (heatmapMode) {
      HeatmapMode.PENALTY -> penaltyToColor(point.penalty)
      HeatmapMode.EDGE -> distanceToColor(point.edgeDist, 6)
      HeatmapMode.WALL -> distanceToColor(point.wallDist, 6)
      HeatmapMode.CORRIDOR -> corridorToColor(point.corridorWidth, point.centerOffset)
      HeatmapMode.DEBUG -> debugColor(point)
    }
  }

  private fun debugColor(point: HeatmapPoint): Color {
    if (point.penalty > 10 && point.edgeDist >= 3 && point.wallDist >= 2) {
      return Color(255, 0, 255, 200)
    }
    return penaltyToColor(point.penalty)
  }

  private fun penaltyToColor(penalty: Double): Color {
    return when {
      penalty <= 0.5 -> Color(0, 255, 0, 150)
      penalty <= 2.0 -> Color(100, 255, 0, 140)
      penalty <= 5.0 -> Color(180, 255, 0, 130)
      penalty <= 10.0 -> Color(255, 255, 0, 140)
      penalty <= 20.0 -> Color(255, 150, 0, 150)
      penalty <= 35.0 -> Color(255, 80, 0, 160)
      else -> Color(255, 0, 0, 180)
    }
  }

  private fun distanceToColor(dist: Int, max: Int): Color {
    val ratio = (dist.toFloat() / max).coerceIn(0f, 1f)
    val r = ((1 - ratio) * 255).toInt()
    val g = (ratio * 255).toInt()
    return Color(r, g, 0, 160)
  }

  private fun corridorToColor(corridorWidth: Int, centerOffset: Int): Color {
    if (corridorWidth == 0) {
      return Color(100, 100, 255, 120)
    }

    val halfWidth = corridorWidth / 2.0
    val offCenterRatio = if (halfWidth > 0) centerOffset / halfWidth else 0.0

    return when {
      offCenterRatio <= 0.3 -> Color(0, 255, 0, 160)
      offCenterRatio <= 0.5 -> Color(150, 255, 0, 150)
      offCenterRatio <= 0.7 -> Color(255, 255, 0, 150)
      offCenterRatio <= 0.85 -> Color(255, 150, 0, 160)
      else -> Color(255, 50, 0, 170)
    }
  }

  fun onRender(ctx: Context) {
    if (!showHeatmap) return
    val data = heatmapData ?: return

    for (point in data.points) {
      val box = Box(
        point.x + 0.1, point.y.toDouble(), point.z + 0.1,
        point.x + 0.9, point.y + 0.05, point.z + 0.9
      )
      ctx.drawBox(box, getColor(point), esp = true)
    }
  }
}
