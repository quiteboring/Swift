package dev.quiteboring.swift.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import dev.quiteboring.swift.calc.Path
import dev.quiteboring.swift.calc.path.AStarPathfinder
import dev.quiteboring.swift.event.Context
import dev.quiteboring.swift.goal.Goal
import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.util.PlayerUtils
import dev.quiteboring.swift.util.render.drawBoxes
import dev.quiteboring.swift.util.render.drawLines
import java.awt.Color
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object PathCommand {
  private var path: Path? = null

  private var cachedBoxes: List<Pair<Box, Color>>? = null
  private var cachedLines: List<Triple<Vec3d, Vec3d, Color>>? = null
  private var cachedPathHash: Int = 0

  private const val PATH_ALPHA = 100
  private val BOX_COLOR = Color(0, 255, 255, PATH_ALPHA)
  private val LINE_COLOR = Color(255, 0, 255, PATH_ALPHA)

  fun dispatch() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      val xArg = ClientCommandManager.argument("x", IntegerArgumentType.integer())
      val yArg = ClientCommandManager.argument("y", IntegerArgumentType.integer())
      val zArg = ClientCommandManager.argument("z", IntegerArgumentType.integer())

      val pathfindCommand = ClientCommandManager.literal("pathfind")
        .then(ClientCommandManager.literal("clear").executes {
          clearPath()
          println("Rendering cleared!")
          1
        })
        .then(xArg.then(yArg.then(zArg.executes { context ->
          val x = IntegerArgumentType.getInteger(context, "x")
          val y = IntegerArgumentType.getInteger(context, "y")
          val z = IntegerArgumentType.getInteger(context, "z")

          val player = PlayerUtils.getBlockStandingOn() ?: return@executes 1
          val ctx = CalculationContext()

          var goalY = y
          val goalPos = BlockPos(x, y, z)
          val goalState = ctx.get(x, y, z)
          if (goalState != null && !goalState.getCollisionShape(ctx.world, goalPos).isEmpty) {
            goalY++
          }

          val goal = Goal(x, goalY, z, ctx)

          val startX = player.x
          val startY = player.y + 1
          val startZ = player.z

          val newPath = AStarPathfinder(startX, startY, startZ, goal, ctx).findPath()
          setPath(newPath)

          newPath?.let { println("${it.timeTaken} ms") }
          return@executes 1
        })))

      dispatcher.register(pathfindCommand)
    }
  }

  private fun setPath(newPath: Path?) {
    path = newPath
    invalidateCache()
  }

  private fun clearPath() {
    path = null
    invalidateCache()
  }

  private fun invalidateCache() {
    cachedBoxes = null
    cachedLines = null
    cachedPathHash = 0
  }

  private fun buildCache(pathPoints: List<BlockPos>): Pair<List<Pair<Box, Color>>, List<Triple<Vec3d, Vec3d, Color>>> {
    val boxes = ArrayList<Pair<Box, Color>>(pathPoints.size)
    val lines = ArrayList<Triple<Vec3d, Vec3d, Color>>(pathPoints.size - 1)

    var previous: BlockPos? = null

    for (pos in pathPoints) {
      val box = Box(
        pos.x.toDouble(),
        pos.y.toDouble(),
        pos.z.toDouble(),
        pos.x.toDouble() + 1.0,
        pos.y.toDouble() + 1.0,
        pos.z.toDouble() + 1.0
      )
      boxes.add(box to BOX_COLOR)

      previous?.let { prev ->
        lines.add(
          Triple(
            Vec3d(prev.x + 0.5, prev.y + 0.5, prev.z + 0.5),
            Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
            LINE_COLOR
          )
        )
      }
      previous = pos
    }

    return boxes to lines
  }

  fun onRender(ctx: Context) {
    val currentPath = path ?: return
    val pathPoints = currentPath.points
    val pathHash = System.identityHashCode(currentPath)

    if (cachedBoxes == null || cachedPathHash != pathHash) {
      val (boxes, lines) = buildCache(pathPoints)
      cachedBoxes = boxes
      cachedLines = lines
      cachedPathHash = pathHash
    }

    cachedBoxes?.let { ctx.drawBoxes(it, esp = false) }
    cachedLines?.let { ctx.drawLines(it, esp = false, thickness = 2f) }
  }
}
