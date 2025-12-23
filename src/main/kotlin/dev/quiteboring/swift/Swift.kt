package dev.quiteboring.swift

import dev.quiteboring.swift.cache.CachedWorld
import dev.quiteboring.swift.command.HeatmapCommand
import dev.quiteboring.swift.command.PathCommand
import dev.quiteboring.swift.event.PacketEvent
import dev.quiteboring.swift.event.WorldRenderEvent
import dev.quiteboring.swift.util.setting.Settings
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object Swift : ClientModInitializer {

  private val settings = Settings()

  override fun onInitializeClient() {
    PathCommand.dispatch()
    HeatmapCommand.dispatch()

    WorldRenderEvent.LAST.register { ctx ->
      PathCommand.onRender(ctx)
      HeatmapCommand.onRender(ctx)
    }

    PacketEvent.RECEIVE.register { packet ->
      if (settings.useWorldCache) {
        CachedWorld.onPacketReceive(packet)
      }
    }

    ClientTickEvents.END_CLIENT_TICK.register { client ->
      if (client.world != null && settings.useWorldCache) {
        CachedWorld.processPendingChunks()
      }
    }
  }

  @JvmStatic
  fun getSettings(): Settings {
    return settings
  }

}
