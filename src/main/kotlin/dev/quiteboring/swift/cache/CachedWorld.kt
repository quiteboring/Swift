package dev.quiteboring.swift.cache

import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket

object CachedWorld {

  fun onPacketReceived(packet: Packet<*>) {
    if (packet is ChunkDeltaUpdateS2CPacket) {

    } else if (packet is BlockUpdateS2CPacket) {

    }
  }

}
