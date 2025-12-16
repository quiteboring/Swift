package dev.quiteboring.swift.util.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.quiteboring.swift.event.Context
import java.awt.Color
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

object RenderConfig {
  var maxRenderDistance = 256.0
  var maxRenderDistanceSq = maxRenderDistance * maxRenderDistance
  var frustumCulling = true
  var distanceCulling = true

  fun setMaxDistance(distance: Double) {
    maxRenderDistance = distance
    maxRenderDistanceSq = distance * distance
  }
}

private val tempVector = Vector3f()

private inline fun Box.isWithinDistance(cameraPos: Vec3d, maxDistSq: Double): Boolean {
  val centerX = (minX + maxX) * 0.5
  val centerY = (minY + maxY) * 0.5
  val centerZ = (minZ + maxZ) * 0.5
  return cameraPos.squaredDistanceTo(centerX, centerY, centerZ) <= maxDistSq
}

private inline fun shouldRenderBox(
  box: Box,
  cameraPos: Vec3d,
  frustum: Frustum?,
  esp: Boolean
): Boolean {
  if (RenderConfig.distanceCulling && !box.isWithinDistance(cameraPos, RenderConfig.maxRenderDistanceSq)) {
    return false
  }
  if (!esp && RenderConfig.frustumCulling && frustum != null && !frustum.isVisible(box)) {
    return false
  }
  return true
}

fun Context.drawBox(box: Box, color: Color, esp: Boolean = false) {
  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return
  val cameraPos = camera.pos

  if (!shouldRenderBox(box, cameraPos, frustum, esp)) return

  val fillLayer = if (esp) Layers.TRIANGLE_STRIP_ESP else Layers.TRIANGLE_STRIP
  val lineLayer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  val r = color.red * 0.00392157f // 1/255
  val g = color.green * 0.00392157f
  val b = color.blue * 0.00392157f
  val a = color.alpha * 0.00392157f

  matrix.push()
  matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

  VertexRendering.drawFilledBox(
    matrix,
    bufferSource.getBuffer(fillLayer),
    box.minX, box.minY, box.minZ,
    box.maxX, box.maxY, box.maxZ,
    r, g, b, a
  )
  bufferSource.draw(fillLayer)

  VertexRendering.drawBox(
    matrix.peek(),
    bufferSource.getBuffer(lineLayer),
    box.minX, box.minY, box.minZ,
    box.maxX, box.maxY, box.maxZ,
    r, g, b, 1f
  )
  bufferSource.draw(lineLayer)

  matrix.pop()
}

fun Context.drawBoxes(boxes: List<Pair<Box, Color>>, esp: Boolean = false) {
  if (boxes.isEmpty()) return

  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return
  val cameraPos = camera.pos
  val frustum = this.frustum

  val fillLayer = if (esp) Layers.TRIANGLE_STRIP_ESP else Layers.TRIANGLE_STRIP
  val lineLayer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  matrix.push()
  matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

  var fillBuffer = bufferSource.getBuffer(fillLayer)
  var hasContent = false

  for (i in boxes.indices) {
    val (box, color) = boxes[i]
    if (!shouldRenderBox(box, cameraPos, frustum, esp)) continue

    val r = color.red * 0.00392157f
    val g = color.green * 0.00392157f
    val b = color.blue * 0.00392157f
    val a = color.alpha * 0.00392157f

    VertexRendering.drawFilledBox(
      matrix, fillBuffer,
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      r, g, b, a
    )
    hasContent = true
  }

  if (hasContent) {
    bufferSource.draw(fillLayer)
  }

  val lineBuffer = bufferSource.getBuffer(lineLayer)
  hasContent = false

  for (i in boxes.indices) {
    val (box, color) = boxes[i]
    if (!shouldRenderBox(box, cameraPos, frustum, esp)) continue

    val r = color.red * 0.00392157f
    val g = color.green * 0.00392157f
    val b = color.blue * 0.00392157f

    VertexRendering.drawBox(
      matrix.peek(), lineBuffer,
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      r, g, b, 1f
    )
    hasContent = true
  }

  if (hasContent) {
    bufferSource.draw(lineLayer)
  }

  matrix.pop()
}

