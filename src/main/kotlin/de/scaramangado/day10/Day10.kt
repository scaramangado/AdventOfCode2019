package de.scaramangado.day10

import de.scaramangado.day1.Day1
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sign

fun main() {

  originalInput
      .map {
        it to originalInput.minus(it)
            .map { a -> it directionTo a }
            .toSet()
      }
      .map { it.first to it.second.size }
      .maxBy { it.second }!!
      .also { println("Answer 1:\n${it.second}") }.first
      .let { Laser(originalInput.minus(it).toMutableList(), it) }
      .run {
        (1..200).map { this.shoot() }[199].run { println("Answer 2:\n${this.x.times(100).plus(this.y)}") }
      }
}

class Laser(private val asteroids: MutableList<Point>, private val position: Point) {

  private var direction: Direction = Direction(0, -1)
  private val asteroidDirections =
      asteroids.map { position directionTo it }
          .sortedBy { it.angle() }
          .toMutableList()

  init {

    if (asteroids.none { position directionTo it == direction }) {
      direction = asteroidDirections
          .filter { it.angle() > direction.angle() }
          .minBy { it.angle() } ?: asteroidDirections[0]
    }
  }

  fun shoot(): Point {

    return asteroids
        .filter { position directionTo it == direction }
        .also {
          if (it.size <= 1) {
            asteroidDirections.remove(direction)
          }
        }
        .minBy { position distanceTo it }
        .also { asteroids.remove(it) }
        .also {
          direction = asteroidDirections
              .filter { d -> d.angle() > direction.angle() }
              .filter { d -> asteroids.map { a -> position directionTo a }.contains(d) }
              .minBy { d -> d.angle() } ?: asteroidDirections[0]
        } ?: throw IllegalStateException()
  }
}

class Point(val x: Int, val y: Int) {

  infix fun directionTo(other: Point): Direction =
      Direction(other.x - x, other.y - y)

  infix fun distanceTo(other: Point): Int =
      abs(other.x - x) + abs(other.y - y)

  override fun toString(): String = "($x, $y)"
}

class Direction(val dx: Int, val dy: Int) {

  val gcd = gcd(dx, dy)

  fun angle(): Double = Math.PI - atan2(dy.toDouble(), -dx.toDouble())

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Direction -> dx.sign == other.dx.sign
          && dy.sign == other.dy.sign
          && dx / gcd == other.dx / other.gcd
          && dy / gcd == other.dy / other.gcd
      else -> false
    }
  }

  override fun hashCode(): Int {

    if (this == Direction(0, 0)) {
      return 0
    }

    var signs = 0
    if (dx.sign == 1) signs += 1
    if (dy.sign == 1) signs += 2

    var result = signs
    result = (113 * result + dy / gcd)
    result = (113 * result + dx / gcd)

    return result
  }

  override fun toString(): String = gcd(dx, dy).let { "d(${dx / it}, ${dy / it})" }
}

private fun gcd(a: Int, b: Int): Int {

  return if (b == 0) a
  else gcd(b, a % b)
}

val originalInput by lazy {

  Day1::class.java.classLoader.getResource("day10/input_puzzle_1")?.readText()
      ?.lines()
      ?.map { line ->
        line.toCharArray()
            .mapIndexed { i, c ->
              when (c) {
                '.' -> i to null
                '#' -> i to i
                else -> throw IllegalArgumentException("'$c' is not a valid character.")
              }
            }
            .mapNotNull { it.second }
      }
      ?.mapIndexed { line, column -> column.map { Point(it, line) } }
      ?.flatten()
      ?.toList() ?: emptyList()
}
