package dev.quiteboring.swift.movement

import dev.quiteboring.swift.calc.PrecomputedData
import dev.quiteboring.swift.costs.ActionCosts
import dev.quiteboring.swift.util.BlockStateAccessor
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffects

class CalculationContext {

  val mc: MinecraftClient = MinecraftClient.getInstance()
  val world = mc.world
  val player = mc.player

  val bsa = BlockStateAccessor(mc.world!!)
  val jumpBoostAmplifier = player?.getStatusEffect(StatusEffects.JUMP_BOOST)?.amplifier ?: -1
  val cost = ActionCosts(jumpBoostAmplifier = jumpBoostAmplifier)

  val maxFallHeight = 10

  val precomputed = PrecomputedData()

  fun get(x: Int, y: Int, z: Int): BlockState? {
    return bsa.get(x, y, z)
  }

  fun getFlags(x: Int, y: Int, z: Int): Int {
    val state = get(x, y, z) ?: return PrecomputedData.MASK_SOLID
    return precomputed.get(state)
  }

}
