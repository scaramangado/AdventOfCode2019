package de.scaramangado.day3

import de.scaramangado.day1.Day1
import java.util.stream.Collectors.*
import kotlin.math.abs

fun main() {

  val paths = getPathsFromFile().toTypedArray()
  val intersections = findAllIntersections(*paths)

  println("Answer 1:")
  println(intersections
              .map { it to Point(0, 0) }
              .min())
  println()
  println("Answer 2:")

  println(intersections
              .map { paths.map { p -> p.stepsToPoint(it) }.sum() }
              .min()
  )
}

fun findAllIntersections(vararg paths: Path): Set<Point> {

  val pointSets = paths.map { it.points }
  val firstSet = pointSets[0].toMutableSet()
  val otherSets = pointSets.subList(1, pointSets.size)

  return firstSet
      .parallelStream()
      .filter { p -> otherSets.findLast { !it.contains(p) }.isNullOrEmpty() }
      .collect(toSet())
}

class Path(private val directions: List<String>) {

  val points by lazy {

    val pointSet = mutableListOf<Point>()
    var currentPoint = Point(0, 0)

    directions
        .map { Move(it) }
        .forEach {
          for (i in 1..it.length) {
            currentPoint = when (it.direction) {
              Direction.U -> Point(currentPoint.x, currentPoint.y + 1)
              Direction.D -> Point(currentPoint.x, currentPoint.y - 1)
              Direction.L -> Point(currentPoint.x - 1, currentPoint.y)
              Direction.R -> Point(currentPoint.x + 1, currentPoint.y)
            }

            pointSet.add(currentPoint.copy())
          }
        }

    pointSet.toList()
  }

  fun stepsToPoint(point: Point): Int {
    val index = points.indexOf(point)
    require(index >= 0)
    return index + 1
  }

  inner class Move(val direction: Direction, val length: Int) {
    constructor(command: String) : this(parseDirection(command), parseLength(command))
  }

  enum class Direction {
    U, D, L, R
  }
}

class Point(val x: Int, val y: Int) {

  infix fun to(otherPoint: Point): Int =
      abs(x - otherPoint.x) + abs(y - otherPoint.y)

  fun copy(): Point = Point(x, y)

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Point -> x == other.x && y == other.y
      else -> false
    }
  }

  override fun hashCode(): Int {
    return 10000 * x + y
  }
}

private fun printPoint(point: Point): String = "Point(${point.x}, ${point.y})"

private fun parseDirection(command: String): Path.Direction {
  require(command.matches(Regex("[UDLR]\\d+")))
  return Path.Direction.valueOf(command.substring(0, 1))
}

private fun parseLength(command: String): Int {
  require(command.matches(Regex("[UDLR]\\d+")))
  return command.removeRange(0, 1).toInt()
}

private fun getPathsFromFile(): List<Path> {
  return Day1::class.java.classLoader.getResource("day3/input_puzzle_1")?.readText()?.split("\n")
      ?.map { Path(it.split(",")) } ?: emptyList()
}
