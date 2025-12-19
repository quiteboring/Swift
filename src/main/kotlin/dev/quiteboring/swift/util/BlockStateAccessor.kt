package dev.quiteboring.swift.util

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus

class BlockStateAccessor(val world: World) {

  private var prevChunk: Chunk? = null
  private var prevChunkX = Int.MIN_VALUE
  private var prevChunkZ = Int.MIN_VALUE

  private val bottomY = world.bottomY

  val mutablePos: BlockPos.Mutable = BlockPos.Mutable()

  private val air: BlockState = Blocks.AIR.defaultState

  fun get(x: Int, y: Int, z: Int): BlockState? {
    val chunkX = x shr 4
    val chunkZ = z shr 4

    val chunk: Chunk?
    if (prevChunkX == chunkX && prevChunkZ == chunkZ) {
      chunk = prevChunk
    } else {
      chunk = world.chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false)
      if (chunk != null) {
        prevChunk = chunk
        prevChunkX = chunkX
        prevChunkZ = chunkZ
      }
    }

    return chunk?.let { getFromChunk(it, x, y, z) }
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
