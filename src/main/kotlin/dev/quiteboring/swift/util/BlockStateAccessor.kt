package dev.quiteboring.swift.util

import dev.quiteboring.swift.cache.CachedChunk
import dev.quiteboring.swift.cache.CachedWorld
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.chunk.ChunkStatus

class BlockStateAccessor(val world: World) {

  var prev: Chunk? = null
  var prevCached: CachedChunk? = null

  val mutablePos = BlockPos.Mutable()
  val access = BlockViewWrapper(this)
  val air: BlockState = Blocks.AIR.defaultState

  fun get(x: Int, y: Int, z: Int): BlockState {
    if (y !in 0 until world.dimension.height) {
      return air
    }

    if (DONT_USE_CACHE) {
      val cached: Chunk? = prev

      if (cached != null && cached.pos.x == (x shr 4) && cached.pos.z == (z shr 4)) {
        return getFromChunk(cached, x, y, z)
      }

      val chunk: Chunk? = world.chunkManager.getChunk(x shr 4, z shr 4, ChunkStatus.FULL, false)

      if (chunk != null && !chunk.sectionArray.all { it.isEmpty }) {
        prev = chunk
        return getFromChunk(chunk, x, y, z)
      }
    }

    var cached = prevCached

    if (cached == null || cached.x != x shr 9 || cached.z != z shr 9) {
      val region: CachedChunk = CachedWorld.getChunk(x shr 9, z shr 9) ?: return air

      prevCached = region
      cached = region
    }

    return cached.get(x and 511, y, z and 511) ?: return air
  }

  fun getFromChunk(chunk: Chunk, x: Int, y: Int, z: Int): BlockState {
    val section: ChunkSection = chunk.sectionArray[y shr 4]

    if (section.isEmpty) {
      return air
    }

    return section.getBlockState(x and 15, y and 15, z and 15)
  }

  companion object {
    const val DONT_USE_CACHE = false // Replace this with a setting
  }

}
