package de.scaramangado.day12

import de.scaramangado.day1.Day1
import kotlin.math.abs

fun main() {

  Universe(originalInput)
      .also { it.simulateTimeSteps(1000) }
      .run { println("Answer 1:\n${this.getTotalEnergy()}") }

  listOf(Universe::simulateXStep, Universe::simulateYStep, Universe::simulateZStep)
      .map { Universe(originalInput).findLoop(it) }
      .lcm()
      .run { println("Answer 2:\n${this}") }
}

private class Universe(private val bodies: List<Body>) {

  private val pairs = bodies.map { (bodies - it).map { list -> BodyPair(it, list) } }.flatten().toSet()

  fun simulateTimeSteps(n: Int) {

    require(n >= 0)

    if (n == 0) return

    if (n > 1) {
      simulateTimeSteps(1)
      simulateTimeSteps(n - 1)
      return
    }

    simulateXStep()
    simulateYStep()
    simulateZStep()
  }

  fun simulateXStep() {
    pairs.forEach {
      with(it.body1.x - it.body2.x) {
        when {
          this > 0L -> {
            it.body1.vx--
            it.body2.vx++
          }

          this < 0L -> {
            it.body1.vx++
            it.body2.vx--
          }

          this == 0L -> {
          }

          else -> throw IllegalStateException()
        }
      }
    }

    bodies.forEach { it.x += it.vx }
  }

  fun simulateYStep() {
    pairs.forEach {
      with(it.body1.y - it.body2.y) {
        when {
          this > 0L -> {
            it.body1.vy--
            it.body2.vy++
          }

          this < 0L -> {
            it.body1.vy++
            it.body2.vy--
          }

          this == 0L -> {}

          else -> throw IllegalStateException()
        }
      }
    }

    bodies.forEach { it.y += it.vy }
  }

  fun simulateZStep() {
    pairs.forEach {
      with(it.body1.z - it.body2.z) {
        when {
          this > 0L -> {
            it.body1.vz--
            it.body2.vz++
          }

          this < 0L -> {
            it.body1.vz++
            it.body2.vz--
          }

          this == 0L -> {
          }

          else -> throw IllegalStateException()
        }
      }
    }

    bodies.forEach { it.z += it.vz }
  }

  fun findLoop(simulateMethod: Universe.() -> Unit): Long {

    val initialState = bodies.map { it.copy() }
    simulateMethod()

    var i = 1L

    while (bodies != initialState) {
      i++
      simulateMethod()
    }

    return i
  }

  fun getTotalEnergy() = bodies.map { it.getEnergy() }.sum()

  override fun toString(): String = bodies.joinToString("\n") { it.toString() }
}

private class BodyPair(val body1: Body, val body2: Body) {
  override fun equals(other: Any?): Boolean {
    return when (other) {
      is BodyPair -> (body1 == other.body1 && body2 == other.body2) || (body1 == other.body2 && body2 == other.body1)
      else -> false
    }
  }

  override fun hashCode() = body1.hashCode() + body2.hashCode()
}

private data class Body(var x: Long, var y: Long, var z: Long, var vx: Long = 0L, var vy: Long = 0L,
                        var vz: Long = 0L) {

  fun getEnergy() = (abs(x) + abs(y) + abs(z)) * (abs(vx) + abs(vy) + abs(vz))

  override fun toString(): String = "($x, $y, $z) v($vx, $vy, $vz)"
}

private fun parsePositionalData(positionString: String): Body {

  fun getCoordinate(name: String): Long {
    return positionString.split("$name=")[1].split(Regex("[,>]"))[0].toLong()
  }

  return Body(getCoordinate("x"), getCoordinate("y"), getCoordinate("z"))
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day12/input_puzzle_1")?.readText()
      ?.lines()
      ?.map { parsePositionalData(it) }
      ?: emptyList()
}

private fun List<Number>.lcm(): Long {

  require(isNotEmpty())

  fun lcm(vararg numbers: Long): Long {

    if (numbers.size > 2) {
      return lcm(lcm(*numbers.copyOfRange(0, 2)), *numbers.copyOfRange(2, numbers.size))
    }

    fun gcd(a: Long, b: Long): Long {
      return if (b == 0L) a
      else gcd(b, a % b)
    }

    return numbers[0] * numbers[1] / gcd(numbers[0], numbers[1])
  }

  return if (size == 1) this[0].toLong()
  else lcm(*map { it.toLong() }.toLongArray())
}
