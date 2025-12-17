package dev.quiteboring.swift

import dev.quiteboring.swift.command.PathCommand
import dev.quiteboring.swift.event.WorldRenderEvent
import net.fabricmc.api.ClientModInitializer

class Swift : ClientModInitializer {

  override fun onInitializeClient() {
    PathCommand.dispatch()

    WorldRenderEvent.LAST.register { context ->
      PathCommand.onRender(context)
    }
  }
}
