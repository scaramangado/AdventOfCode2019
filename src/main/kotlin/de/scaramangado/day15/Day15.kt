package de.scaramangado.day15

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange
import kotlin.concurrent.thread

fun main() {

  Droid(originalInput)
      .also { it.mapOutFloor() }
      .also { println("Answer 1:\n${it.findPathLength()}") }
      .also { println("Answer 2:\n${it.findMaxDistanceFromOxygenSystem()}") }
}

private class Droid(private val intCode: List<Long>) {

  private val map = mutableMapOf(Tile(0, 0, Type.INITIAL) to 0)

  fun mapOutFloor() {

    val readFromCpu = IntExchange()
    val writeToCpu = IntExchange()

    val cpu = CPU(intCode, writeToCpu, readFromCpu)

    thread { cpu.compute() }

    var position = Tile(0, 0, Type.FLOOR)

    for (i in 0..5000) {

      val coordinates = Direction.values()
          .map { it to position.coordinatesInDirection(it).let { p -> getTile(p.x, p.y) } }

      val nextDirection = coordinates
          .filter { it.second.type == Type.UNKNOWN }
          .ifEmpty {
            coordinates.filter { getTile(it.second.x, it.second.y).type != Type.WALL }
          }
          .minBy { map[it.second] ?: 0 }
          ?.first ?: Direction.NORTH

      writeToCpu.addNumber(nextDirection.toInt())

      val nextTile =
          when (nextDirection) {
            Direction.NORTH -> position.copy(y = position.y - 1)
            Direction.SOUTH -> position.copy(y = position.y + 1)
            Direction.EAST -> position.copy(x = position.x + 1)
            Direction.WEST -> position.copy(x = position.x - 1)
          }

      val type = when (val code = readFromCpu.readNumber()) {
        0 -> Type.WALL

        1 -> {
          position = nextTile
          Type.FLOOR
        }

        2 -> {
          position = nextTile
          Type.OXYGEN_SYSTEM
        }

        else -> throw IllegalStateException("$code is not a valid status code.")
      }

      if (!map.contains(nextTile)) {
        map[nextTile.copy(type = type)] = if (type != Type.WALL) 1 else 0
      } else {
        map[nextTile] = map[nextTile]!! + if (type != Type.WALL) 1 else 0
      }
    }

    writeToCpu.addNumber(99) // End Program
  }

  fun findPathLength(): Int =
      getDistances { if (it.type == Type.INITIAL) 0 else Int.MAX_VALUE }
          .last { it.tile.type == Type.OXYGEN_SYSTEM }.distance

  fun findMaxDistanceFromOxygenSystem(): Int =
      getDistances { if (it.type == Type.OXYGEN_SYSTEM) 0 else Int.MAX_VALUE }
          .maxBy { it.distance }!!.distance

  fun getDistances(init: (Tile) -> Int): Set<Node> {

    val unvisited = map
        .keys
        .map {
          val distance = init(it)

          val neighbors = setOf(
              getTile(it.x, it.y - 1),
              getTile(it.x, it.y + 1),
              getTile(it.x - 1, it.y),
              getTile(it.x + 1, it.y)
          ).filter { t -> t.type !in listOf(Type.WALL, Type.UNKNOWN) }.toSet()

          Node(it, distance, neighbors)
        }.toMutableSet()

    val visited = mutableSetOf<Node>()

    while (unvisited.isNotEmpty()) {
      val currentNode = unvisited.minBy { it.distance }!!
      unvisited.remove(currentNode)
      visited.add(currentNode)
      val nextDistance = currentNode.distance + 1
      currentNode.neighbors
          .mapNotNull { unvisited.lastOrNull { n -> n.tile == it } }
          .forEach {
            if (it.distance > nextDistance) {
              it.distance = nextDistance
            }
          }
    }

    return visited.filter { it.tile.type !in listOf(Type.WALL, Type.UNKNOWN) }.toSet()
  }

  private fun getTile(x: Int, y: Int) = map.keys.lastOrNull { x == it.x && y == it.y } ?: Tile(x, y, Type.UNKNOWN)

  private inner class Node(val tile: Tile, var distance: Int, val neighbors: Set<Tile>)
}

private data class Tile(val x: Int, val y: Int, val type: Type) {
  override fun toString() =
      when (type) {
        Type.INITIAL -> "I"
        Type.FLOOR -> " "
        Type.WALL -> "\u2588"
        Type.OXYGEN_SYSTEM -> "O"
        Type.UNKNOWN -> "?"
      }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Tile -> x == other.x && y == other.y
      else -> false
    }
  }

  override fun hashCode() = x + 31 * y

  fun coordinatesInDirection(direction: Direction) =
      when (direction) {
        Direction.NORTH -> this.copy(y = y - 1)
        Direction.SOUTH -> this.copy(y = y + 1)
        Direction.EAST -> this.copy(x = x + 1)
        Direction.WEST -> this.copy(x = x - 1)
      }
}

private enum class Type {
  INITIAL, FLOOR, WALL, OXYGEN_SYSTEM, UNKNOWN
}

private enum class Direction {
  NORTH, SOUTH, EAST, WEST;

  fun toInt() =
      when (this) {
        NORTH -> 1
        SOUTH -> 2
        WEST -> 3
        EAST -> 4
      }
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day15/input_puzzle_1")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}
