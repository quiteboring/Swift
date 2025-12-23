package dev.quiteboring.swift.finder.movement

import dev.quiteboring.swift.finder.costs.ActionCosts
import dev.quiteboring.swift.finder.helper.BlockStateAccessor
import dev.quiteboring.swift.finder.precompute.PrecomputedData
import dev.quiteboring.swift.finder.precompute.WallDistanceCalculator
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffects

class CalculationContext {

  @JvmField val mc: MinecraftClient = MinecraftClient.getInstance()
  @JvmField val world = mc.world!!
  @JvmField val player = mc.player

  @JvmField val bsa = BlockStateAccessor(world)
  @JvmField val jumpBoostAmplifier = player?.getStatusEffect(StatusEffects.JUMP_BOOST)?.amplifier ?: -1
  @JvmField val cost = ActionCosts(jumpBoostAmplifier = jumpBoostAmplifier)
  @JvmField val maxFallHeight = 20

  @JvmField val wdc = WallDistanceCalculator(this)
  @JvmField val precomputedData = PrecomputedData(bsa)

  fun get(x: Int, y: Int, z: Int): BlockState {
    return bsa.get(x, y, z)
  }
}
