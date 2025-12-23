package dev.quiteboring.swift

import dev.quiteboring.swift.cache.CachedWorld
import dev.quiteboring.swift.command.HeatmapCommand
import dev.quiteboring.swift.command.PathCommand
import dev.quiteboring.swift.event.PacketEvent
import dev.quiteboring.swift.event.WorldRenderEvent
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class Swift : ClientModInitializer {

  override fun onInitializeClient() {
    PathCommand.dispatch()
    HeatmapCommand.dispatch()

    WorldRenderEvent.LAST.register { context ->
      PathCommand.onRender(context)
      HeatmapCommand.onRender(context)
    }

    PacketEvent.RECEIVE.register { packet ->
      CachedWorld.onPacketReceive(packet)
    }

    ClientTickEvents.END_CLIENT_TICK.register { client ->
      if (client.world != null) {
        CachedWorld.processPendingChunks()
      }
    }
  }
}
