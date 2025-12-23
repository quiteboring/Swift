package dev.quiteboring.swift.cache

object CachedWorld {

  fun getRegion(x: Int, z: Int): CachedRegion? {
    return CachedRegion(x, z)
  }

}
