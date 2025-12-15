package dev.quiteboring.swift.util

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.chunk.ChunkStatus

class BlockStateAccessor(
  val world: World,
) {

  var prev: Chunk? = null
  val isPassable = BlockPos.Mutable()

  private val air = Blocks.AIR.defaultState

  fun get(x: Int, y: Int, z: Int): BlockState? {
    val cached: Chunk? = prev

    if (cached != null && cached.pos.x == (x shr 4) && cached.pos.z == (z shr 4)) {
      return getFromChunk(cached, x, y, z)
    }

    val chunk: Chunk? = world.chunkManager.getChunk(x shr 4, z shr 4, ChunkStatus.FULL, false)

    if (chunk != null && !chunk.sectionArray.all { it.isEmpty }) {
      prev = chunk
      return getFromChunk(chunk, x, y, z)
    }

    return null
  }

  fun getFromChunk(chunk: Chunk, x: Int, y: Int, z: Int): BlockState {
    return chunk.getBlockState(BlockPos(x, y, z))
  }

}
