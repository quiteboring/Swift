package dev.quiteboring.swift.util

import dev.quiteboring.swift.cache.CachedWorld
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus

class BlockStateAccessor(val world: World) {

  @JvmField var prevChunk: Chunk? = null
  @JvmField var prevChunkX = Int.MIN_VALUE
  @JvmField var prevChunkZ = Int.MIN_VALUE

  @JvmField val mutablePos: BlockPos.Mutable = BlockPos.Mutable()
  @JvmField val access: BlockViewWrapper = BlockViewWrapper(this)
  @JvmField val air: BlockState = Blocks.AIR.defaultState

  @JvmField val bottomY: Int = world.bottomY
  @JvmField val topY: Int = world.topYInclusive
  @JvmField val minSectionIndex: Int = world.bottomSectionCoord

  fun get(x: Int, y: Int, z: Int): BlockState {
    if (y !in bottomY..topY) return air

    val chunkX = x shr 4
    val chunkZ = z shr 4

    val chunk = prevChunk
    if (chunk != null && prevChunkX == chunkX && prevChunkZ == chunkZ) {
      return getFromChunkFast(chunk, x, y, z)
    }

    val loadedChunk = world.chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false)
    if (loadedChunk != null) {
      prevChunk = loadedChunk
      prevChunkX = chunkX
      prevChunkZ = chunkZ
      return getFromChunkFast(loadedChunk, x, y, z)
    }

    return CachedWorld.getBlockState(x, y, z) ?: air
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun getFromChunkFast(chunk: Chunk, x: Int, y: Int, z: Int): BlockState {
    val sectionIndex = (y shr 4) - minSectionIndex
    val sections = chunk.sectionArray

    if (sectionIndex < 0 || sectionIndex >= sections.size) return air

    val section = sections[sectionIndex]
    if (section.isEmpty) return air

    return section.getBlockState(x and 15, y and 15, z and 15)
  }

  fun invalidate() {
    prevChunk = null
    prevChunkX = Int.MIN_VALUE
    prevChunkZ = Int.MIN_VALUE
  }
}
