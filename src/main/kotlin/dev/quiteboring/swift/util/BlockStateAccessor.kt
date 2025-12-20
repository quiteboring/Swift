package dev.quiteboring.swift.util

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus

class BlockStateAccessor(val world: World) {

  val mutablePos: BlockPos.Mutable = BlockPos.Mutable()

  private var prevChunk: Chunk? = null

  private val bottomY = world.bottomY
  private val air: BlockState = Blocks.AIR.defaultState

  fun get(x: Int, y: Int, z: Int): BlockState? {
    val cached = prevChunk

    if (cached != null && cached.getPos().x == x shr 4 && cached.getPos().z == z shr 4) {
      return getFromChunk(cached, x, y, z)
    }

    val chunk = world.chunkManager.getChunk(x shr 4, z shr 4, ChunkStatus.FULL, false)

    if (chunk != null && !chunk.sectionArray.all { it.isEmpty }) {
      prevChunk = chunk
      return getFromChunk(chunk, x, y, z)
    }

    return null
  }

  fun isChunkLoaded(blockX: Int, blockZ: Int): Boolean {
    return world.chunkManager.isChunkLoaded(blockX shr 4, blockZ shr 4)
  }

  fun getFromChunk(chunk: Chunk, x: Int, y: Int, z: Int): BlockState {
    val sectionIndex = (y - bottomY) shr 4

    if (sectionIndex < 0 || sectionIndex >= chunk.sectionArray.size) {
      return air
    }

    val section = chunk.sectionArray[sectionIndex]

    if (section.isEmpty) {
      return air
    }

    return section.getBlockState(x and 15, y and 15, z and 15)
  }

}
