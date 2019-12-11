package de.scaramangado.day11

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange
import de.scaramangado.intcode.NumberExchange
import kotlin.concurrent.thread

fun main() {

  Robot(originalInput)
      .generatePaintInstructions()
      .map { it.first.position }
      .toSet()
      .run { println("\nAnswer 1:\n${this.size}") }

  Robot(originalInput, Color.WHITE).run {
    generatePaintInstructions()
    println("\nAnswer 2:")
    hull.print()
  }
}

private class Robot(intCode: List<Long>, private val initialColor: Color = Color.BLACK) {

  private val writeToCpu = IntExchange(NumberExchange.DebugMode.SLEEP)
  private val readFromCpu = IntExchange(NumberExchange.DebugMode.SLEEP)
  private val cpu = CPU(intCode, writeToCpu, readFromCpu)

  val hull = Hull()

  private var position = Position(0, 0)
  private var direction = Direction.UP

  private var counter = 0

  fun generatePaintInstructions(): List<Pair<Panel, Color>> {

    val instructions = mutableListOf<Pair<Panel, Color>>()

    thread { cpu.compute() }
    hull.getPanel(position).color = initialColor

    while (readFromCpu.running()) {

      if (++counter % 100 == 0) print(".")

      with(hull.getPanel(position)) {

        writeToCpu.addNumber(this.color.toInt())

        if (this paint colorFromInt(readFromCpu.readNumber())) {
          instructions.add(this to this.color)
        }
      }

      when (readFromCpu.readNumber()) {
        0 -> turnLeft()
        1 -> turnRight()
      }

      moveForward()
    }

    return instructions.toList()
  }

  private fun moveForward() {
    position = when (direction) {
      Direction.UP -> Position(position.x, position.y - 1)
      Direction.DOWN -> Position(position.x, position.y + 1)
      Direction.LEFT -> Position(position.x - 1, position.y)
      Direction.RIGHT -> Position(position.x + 1, position.y)
    }
  }

  private fun turnLeft() {
    direction = when (direction) {
      Direction.UP -> Direction.LEFT
      Direction.DOWN -> Direction.RIGHT
      Direction.LEFT -> Direction.DOWN
      Direction.RIGHT -> Direction.UP
    }
  }

  private fun turnRight() {
    direction = when (direction) {
      Direction.UP -> Direction.RIGHT
      Direction.DOWN -> Direction.LEFT
      Direction.LEFT -> Direction.UP
      Direction.RIGHT -> Direction.DOWN
    }
  }

  private fun colorFromInt(i: Int) = when (i) {
    0 -> Color.BLACK
    1 -> Color.WHITE
    else -> throw IllegalArgumentException()
  }

  private fun Color.toInt() = when (this) {
    Color.BLACK -> 0
    Color.WHITE -> 1
  }

  private infix fun Panel.paint(newColor: Color): Boolean {
    return (color != newColor).also { color = newColor }
  }

  private enum class Direction {
    UP, DOWN, LEFT, RIGHT
  }
}

private class Hull {

  val panels = mutableListOf<Panel>()

  fun getPanel(position: Position) =
      panels
          .filter { it.position == position }
          .also { require(it.size <= 1) }
          .lastOrNull() ?: (Panel(position).also { panels.add(it) })

  fun print() {

    if (panels.isEmpty()) return

    val xRange = panels.map { it.position.x }.let { it.min()!!..it.max()!! }
    val yRange = panels.map { it.position.y }.let { it.min()!!..it.max()!! }

    yRange.forEach { y ->
      println(xRange
                  .map { x -> getPanel(Position(x, y)).color }.joinToString("") {
            when (it) {
              Color.BLACK -> " "
              Color.WHITE -> "\u2588"
            }
          }
      )
    }
  }
}

private class Panel(val position: Position, var color: Color = Color.BLACK) {

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Panel -> position == other.position
      else -> false
    }
  }

  override fun hashCode(): Int = position.x + 31 * position.y
}

private data class Position(val x: Int, val y: Int)

private enum class Color {
  BLACK, WHITE
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day11/input_puzzle_1")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}
