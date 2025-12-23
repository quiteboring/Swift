package dev.quiteboring.swift.finder.calculate.openset

import dev.quiteboring.swift.finder.calculate.PathNode

class BinaryHeapOpenSet(initialSize: Int = 2048) {

  private var items = arrayOfNulls<PathNode>(initialSize)

  @JvmField
  var size = 0

  fun add(node: PathNode) {
    if (size >= items.size - 1) {
      items = items.copyOf(items.size * 2)
    }

    size++
    node.heapPosition = size
    items[size] = node
    siftUp(size)
  }

  fun relocate(node: PathNode) {
    siftUp(node.heapPosition)
  }

  private fun siftUp(startPos: Int) {
    var pos = startPos
    val node = items[pos]!!
    val cost = node.fCost

    while (pos > 1) {
      val parentPos = pos ushr 1
      val parent = items[parentPos]!!

      if (cost >= parent.fCost) break

      items[pos] = parent
      parent.heapPosition = pos
      pos = parentPos
    }

    items[pos] = node
    node.heapPosition = pos
  }

  fun poll(): PathNode {
    val result = items[1]!!
    result.heapPosition = -1

    if (size == 1) {
      items[1] = null
      size = 0
      return result
    }

    val last = items[size]!!
    items[size] = null
    size--
    items[1] = last
    last.heapPosition = 1
    siftDown()

    return result
  }

  private fun siftDown() {
    var pos = 1
    val node = items[1]!!
    val cost = node.fCost
    val halfSize = size ushr 1

    while (pos <= halfSize) {
      var childPos = pos shl 1
      var child = items[childPos]!!

      val rightPos = childPos + 1
      if (rightPos <= size) {
        val right = items[rightPos]!!
        if (right.fCost < child.fCost) {
          childPos = rightPos
          child = right
        }
      }

      if (cost <= child.fCost) break

      items[pos] = child
      child.heapPosition = pos
      pos = childPos
    }

    items[pos] = node
    node.heapPosition = pos
  }

  @Suppress("NOTHING_TO_INLINE")
  inline fun isEmpty() = size == 0

  @Suppress("NOTHING_TO_INLINE")
  inline fun isNotEmpty() = size > 0

  fun clear() {
    for (i in 1..size) {
      items[i]?.heapPosition = -1
      items[i] = null
    }
    size = 0
  }
}
