package de.scaramangado.day1

import kotlin.math.floor
import kotlin.math.roundToInt

class Day1

fun main() {

  println("Answer 1:")
  println(readInput()
              .asSequence()
              .map { calculateFuelNeeded(it) }
              .sum())

  println()
  println("Answer 2:")
  println(readInput()
              .asSequence()
              .map { calculateTotalFuel(it) }
              .sum())
}

private fun calculateTotalFuel(baseFuel: Int): Int {

  val fuelNeeded = calculateFuelNeeded(baseFuel)

  return if (fuelNeeded <= 0) 0
  else fuelNeeded + calculateTotalFuel(fuelNeeded)
}

private fun calculateFuelNeeded(mass: Int): Int =
    floor(mass / 3.0).roundToInt() - 2

private fun readInput(): List<Int> =
    Day1::class.java.classLoader.getResource("day1/input_puzzle_1")?.readText()
        ?.split("\n")
        ?.map { it.toInt() }
        ?: emptyList()
