package de.scaramangado.day13

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange
import de.scaramangado.intcode.NumberExchange
import de.scaramangado.intcode.SingleValueExchange
import kotlin.concurrent.thread
import kotlin.math.sign

fun main() {

  Arcade(originalInput)
      .also { it.autoGame() }
      .run { println("Answer 1:\n${this.blockCount()}") }

  Arcade(playInput)
      .also { it.freeGame() }
      .run { println("Answer 2:\n${this.score}") }
}

private class Arcade(intCode: List<Long>) {

  var score = 0

  private val readFromCpu = IntExchange()
  private val writeToCpu = SingleValueExchange()
  private val cpu = CPU(intCode, writeToCpu, readFromCpu)

  private val screen = Screen()

  private var ball = Tile(-1,-1)
  private var paddle = Tile(-1,-1)

  fun autoGame() {

    cpu.compute()
    while (readFromCpu.running() || readFromCpu.ready()) {

      screen.printTile(Tile(readFromCpu.readNumber(), readFromCpu.readNumber(), readFromCpu.readNumber()))
    }
  }

  fun freeGame() {

    readFromCpu.debugMode = NumberExchange.DebugMode.SLEEP
    thread { cpu.compute() }
    Thread.sleep(3000)

    var ballDrawn = false
    var paddleDrawn = false

    while (readFromCpu.running() || readFromCpu.ready()) {

      val x = readFromCpu.readNumber()

      if (x == -1) {
        readFromCpu.readNumber()
        score = readFromCpu.readNumber()
        continue
      }

      val y = readFromCpu.readNumber()
      val type = readFromCpu.readNumber()

      Tile(x, y, type)
          .also {
            when (it.type) {
              TileType.BALL -> {
                ball = it
                ballDrawn = true
                screen.printTile(it)
              }
              TileType.PADDLE -> {
                paddle = it
                paddleDrawn = true
                screen.printTile(it)
              }

              else -> {}
            }
          }

      if (ballDrawn && paddleDrawn) {

        ballDrawn = false
        paddleDrawn = false

        when ((paddle.x - ball.x).sign) {
          1 -> {
            writeToCpu.addNumber(-1)
          }

          -1 -> {
            writeToCpu.addNumber(1)
          }

          else -> {
            writeToCpu.addNumber(0)
            paddleDrawn = true
          }
        }
      }
    }
  }

  fun blockCount() = screen.allTiles.filter { it.type == TileType.BLOCK }.count()
}

private class Screen {

  val allTiles = mutableListOf<Tile>()

  fun printTile(tile: Tile) {
    allTiles
        .filter { it == tile }
        .uniqueResult()
        ?.also { it.type = tile.type }
        ?: allTiles.add(tile)
  }

  fun printScreen() {

    if (allTiles.isEmpty()) return

    val xRange = allTiles.map { it.x }.let { it.min()!!..it.max()!! }
    val yRange = allTiles.map { it.y }.let { it.min()!!..it.max()!! }

    yRange.forEach { y ->
      println(xRange
                  .map { x -> allTiles.filter { it == Tile(x, y) }.uniqueResult() }
                  .joinToString("") { it.toString() }
      )
    }
  }
}

private fun <E> List<E>.uniqueResult(): E? {

  require(size <= 1)

  return getOrNull(0)
}

private class Tile(val x: Int, val y: Int, var type: TileType = TileType.EMPTY) {

  constructor(x: Int, y: Int, t: Int) : this(x, y, when (t) {
    0 -> TileType.EMPTY
    1 -> TileType.WALL
    2 -> TileType.BLOCK
    3 -> TileType.PADDLE
    4 -> TileType.BALL
    else -> throw IllegalArgumentException("$t is not a valid tile type.")
  })

  override fun equals(other: Any?) =
      when (other) {
        is Tile -> x == other.x && y == other.y
        else -> false
      }

  override fun hashCode() = x + 31 * y

  override fun toString() =
      when (type) {
        TileType.EMPTY -> " "
        TileType.WALL -> "\u2588"
        TileType.BLOCK -> "\u25AF"
        TileType.PADDLE -> "_"
        TileType.BALL -> "o"
      }
}

private enum class TileType {
  EMPTY, WALL, BLOCK, PADDLE, BALL
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day13/input_puzzle_1")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}

private val playInput by lazy {
  Day1::class.java.classLoader.getResource("day13/input_puzzle_2")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}
