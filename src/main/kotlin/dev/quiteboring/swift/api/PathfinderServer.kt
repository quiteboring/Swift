package dev.quiteboring.swift.api

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import dev.quiteboring.swift.calc.KeyNodeExtractor
import dev.quiteboring.swift.calc.path.AStarPathfinder
import dev.quiteboring.swift.goal.Goal
import dev.quiteboring.swift.movement.CalculationContext
import net.minecraft.client.MinecraftClient
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object PathfinderServer {

  private var server: HttpServer? = null
  private val gson = Gson()
  private val executor = Executors.newFixedThreadPool(2)

  private const val PORT = 8080

  fun start() {
    if (server != null) return

    try {
      server = HttpServer.create(InetSocketAddress(PORT), 0).apply {
        createContext("/api/pathfind") { handlePathfind(it) }
        createContext("/api/loadmap") { handleLoadMap(it) }
        createContext("/keepalive") { handleKeepAlive(it) }
        executor = this@PathfinderServer.executor
        start()
      }
      println("[Swift] Pathfinder server started on port $PORT")
    } catch (e: Exception) {
      println("[Swift] Failed to start server: ${e.message}")
    }
  }

  fun stop() {
    server?.stop(0)
    server = null
    println("[Swift] Pathfinder server stopped")
  }

  private fun handleKeepAlive(exchange: HttpExchange) {
    sendResponse(exchange, 200, """{"placeholder type shit"}""")
  }

  private fun handleLoadMap(exchange: HttpExchange) {
    sendResponse(exchange, 200, """{"placeholder type shit"}""")
  }

  private fun handlePathfind(exchange: HttpExchange) {
    if (exchange.requestMethod == "OPTIONS") {
      exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
      exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
      exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
      exchange.sendResponseHeaders(204, -1)
      return
    }

    if (exchange.requestMethod != "POST") {
      sendResponse(exchange, 405, """{"error": "Method not allowed"}""")
      return
    }

    try {
      val body = InputStreamReader(exchange.requestBody).use { it.readText() }
      val json = JsonParser.parseString(body).asJsonObject

      val startCoords = json.get("start").asString.split(",").map { it.trim().toInt() }
      val endCoords = json.get("end").asString.split(",").map { it.trim().toInt() }

      val startX = startCoords[0]
      val startY = startCoords[1] + 1
      val startZ = startCoords[2]
      val endX = endCoords[0]
      val endY = endCoords[1] + 1
      val endZ = endCoords[2]

      val future = MinecraftClient.getInstance().submit<String> {
        runPathfinding(startX, startY, startZ, endX, endY, endZ)
      }

      val result = future.get(30, TimeUnit.SECONDS)
      sendResponse(exchange, 200, result)

    } catch (e: Exception) {
      e.printStackTrace()
      sendResponse(exchange, 500, gson.toJson(mapOf(
        "error" to (e.message ?: "Unknown error"),
        "keynodes" to emptyList<Any>(),
        "path_between_key_nodes" to emptyList<Any>()
      )))
    }
  }

  private fun runPathfinding(startX: Int, startY: Int, startZ: Int, endX: Int, endY: Int, endZ: Int): String {
    val mc = MinecraftClient.getInstance()

    if (mc.world == null || mc.player == null) {
      return gson.toJson(mapOf(
        "error" to "Not in world",
        "keynodes" to emptyList<Any>(),
        "path_between_key_nodes" to emptyList<Any>()
      ))
    }

    val ctx = CalculationContext()
    val goal = Goal(endX, endY, endZ, ctx)
    val pathfinder = AStarPathfinder(startX, startY, startZ, goal, ctx)
    val path = pathfinder.findPath()

    if (path == null) {
      return gson.toJson(mapOf(
        "error" to "No path found",
        "keynodes" to emptyList<Any>(),
        "path_between_key_nodes" to emptyList<Any>()
      ))
    }

    val keyNodes = KeyNodeExtractor.extract(path.points)

    val keyNodesJson = keyNodes.map { mapOf("x" to it.x, "y" to it.y - 1, "z" to it.z) }
    val pathJson = path.points.map { mapOf("x" to it.x, "y" to it.y - 1, "z" to it.z) }

    return gson.toJson(mapOf(
      "keynodes" to keyNodesJson,
      "path_between_key_nodes" to pathJson,
      "time_ms" to path.timeTaken
    ))
  }

  private fun sendResponse(exchange: HttpExchange, code: Int, response: String) {
    val bytes = response.toByteArray(Charsets.UTF_8)
    exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
    exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
    exchange.sendResponseHeaders(code, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
  }
}
