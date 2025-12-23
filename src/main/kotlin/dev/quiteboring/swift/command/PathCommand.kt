package dev.quiteboring.swift.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import dev.quiteboring.swift.event.Context
import dev.quiteboring.swift.finder.calculate.Path
import dev.quiteboring.swift.finder.calculate.path.AStarPathfinder
import dev.quiteboring.swift.finder.goal.Goal
import dev.quiteboring.swift.finder.movement.CalculationContext
import dev.quiteboring.swift.util.PlayerUtils
import dev.quiteboring.swift.util.render.drawBox
import dev.quiteboring.swift.util.render.drawLine
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color

object PathCommand {

  private var path: Path? = null

  fun dispatch() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      dispatcher.register(
        ClientCommandManager.literal("swiftpf")
          .then(ClientCommandManager.literal("clear").executes {
            path = null
            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.of("Rendering Cleared"))
            1
          })
          .then(
            ClientCommandManager.argument("x", IntegerArgumentType.integer())
              .then(
                ClientCommandManager.argument("y", IntegerArgumentType.integer())
                  .then(
                    ClientCommandManager.argument("z", IntegerArgumentType.integer())
                      .executes { context ->
                        val x = IntegerArgumentType.getInteger(context, "x")
                        val y = IntegerArgumentType.getInteger(context, "y")
                        val z = IntegerArgumentType.getInteger(context, "z")

                        val standingOn = PlayerUtils.getBlockStandingOn()
                        if (standingOn == null) {
                          MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
                            Text.of("§cCouldn't get player position")
                          )
                          return@executes 1
                        }

                        val mc = MinecraftClient.getInstance()
                        val chat = mc.inGameHud.chatHud

                        try {
                          val ctx = CalculationContext()
                          val result = AStarPathfinder(
                            standingOn.x, standingOn.y, standingOn.z,
                            Goal(x, y, z, ctx), ctx
                          ).findPath()

                          if (result != null) {
                            path = result
                            chat.addMessage(Text.of("§aPath found in ${result.timeTaken}ms (${result.points.size} nodes)"))
                          } else {
                            chat.addMessage(Text.of("§cNo path found"))
                          }
                        } catch (e: Exception) {
                          chat.addMessage(Text.of("§cError: ${e.message}"))
                          e.printStackTrace()
                        }

                        1
                      }
                  )
              )
          )
      )
    }
  }

  fun onRender(ctx: Context) {
    val currentPath = path ?: return

    var prev: Vec3d? = null

    for (pos in currentPath.points) {
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
