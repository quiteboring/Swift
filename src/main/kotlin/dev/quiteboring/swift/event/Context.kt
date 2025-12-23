package dev.quiteboring.swift.event

import net.minecraft.client.render.Camera
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

class Context {
  var matrixStack: MatrixStack? = null
    private set

  lateinit var consumers: VertexConsumerProvider
    private set

  lateinit var camera: Camera
    private set

  lateinit var frustum: Frustum
    private set

  fun setMatrixStack(stack: MatrixStack) {
    this.matrixStack = stack
  }

  fun setConsumers(consumers: VertexConsumerProvider) {
    this.consumers = consumers
  }

  fun setCamera(camera: Camera) {
    this.camera = camera
  }

  fun setFrustum(frustum: Frustum) {
    this.frustum = frustum
  }
}
