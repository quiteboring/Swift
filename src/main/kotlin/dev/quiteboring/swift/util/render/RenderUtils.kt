package dev.quiteboring.swift.util.render

import com.mojang.blaze3d.systems.RenderSystem
import dev.quiteboring.swift.event.Context
import java.awt.Color
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

fun Context.drawBox(box: Box, color: Color, esp: Boolean = false) {
  drawBoxes(listOf(box to color), esp)
}

fun Context.drawBoxes(boxes: List<Pair<Box, Color>>, esp: Boolean = false) {
  if (boxes.isEmpty()) return

  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return

  val fillLayer = if (esp) Layers.TRIANGLE_STRIP_ESP else Layers.TRIANGLE_STRIP
  val lineLayer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  matrix.push()
  with(camera.pos) { matrix.translate(-x, -y, -z) }

  for ((box, color) in boxes) {
    val r = color.red / 255f
    val g = color.green / 255f
    val b = color.blue / 255f
    val a = color.alpha / 255f

    VertexRendering.drawFilledBox(
      matrix,
      bufferSource.getBuffer(fillLayer),
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      r, g, b, a
    )

    VertexRendering.drawBox(
      matrix.peek(),
      bufferSource.getBuffer(lineLayer),
      box.minX, box.minY, box.minZ,
      box.maxX, box.maxY, box.maxZ,
      r, g, b, 1f
    )
  }

  matrix.pop()
  bufferSource.draw(fillLayer)
  bufferSource.draw(lineLayer)
}

fun Context.drawLine(
  start: Vec3d,
  end: Vec3d,
  color: Color,
  esp: Boolean = false,
  thickness: Float = 2f,
) {
  drawLines(listOf(Triple(start, end, color)), esp, thickness)
}

fun Context.drawLines(
  lines: List<Triple<Vec3d, Vec3d, Color>>,
  esp: Boolean = false,
  thickness: Float = 2f,
) {
  if (lines.isEmpty()) return

  val matrix = matrixStack ?: return
  val bufferSource = consumers as? VertexConsumerProvider.Immediate ?: return
  val layer = if (esp) Layers.LINE_LIST_ESP else Layers.LINE_LIST

  RenderSystem.lineWidth(thickness)
  matrix.push()
  with(camera.pos) { matrix.translate(-x, -y, -z) }

  for ((start, end, color) in lines) {
    val startOffset = Vector3f(start.x.toFloat(), start.y.toFloat(), start.z.toFloat())
    val direction = end.subtract(start)
    VertexRendering.drawVector(matrix, bufferSource.getBuffer(layer), startOffset, direction, color.rgb)
  }

  matrix.pop()
  bufferSource.draw(layer)
}
