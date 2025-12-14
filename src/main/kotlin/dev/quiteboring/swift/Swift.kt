package dev.quiteboring.swift

import dev.quiteboring.swift.command.TestCommand
import dev.quiteboring.swift.event.WorldRenderEvent
import net.fabricmc.api.ClientModInitializer

class Swift : ClientModInitializer{

  override fun onInitializeClient() {
    TestCommand.dispatch()

    WorldRenderEvent.LAST.register { context ->
      TestCommand.onRender(context)
    }
  }

}
