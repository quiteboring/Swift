package dev.quiteboring.swift.cache

import net.minecraft.network.packet.Packet

object CachedWorld {

  // TODO: Complete Implementation
  fun getChunk(x: Int, z: Int): CachedChunk? {
    return null
  }

  // TODO: Use ChunkDeltaUpdateS2CPacket and BlockUpdateS2CPacket to cache block states
  fun onPacketReceive(packet: Packet<*>) {

  }

}
