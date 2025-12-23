package dev.quiteboring.swift.cache

import net.minecraft.block.BlockState
import java.util.concurrent.atomic.AtomicLong

class CachedChunk(
  val chunkX: Int,
  val chunkZ: Int,
  private val minY: Int,
  private val maxY: Int
) {

  @Volatile
  private var sections: Array<CachedSection?> = arrayOfNulls((maxY - minY + 15) shr 4)

  private val _lastAccessTime = AtomicLong(System.currentTimeMillis())
  val lastAccessTime: Long get() = _lastAccessTime.get()

  fun get(localX: Int, y: Int, localZ: Int): BlockState? {
    if (y < minY || y >= maxY) return null

    _lastAccessTime.lazySet(System.currentTimeMillis())

    val sectionIndex = (y - minY) shr 4
    if (sectionIndex < 0 || sectionIndex >= sections.size) return null

    return sections[sectionIndex]?.get(localX and 15, y and 15, localZ and 15)
  }

  fun set(localX: Int, y: Int, localZ: Int, state: BlockState) {
    if (y < minY || y >= maxY) return

    _lastAccessTime.lazySet(System.currentTimeMillis())

    val sectionIndex = (y - minY) shr 4
    if (sectionIndex < 0 || sectionIndex >= sections.size) return

    val section = sections[sectionIndex] ?: CachedSection().also { sections[sectionIndex] = it }
    section.set(localX and 15, y and 15, localZ and 15, state)
  }

  fun setSection(sectionIndex: Int, section: CachedSection) {
    if (sectionIndex in sections.indices) {
      sections[sectionIndex] = section
    }
  }
}

class CachedSection {
  @Volatile
  private var blocks: Array<BlockState?> = arrayOfNulls(4096)

  @Suppress("NOTHING_TO_INLINE")
  private inline fun index(x: Int, y: Int, z: Int): Int = (y shl 8) or (z shl 4) or x

  fun get(x: Int, y: Int, z: Int): BlockState? = blocks[index(x, y, z)]

  fun set(x: Int, y: Int, z: Int, state: BlockState) {
    blocks[index(x, y, z)] = state
  }
}
