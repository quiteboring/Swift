package dev.quiteboring.swift.command

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.quiteboring.swift.event.Context
import dev.quiteboring.swift.finder.calculate.Path
import dev.quiteboring.swift.finder.calculate.path.AStarPathfinder
import dev.quiteboring.swift.finder.goal.Goal
import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.finder.movement.Moves
import dev.quiteboring.swift.finder.movement.MovesFly
import dev.quiteboring.swift.util.PlayerUtils
import dev.quiteboring.swift.util.render.drawBox
import dev.quiteboring.swift.util.render.drawLine
import java.awt.Color
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object PathCommand {

  private var path: Path? = null

  fun dispatch() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      dispatcher.register(
        ClientCommandManager.literal("swiftpf")
          .then(
            ClientCommandManager.literal("clear").executes {
              path = null
              MinecraftClient.getInstance().inGameHud.chatHud
                .addMessage(Text.of("Rendering Cleared"))
              1
            }
          )
          .then(
            ClientCommandManager.argument("x", IntegerArgumentType.integer())
              .then(
                ClientCommandManager.argument("y", IntegerArgumentType.integer())
                  .then(
                    ClientCommandManager.argument("z", IntegerArgumentType.integer())
                      .executes { context ->
                        executePathfind(context, false)
                      }
                      .then(
                        ClientCommandManager.argument("fly", BoolArgumentType.bool())
                          .executes { context ->
                            val fly = BoolArgumentType.getBool(context, "fly")
                            executePathfind(context, fly)
                          }
                      )
                  )
              )
          )
      )
    }
  }

  private fun executePathfind(
    context: CommandContext<FabricClientCommandSource>,
    fly: Boolean,
  ): Int {
    val x = IntegerArgumentType.getInteger(context, "x")
    val y = IntegerArgumentType.getInteger(context, "y")
    val z = IntegerArgumentType.getInteger(context, "z")

    val mc = MinecraftClient.getInstance()
    val chat = mc.inGameHud.chatHud
    val standingOn = PlayerUtils.getBlockStandingOn()

    if (standingOn == null) {
      chat.addMessage(Text.of("§cCouldn't get player position"))
      return 0
    }

    return try {
      val ctx = CalculationContext()
      val moves = if (fly) MovesFly.entries else Moves.entries

      val result = AStarPathfinder(
        standingOn.x, standingOn.y, standingOn.z,
        Goal(x, y, z, ctx),
        ctx,
        moves = moves
      ).findPath()

      if (result != null) {
        path = result
        chat.addMessage(
          Text.of("§aPath found in ${result.timeTaken}ms (${result.points.size} nodes, fly=$fly)")
        )
        1
      } else {
        chat.addMessage(Text.of("§cNo path found"))
        0
      }
    } catch (e: Exception) {
      chat.addMessage(Text.of("§cError: ${e.message}"))
      e.printStackTrace()
      0
    }
  }


  fun onRender(ctx: Context) {
    val currentPath = path ?: return

    var prev: Vec3d? = null

    for (pos in currentPath.keyNodes) {
      val center = Vec3d(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)

      prev?.let { vec ->
        ctx.drawLine(vec, center, color = Color(255, 132, 94), thickness = 1F)
      }

      val box = Box(
        pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
        pos.x + 1.0, pos.y - 1.0, pos.z + 1.0
      )

      ctx.drawBox(box, color = Color(255, 132, 94))
      prev = center
    }
  }
}
