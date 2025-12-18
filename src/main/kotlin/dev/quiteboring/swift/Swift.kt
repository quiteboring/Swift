package dev.quiteboring.swift

import dev.quiteboring.swift.api.PathfinderServer
import dev.quiteboring.swift.command.HeatmapCommand
import dev.quiteboring.swift.command.PathCommand
import dev.quiteboring.swift.event.WorldRenderEvent
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents

class Swift : ClientModInitializer {

  override fun onInitializeClient() {
    PathCommand.dispatch()
    HeatmapCommand.dispatch()

    PathfinderServer.start()

    ClientLifecycleEvents.CLIENT_STOPPING.register {
      PathfinderServer.stop()
    }

    WorldRenderEvent.LAST.register { context ->
      PathCommand.onRender(context)
      HeatmapCommand.onRender(context)
    }
  }
}