fun Context.drawBoxesCached(boxes: List<Pair<Box, Color>>, esp: Boolean = false) {
  if (boxes.isEmpty()) return

  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return
  val cameraPos = camera.pos
  val frustum = this.frustum

  val fillLayer = if (esp) Layers.TRIANGLE_STRIP_ESP else Layers.TRIANGLE_STRIP
  val lineLayer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  val visible = boxes.filterTo(ArrayList(boxes.size)) { (box, _) ->
    shouldRenderBox(box, cameraPos, frustum, esp)
  }

  if (visible.isEmpty()) return

  matrix.push()
  matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

  val fillBuffer = bufferSource.getBuffer(fillLayer)
  for (i in visible.indices) {
    val (box, color) = visible[i]
    VertexRendering.drawFilledBox(
      matrix, fillBuffer,
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      color.red * 0.00392157f,
      color.green * 0.00392157f,
      color.blue * 0.00392157f,
      color.alpha * 0.00392157f
    )
  }
  bufferSource.draw(fillLayer)

  val lineBuffer = bufferSource.getBuffer(lineLayer)
  for (i in visible.indices) {
    val (box, color) = visible[i]
    VertexRendering.drawBox(
      matrix.peek(), lineBuffer,
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      color.red * 0.00392157f,
      color.green * 0.00392157f,
      color.blue * 0.00392157f,
      1f
    )
  }
  bufferSource.draw(lineLayer)

  matrix.pop()
}

private inline fun shouldRenderLine(
  start: Vec3d,
  end: Vec3d,
  cameraPos: Vec3d,
  frustum: Frustum?,
  esp: Boolean
): Boolean {
  if (RenderConfig.distanceCulling) {
    val maxDistSq = RenderConfig.maxRenderDistanceSq
    val startDistSq = cameraPos.squaredDistanceTo(start)
    val endDistSq = cameraPos.squaredDistanceTo(end)
    if (startDistSq > maxDistSq && endDistSq > maxDistSq) {
      val midX = (start.x + end.x) * 0.5
      val midY = (start.y + end.y) * 0.5
      val midZ = (start.z + end.z) * 0.5
      if (cameraPos.squaredDistanceTo(midX, midY, midZ) > maxDistSq) {
        return false
      }
    }
  }

  if (!esp && RenderConfig.frustumCulling && frustum != null) {
    val lineBox = Box(
      minOf(start.x, end.x), minOf(start.y, end.y), minOf(start.z, end.z),
      maxOf(start.x, end.x), maxOf(start.y, end.y), maxOf(start.z, end.z)
    )
    if (!frustum.isVisible(lineBox)) return false
  }

  return true
}

fun Context.drawLine(
  start: Vec3d,
  end: Vec3d,
  color: Color,
  esp: Boolean = false,
  thickness: Float = 2f,
) {
  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return
  val cameraPos = camera.pos

  if (!shouldRenderLine(start, end, cameraPos, frustum, esp)) return

  val layer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  RenderSystem.lineWidth(thickness)
  matrix.push()
  matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

  tempVector.set(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
  VertexRendering.drawVector(
    matrix,
    bufferSource.getBuffer(layer),
    tempVector,
    end.subtract(start),
    color.rgb
  )

  matrix.pop()
  bufferSource.draw(layer)
}

fun Context.drawLines(
  lines: List<Triple<Vec3d, Vec3d, Color>>,
  esp: Boolean = false,
  thickness: Float = 2f,
) {
  if (lines.isEmpty()) return

  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return
  val cameraPos = camera.pos
  val frustum = this.frustum
  val layer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  RenderSystem.lineWidth(thickness)
  matrix.push()
  matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

  val buffer = bufferSource.getBuffer(layer)
  var hasContent = false

  for (i in lines.indices) {
    val (start, end, color) = lines[i]
    if (!shouldRenderLine(start, end, cameraPos, frustum, esp)) continue

    tempVector.set(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
    VertexRendering.drawVector(matrix, buffer, tempVector, end.subtract(start), color.rgb)
    hasContent = true
  }

  matrix.pop()

  if (hasContent) {
    bufferSource.draw(layer)
  }
}
