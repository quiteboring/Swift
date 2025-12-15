package dev.quiteboring.swift.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import dev.quiteboring.swift.calc.Path
import dev.quiteboring.swift.calc.path.AStarPathfinder
import dev.quiteboring.swift.event.Context
import dev.quiteboring.swift.goal.Goal
import dev.quiteboring.swift.movement.CalculationContext
import dev.quiteboring.swift.util.PlayerUtils
import dev.quiteboring.swift.util.render.drawBox
import dev.quiteboring.swift.util.render.drawLine
import java.awt.Color
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object TestCommand {

  var path: Path? = null

  fun dispatch() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      val xArg = ClientCommandManager.argument("x", IntegerArgumentType.integer())
      val yArg = ClientCommandManager.argument("y", IntegerArgumentType.integer())
      val zArg = ClientCommandManager.argument("z", IntegerArgumentType.integer())

      val pathfindCommand = ClientCommandManager.literal("pathfind")
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

          this.path = AStarPathfinder(
            startX, startY, startZ, goal, ctx
          ).findPath()

          this.path?.let { println("${it.timeTaken} ms") }
          return@executes 1
        })))

      dispatcher.register(pathfindCommand)
    }
  }

  fun onRender(ctx: Context) {
    val pathPoints = path?.points ?: return
    var previous: BlockPos? = null

    pathPoints.forEach { pos ->
      val box = Box(
        pos.x.toDouble(),
        pos.y.toDouble(),
        pos.z.toDouble(),
        pos.x.toDouble() + 1.0,
        pos.y.toDouble() + 1.0,
        pos.z.toDouble() + 1.0
      )

      ctx.drawBox(box, Color.CYAN, esp = true)

      previous?.let { prev ->
        ctx.drawLine(
          Vec3d(prev.x + 0.5, prev.y + 0.5, prev.z + 0.5),
          Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5),
          Color.MAGENTA,
          esp = true,
          thickness = 2f
        )
      }

      previous = pos
    }
  }

}
