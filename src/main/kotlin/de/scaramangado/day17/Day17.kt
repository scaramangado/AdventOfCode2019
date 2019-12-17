package de.scaramangado.day17

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange

fun main() {

  Camera(originalInput)
      .also { it.generateMap() }
      .also {
        println("Answer 1:\n${it.getCrossings().map { t -> t.x * t.y }.sum()}")
      }
      .also {
        println("Answer 2:\n${it.findRobots()}")
      }
}

private class Camera(private val intCode: List<Long>) {

  private val map = mutableMapOf<Pair<Int, Int>, Tile>()

  fun generateMap() {

    val readFromCpu = IntExchange()
    val cpu = CPU(intCode, output = readFromCpu)

    cpu.compute()

    var x = 0
    var y = 0

    while (readFromCpu.ready()) {
      when (readFromCpu.readNumber().toChar()) {
        '.' -> map[x to y] = Tile(x++, y, Type.SPACE)
        '#' -> map[x to y] = Tile(x++, y, Type.SCAFFOLD)
        in listOf('^', '<', '>', 'v') -> map[x to y] = Tile(x++, y, Type.ROBOT)

        '\n' -> {
          x = 0
          y++
        }
      }
    }
  }

  fun getCrossings(): Set<Tile> {

    val maxX = map.keys.map { it.first }.max()!!
    val maxY = map.keys.map { it.second }.max()!!

    return map.values
        .asSequence()
        .filter { it.x in 1 until maxX }
        .filter { it.y in 1 until maxY }
        .filter { it.type == Type.SCAFFOLD }
        .filter { map[it.x to it.y + 1]!!.type == Type.SCAFFOLD }
        .filter { map[it.x to it.y - 1]!!.type == Type.SCAFFOLD }
        .filter { map[it.x + 1 to it.y]!!.type == Type.SCAFFOLD }
        .filter { map[it.x - 1 to it.y]!!.type == Type.SCAFFOLD }
        .toSet()
  }

  fun findRobots(): Int {

    val robotCode = intCode.toMutableList()
    robotCode[0] = 2

    val writeToCpu = IntExchange()
    val readFromCpu = IntExchange()
    val cpu = CPU(robotCode, writeToCpu, readFromCpu)

    listOf(
        'A', ',', 'C', ',', 'A', ',', 'B', ',', 'B', ',', 'A', ',', 'C', ',', 'B', ',', 'C', ',', 'C', '\n',
        // L8, R10, L8, R8
        'L', ',', '8', ',', 'R', ',', '1', '0', ',', 'L', ',', '8', ',', 'R', ',', '8', '\n',
        // L8, R6, R6, R10, L8
        'L', ',', '8', ',', 'R', ',', '6', ',', 'R', ',', '6', ',', 'R', ',', '1', '0', ',', 'L', ',', '8', '\n',
        // L12, R8, R8
        'L', ',', '1', '2', ',', 'R', ',', '8', ',', 'R', ',', '8', '\n',
        'n', '\n'
    )
        .forEach {
          writeToCpu.addNumber(it.toInt())
        }

    cpu.compute()

    val numberOfSpaces = map.values.filter { it.type == Type.SPACE }.count()
    var spaceCount = 0

    while (true) {

      val nextChar = readFromCpu.readNumber().toChar()

      if (nextChar == '.') spaceCount++
      if (spaceCount >= numberOfSpaces && nextChar == '\n') break
    }

    var output = 0

    while (readFromCpu.ready()) {
      output = readFromCpu.readNumber()
    }

    return output
  }
}

private data class Tile(val x: Int, val y: Int, val type: Type)

private enum class Type {
  SPACE, SCAFFOLD, ROBOT
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day17/input_puzzle_1")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}
