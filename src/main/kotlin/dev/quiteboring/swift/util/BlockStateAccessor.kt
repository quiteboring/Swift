package dev.quiteboring.swift.util

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus

class BlockStateAccessor(val world: World) {

  var prev: Chunk? = null
  private val air = Blocks.AIR.defaultState

  private val minY = world.bottomY
  private val height = world.height
  private val maxY = minY + height

  fun get(x: Int, y: Int, z: Int): BlockState? {
    if (y < minY || y >= maxY) return air

    val cached: Chunk? = prev
    if (cached != null && cached.pos.x == (x shr 4) && cached.pos.z == (z shr 4)) {
      return getFromChunk(cached, x, y, z)
    }

    val chunk: Chunk? = world.chunkManager.getChunk(x shr 4, z shr 4, ChunkStatus.FULL, false)
    if (chunk != null) {
      prev = chunk
      return getFromChunk(chunk, x, y, z)
    }

    return null
  }

  private fun getFromChunk(chunk: Chunk, x: Int, y: Int, z: Int): BlockState {
    val sectionIndex = (y - minY) shr 4
    val sections = chunk.sectionArray

    if (sectionIndex < 0 || sectionIndex >= sections.size) {
      return air
    }

    val section = sections[sectionIndex]

    if (section == null || section.isEmpty) {
      return air
    }

    return section.getBlockState(x and 15, y and 15, z and 15)
  }
}
