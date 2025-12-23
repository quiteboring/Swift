package dev.quiteboring.swift.cache

import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket
import net.minecraft.world.chunk.ChunkStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object CachedWorld {

  private val chunks = ConcurrentHashMap<Long, CachedChunk>()
  private val pendingChunks = ConcurrentLinkedQueue<Pair<Int, Int>>()

  private const val MAX_CACHED_CHUNKS = 1024
  private const val CHUNKS_PER_TICK = 4

  @Suppress("NOTHING_TO_INLINE")
  private inline fun chunkKey(x: Int, z: Int): Long =
    (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)

  fun getBlockState(x: Int, y: Int, z: Int): BlockState? {
    val chunk = chunks[chunkKey(x shr 4, z shr 4)] ?: return null
    return chunk.get(x and 15, y, z and 15)
  }

  fun onPacketReceive(packet: Packet<*>) {
    when (packet) {
      is ChunkDataS2CPacket -> pendingChunks.add(packet.chunkX to packet.chunkZ)
      is BlockUpdateS2CPacket -> handleBlockUpdate(packet)
      is ChunkDeltaUpdateS2CPacket -> handleChunkDelta(packet)
      is UnloadChunkS2CPacket -> { /* Keep cached */ }
    }
  }

  fun processPendingChunks() {
    val mc = MinecraftClient.getInstance()
    val world = mc.world ?: return

    val minY = world.bottomY
    val maxY = world.topYInclusive + 1

    repeat(CHUNKS_PER_TICK) {
      val (chunkX, chunkZ) = pendingChunks.poll() ?: return

      val worldChunk = world.chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false)
      if (worldChunk == null) {
        pendingChunks.add(chunkX to chunkZ)
        return@repeat
      }

      val cached = CachedChunk(chunkX, chunkZ, minY, maxY)

      for ((sectionIndex, section) in worldChunk.sectionArray.withIndex()) {
        if (section.isEmpty) continue

        val cachedSection = CachedSection()
        var hasBlocks = false

        for (localY in 0..15) {
          for (localZ in 0..15) {
            for (localX in 0..15) {
              val state = section.getBlockState(localX, localY, localZ)
              if (!state.isAir) {
                cachedSection.set(localX, localY, localZ, state)
                hasBlocks = true
              }
            }
          }
        }

        if (hasBlocks) {
          cached.setSection(sectionIndex, cachedSection)
        }
      }

      chunks[chunkKey(chunkX, chunkZ)] = cached
      cleanupIfNeeded()
    }
  }

  private fun handleBlockUpdate(packet: BlockUpdateS2CPacket) {
    val pos = packet.pos
    chunks[chunkKey(pos.x shr 4, pos.z shr 4)]?.set(
      pos.x and 15, pos.y, pos.z and 15, packet.state
    )
  }

  private fun handleChunkDelta(packet: ChunkDeltaUpdateS2CPacket) {
    packet.visitUpdates { pos, state ->
      chunks[chunkKey(pos.x shr 4, pos.z shr 4)]?.set(
        pos.x and 15, pos.y, pos.z and 15, state
      )
    }
  }

  private fun cleanupIfNeeded() {
    if (chunks.size < MAX_CACHED_CHUNKS + 256) return

    chunks.entries
      .sortedBy { it.value.lastAccessTime }
      .take(chunks.size - MAX_CACHED_CHUNKS)
      .forEach { chunks.remove(it.key) }
  }

  fun clear() {
    chunks.clear()
    pendingChunks.clear()
  }
}
